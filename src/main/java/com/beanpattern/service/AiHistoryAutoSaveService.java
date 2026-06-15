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
    private AiPatternPipelineService aiPatternPipelineService;

    @Autowired
    private BpHistoryService bpHistoryService;

    @Autowired
    private AiGenerateTaskMapper taskMapper;

    @Async("taskExecutor")
    public void processAndSaveHistory(AiGenerateTask task) {
        try {
            if (task.getUserId() == null || task.getUserId() <= 0) {
                log.info("[AiHistoryAutoSave] skip system/admin test task: {}", task.getTaskId());
                return;
            }
            System.out.println("[AiHistoryAutoSave] 开始异步处理: " + task.getTaskId());

            String brand = StringUtils.hasText(task.getBrand()) ? task.getBrand() : "MARD";
            boolean mirror = Boolean.TRUE.equals(task.getMirror());
            AiPatternPipelineResult result = aiPatternPipelineService.process(task);

            String mappedPixelDataJson = objectMapper.writeValueAsString(result.mappedPixelData());

            BpHistory history = new BpHistory();
            history.setUserId(task.getUserId());
            history.setTaskId(task.getTaskId());
            history.setSourceType("AI");
            history.setBrand(brand);
            history.setColorCount(result.colorCount());
            history.setName("AI记录#" + task.getTaskId());
            history.setGridSize(result.selectedCandidate().gridSize());
            history.setAiStyle(task.getStyle());
            String sourceUrl = result.selectedImageUrl();
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
                latest.setSelectedImageVariant(result.selectedImageVariant());
                latest.setProcessMeta(result.processMetaJson());
                latest.setUpdatedAt(new Date());
                taskMapper.updateById(latest);
            }

            System.out.println("[AiHistoryAutoSave] 异步处理完成: " + task.getTaskId()
                    + ", historyId=" + history.getId()
                    + ", selected=" + result.selectedImageVariant()
                    + ", colors=" + result.colorCount());
        } catch (Exception e) {
            log.error("[AiHistoryAutoSave] 异步处理失败: {}", task.getTaskId(), e);
        }
    }
}
