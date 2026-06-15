package com.beanpattern.service;

import com.beanpattern.controller.AiTaskController.AiGenerateRequest;
import com.beanpattern.config.AiServiceProperties;
import com.beanpattern.entity.AiGenerateTask;
import com.beanpattern.entity.AiMagicStyle;
import com.beanpattern.entity.AiSizePreset;
import com.beanpattern.mapper.AiGenerateTaskMapper;
import com.beanpattern.mapper.AiMagicStyleMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.AiGenerateMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AiTaskService {

    private static final Logger log = LoggerFactory.getLogger(AiTaskService.class);

    @Autowired
    private AiGenerateTaskMapper taskMapper;

    @Autowired
    private AiMagicStyleMapper aiMagicStyleMapper;

    @Autowired
    private AiSizePresetService aiSizePresetService;

    @Autowired
    private AiTaskPublisher aiTaskPublisher;

    @Autowired
    private AiServiceProperties aiServiceProperties;

    @Autowired
    private AiHistoryAutoSaveService aiHistoryAutoSaveService;

    @Autowired
    private UserService userService;

    @Autowired
    private AiQuotaLogService aiQuotaLogService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String createTask(AiGenerateRequest request, Long userId) {
        AiGenerateTask task = createPendingTask(request, userId);
        String taskId = task.getTaskId();

        if (!aiServiceProperties.isEnabled()) {
            System.out.println("[AI] 服务未启用，任务保持 PENDING: " + taskId);
            return taskId;
        }

        try {
            aiTaskPublisher.publish(buildGenerateMessage(task, request));
            System.out.println("[AI] 任务已投递队列: " + taskId);
        } catch (Exception e) {
            updateTaskStatus(taskId, "FAILED", null, "AI任务投递失败: " + e.getMessage());
            throw new IllegalStateException("AI任务投递失败，请稍后重试", e);
        }

        return taskId;
    }

    public String createAdminPromptTestTask(AiGenerateRequest request) {
        if (!StringUtils.hasText(request.getPromptTemplate())) {
            throw new IllegalArgumentException("Prompt is required");
        }
        request.setImageUrl("");
        request.setPrompt("");
        request.setStyle("ADMIN_PROMPT_TEST");
        request.setBrand("MARD");
        request.setColorCount(0);
        request.setMirror(false);

        return createTask(request, 0L);
    }

    public ApiResponse<Map<String, Object>> getTaskStatus(String taskId) {
        AiGenerateTask task = taskMapper.findByTaskId(taskId);

        if (task == null) {
            return ApiResponse.fail("任务不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getTaskId());
        data.put("status", task.getStatus());
        data.put("imageUrl", task.getImageUrl());
        data.put("originalImageUrl", task.getImageUrl());
        data.put("sourceUrl", task.getImageUrl());
        data.put("sizeMode", task.getSizeMode());
        data.put("sizePreset", task.getSizePreset());
        data.put("sizePresetName", task.getSizePresetName());
        data.put("candidateGrids", AiSizePresetService.parseCandidateGrids(task.getCandidateGrids()));
        data.put("defaultGrid", task.getDefaultGrid());

        // 返回后期处理参数，前端可直接用
        data.put("sizeMode", task.getSizeMode());
        data.put("gridMin", task.getGridMin());
        data.put("gridMax", task.getGridMax());
        data.put("style", task.getStyle());
        data.put("aiStyle", task.getStyle());
        data.put("brand", task.getBrand());
        data.put("colorCount", task.getColorCount());
        data.put("mirror", task.getMirror());
        data.put("rawAiImageUrl", task.getRawAiImageUrl());
        data.put("aiImageKey", task.getAiImageKey());
        data.put("rawAiImageKey", task.getRawAiImageKey());
        data.put("detectedGridWidth", task.getDetectedGridWidth());
        data.put("detectedGridHeight", task.getDetectedGridHeight());
        data.put("finalGridWidth", task.getFinalGridWidth());
        data.put("finalGridHeight", task.getFinalGridHeight());
        data.put("perfectPixelStatus", task.getPerfectPixelStatus());
        data.put("perfectPixelError", task.getPerfectPixelError());
        data.put("selectedImageVariant", task.getSelectedImageVariant());
        if (StringUtils.hasText(task.getProcessMeta())) {
            try {
                data.put("processMeta", objectMapper.readValue(task.getProcessMeta(), Map.class));
            } catch (Exception e) {
                data.put("processMeta", task.getProcessMeta());
            }
        }

        if ("SUCCESS".equals(task.getStatus())) {
            data.put("aiImageUrl", task.getAiImageUrl());
            data.put("completedAt", task.getCompletedAt());
            data.put("historyId", task.getHistoryId());
            if (StringUtils.hasText(task.getMappedPixelData())) {
                try {
                    Object mappedPixelData = objectMapper.readValue(
                            task.getMappedPixelData(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class,
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)));
                    data.put("mappedPixelData", mappedPixelData);
                } catch (Exception e) {
                    log.warn("解析 mappedPixelData 失败: {}", task.getTaskId(), e);
                }
            }
        } else if ("FAILED".equals(task.getStatus())) {
            data.put("errorMessage", task.getErrorMessage());
            data.put("completedAt", task.getCompletedAt());
        } else if ("PROCESSING".equals(task.getStatus())) {
            data.put("message", "AI正在生成图片...");
        }

        return ApiResponse.ok(data);
    }

    public void updateTaskStatus(String taskId,
                                 String status,
                                 String aiImageUrl,
                                 String aiImageKey,
                                 String rawAiImageUrl,
                                 String rawAiImageKey,
                                 String errorMessage,
                                 Integer gridMin,
                                 Integer gridMax,
                                 Integer detectedGridWidth,
                                 Integer detectedGridHeight,
                                 Integer finalGridWidth,
                                 Integer finalGridHeight,
                                 String perfectPixelStatus,
                                 String perfectPixelError) {
        updateTaskStatus(taskId, status, aiImageUrl, errorMessage);

        if (!isFinalStatus(status)) {
            return;
        }

        AiGenerateTask task = taskMapper.findByTaskId(taskId);
        if (task == null || !isFinalStatus(task.getStatus())) {
            return;
        }

        if (StringUtils.hasText(rawAiImageUrl)) {
            task.setRawAiImageUrl(rawAiImageUrl);
        }
        if (StringUtils.hasText(aiImageKey)) {
            task.setAiImageKey(aiImageKey);
        }
        if (StringUtils.hasText(rawAiImageKey)) {
            task.setRawAiImageKey(rawAiImageKey);
        }
        if (gridMin != null) {
            task.setGridMin(gridMin);
        }
        if (gridMax != null) {
            task.setGridMax(gridMax);
        }
        if (detectedGridWidth != null) {
            task.setDetectedGridWidth(detectedGridWidth);
        }
        if (detectedGridHeight != null) {
            task.setDetectedGridHeight(detectedGridHeight);
        }
        if (finalGridWidth != null) {
            task.setFinalGridWidth(finalGridWidth);
        }
        if (finalGridHeight != null) {
            task.setFinalGridHeight(finalGridHeight);
        }
        if (StringUtils.hasText(perfectPixelStatus)) {
            task.setPerfectPixelStatus(perfectPixelStatus);
        }
        if (perfectPixelError != null) {
            task.setPerfectPixelError(perfectPixelError);
        }
        task.setUpdatedAt(new Date());
        taskMapper.updateById(task);

        if ("SUCCESS".equals(status) && StringUtils.hasText(aiImageUrl)) {
            aiHistoryAutoSaveService.processAndSaveHistory(task);
        }
    }

    public void updateTaskStatus(String taskId, String status, String aiImageUrl, String errorMessage) {
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("taskId不能为空");
        }

        AiGenerateTask task = taskMapper.findByTaskId(taskId);
        if (task == null) {
            log.warn("[Callback] task not found: taskId={}", taskId);
            throw new IllegalArgumentException("AI任务不存在: " + taskId);
        }

        validateCallbackStatus(status, aiImageUrl, errorMessage);

        if (isFinalStatus(task.getStatus())) {
            if ("SUCCESS".equals(task.getStatus()) && "SUCCESS".equals(status)) {
                System.out.println("[Callback] SUCCESS重复回调已忽略: " + taskId);
            } else {
                System.out.println("[Callback] 终态任务忽略回调: " + taskId + ", 当前: " + task.getStatus() + ", 回调: " + status);
            }
            return;
        }

        task.setStatus(status);
        if ("PROCESSING".equals(status)) {
            task.setErrorMessage(null);
        } else {
            task.setAiImageUrl(aiImageUrl);
            task.setErrorMessage(errorMessage);
            task.setCompletedAt(new Date());
        }
        task.setUpdatedAt(new Date());
        taskMapper.updateById(task);

        if ("FAILED".equals(status)) {
            refundQuotaOnFailure(task, errorMessage);
        }

        System.out.println("[Callback] 任务状态更新: " + taskId + ", 状态: " + status);
    }

    private void refundQuotaOnFailure(AiGenerateTask task, String reason) {
        if (task.getUserId() == null || task.getUserId() <= 0 || !StringUtils.hasText(task.getTaskId())) {
            return;
        }
        if (aiQuotaLogService.hasLoggedBiz(task.getUserId(), "REFUND", "AI_GENERATE_FAILED", task.getTaskId())) {
            return;
        }

        userService.addAiQuota(task.getUserId(), 1);
        boolean logged = aiQuotaLogService.tryLogChange(
                task.getUserId(),
                "REFUND",
                1,
                "AI_GENERATE_FAILED",
                task.getTaskId(),
                summarizeFailureReason(reason) + "，返还次数"
        );
        if (!logged) {
            userService.addAiQuota(task.getUserId(), -1);
        }
    }

    private String summarizeFailureReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            return "AI生成失败";
        }
        String trimmed = reason.trim();
        return trimmed.length() > 120 ? trimmed.substring(0, 120) : trimmed;
    }

    private AiGenerateMessage buildGenerateMessage(AiGenerateTask task, AiGenerateRequest request) {
        AiMagicStyle magicStyle = StringUtils.hasText(request.getStyle()) && !StringUtils.hasText(request.getPromptTemplate())
                ? aiMagicStyleMapper.findByName(request.getStyle())
                : null;

        String promptTemplate = StringUtils.hasText(request.getPromptTemplate())
                ? request.getPromptTemplate()
                : (magicStyle != null ? magicStyle.getPromptTemplate() : "");
        String negativePromptTemplate = StringUtils.hasText(request.getNegativePromptTemplate())
                ? request.getNegativePromptTemplate()
                : (magicStyle != null ? magicStyle.getNegativePromptTemplate() : "");
        String modelKey = StringUtils.hasText(request.getModelKey())
                ? request.getModelKey()
                : (magicStyle != null && StringUtils.hasText(magicStyle.getModelKey())
                ? magicStyle.getModelKey()
                : aiServiceProperties.getDefaultModelKey());

        AiGenerateMessage message = new AiGenerateMessage();
        message.setTaskId(task.getTaskId());
        message.setImageUrl(task.getImageUrl());
        message.setUserPrompt(task.getPrompt());
        message.setStyle(task.getStyle());
        message.setPromptTemplate(promptTemplate);
        message.setNegativePromptTemplate(negativePromptTemplate);
        message.setModelKey(modelKey);
        message.setSizeMode(task.getSizeMode());
        message.setSizePreset(task.getSizePreset());
        message.setSizePresetName(task.getSizePresetName());
        message.setGridMin(task.getGridMin());
        message.setGridMax(task.getGridMax());
        message.setCandidateGrids(AiSizePresetService.parseCandidateGrids(task.getCandidateGrids()));
        message.setDefaultGrid(task.getDefaultGrid());
        message.setBrand(task.getBrand());
        message.setColorCount(task.getColorCount());
        message.setMirror(task.getMirror());
        message.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(task.getCreatedAt()));
        return message;
    }

    private AiGenerateTask createPendingTask(AiGenerateRequest request, Long userId) {
        String taskId = generateTaskId();

        AiGenerateTask task = new AiGenerateTask();
        task.setTaskId(taskId);
        task.setUserId(userId);
        task.setImageUrl(request.getImageUrl());
        task.setPrompt(request.getPrompt());
        task.setStyle(StringUtils.hasText(request.getStyle()) ? request.getStyle() : "default");
        AiSizePreset preset = aiSizePresetService.resolveForGenerate(request.getSizePreset(), request.getSizeMode());
        String sizeMode = StringUtils.hasText(request.getSizeMode()) ? request.getSizeMode() : preset.getPresetKey();
        task.setSizeMode(sizeMode);
        task.setSizePreset(preset.getPresetKey());
        task.setSizePresetName(preset.getName());
        task.setGridMin(preset.getGridMin());
        task.setGridMax(preset.getGridMax());
        task.setCandidateGrids(preset.getCandidateGrids());
        task.setDefaultGrid(preset.getDefaultGrid());
        task.setBrand(request.getBrand());
        task.setColorCount(request.getColorCount());
        task.setMirror(request.getMirror() != null ? request.getMirror() : false);
        task.setStatus("PENDING");
        task.setCreatedAt(new Date());
        task.setUpdatedAt(new Date());

        taskMapper.insert(task);
        return task;
    }

    private void validateCallbackStatus(String status, String aiImageUrl, String errorMessage) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("status不能为空");
        }
        if (!"PROCESSING".equals(status) && !"SUCCESS".equals(status) && !"FAILED".equals(status)) {
            throw new IllegalArgumentException("不支持的AI任务状态: " + status);
        }
        if ("SUCCESS".equals(status) && !StringUtils.hasText(aiImageUrl)) {
            throw new IllegalArgumentException("SUCCESS回调必须提供aiImageUrl");
        }
        if ("FAILED".equals(status) && !StringUtils.hasText(errorMessage)) {
            throw new IllegalArgumentException("FAILED回调必须提供errorMessage");
        }
    }

    private boolean isFinalStatus(String status) {
        return "SUCCESS".equals(status) || "FAILED".equals(status);
    }

    private String generateTaskId() {
        return "AI" + System.currentTimeMillis() +
               String.format("%06d", new Random().nextInt(1000000));
    }
}
