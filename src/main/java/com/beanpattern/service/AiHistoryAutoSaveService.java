package com.beanpattern.service;

import com.beanpattern.entity.AiGenerateTask;
import com.beanpattern.entity.BpHistory;
import com.beanpattern.mapper.AiGenerateTaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * AI 结果异步处理：图像后处理 + 自动写入时光机
 * 独立 Service 确保 @Async 通过 AOP 代理生效
 */
@Service
public class AiHistoryAutoSaveService {

    private static final Logger log = LoggerFactory.getLogger(AiHistoryAutoSaveService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AiImageProcessor aiImageProcessor;

    @Autowired
    private BpHistoryService bpHistoryService;

    @Autowired
    private AiGenerateTaskMapper taskMapper;

    @Autowired
    private ImageStorageService imageStorageService;

    @Async("taskExecutor")
    public void processAndSaveHistory(AiGenerateTask task) {
        try {
            if (task.getUserId() == null || task.getUserId() <= 0) {
                log.info("[AiHistoryAutoSave] skip system/admin test task: {}", task.getTaskId());
                return;
            }
            System.out.println("[AiHistoryAutoSave] 开始异步处理: " + task.getTaskId());

            String brand = StringUtils.hasText(task.getBrand()) ? task.getBrand() : "MARD";
            int colorCount = task.getColorCount() != null ? task.getColorCount() : 0;
            boolean mirror = Boolean.TRUE.equals(task.getMirror());
            int gridSize = task.getFinalGridWidth() != null ? task.getFinalGridWidth()
                    : (task.getFinalGridHeight() != null ? task.getFinalGridHeight()
                    : ("small".equals(task.getSizeMode()) ? 32 : 48));
            int threshold = 0;

            AiImageProcessor.ProcessedResult result;
            if (StringUtils.hasText(task.getAiImageKey())) {
                ImageStorageService.StoredImage image = imageStorageService.readKey(task.getAiImageKey());
                result = aiImageProcessor.process(image.bytes(), brand, colorCount, mirror, gridSize, threshold);
            } else {
                ImageStorageService.StoredImage image = imageStorageService.readPublicUrl(task.getAiImageUrl());
                result = aiImageProcessor.process(image.bytes(), brand, colorCount, mirror, gridSize, threshold);
            }

            String mappedPixelDataJson = objectMapper.writeValueAsString(result.mappedPixelData());

            BpHistory history = new BpHistory();
            history.setUserId(task.getUserId());
            history.setTaskId(task.getTaskId());
            history.setSourceType("AI");
            history.setBrand(brand);
            history.setColorCount(result.colorCount());
            history.setName("AI记录#" + task.getTaskId());
            history.setGridSize(gridSize);
            history.setAiStyle(task.getStyle());
            String sourceUrl = task.getAiImageUrl();
            if (mirror && sourceUrl != null && sourceUrl.startsWith("http")) {
                sourceUrl = sourceUrl + (sourceUrl.contains("?") ? "&" : "?") + "imageMogr2/flip/horizontal";
            }
            history.setSourceUrl(sourceUrl);
            history.setMappedPixelData(mappedPixelDataJson);
            history.setExpiresAt(LocalDateTime.now().plusDays(30));
            bpHistoryService.save(history);

            AiGenerateTask latest = taskMapper.findByTaskId(task.getTaskId());
            if (latest != null) {
                latest.setMappedPixelData(mappedPixelDataJson);
                latest.setHistoryId(history.getId());
                latest.setUpdatedAt(new Date());
                taskMapper.updateById(latest);
            }

            System.out.println("[AiHistoryAutoSave] 异步处理完成: " + task.getTaskId()
                    + ", historyId=" + history.getId()
                    + ", colors=" + result.colorCount());
        } catch (Exception e) {
            log.error("[AiHistoryAutoSave] 异步处理失败: {}", task.getTaskId(), e);
        }
    }
}
