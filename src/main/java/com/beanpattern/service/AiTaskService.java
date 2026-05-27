package com.beanpattern.service;

import com.beanpattern.controller.AiTaskController.AiGenerateRequest;
import com.beanpattern.config.AiServiceProperties;
import com.beanpattern.entity.AiGenerateTask;
import com.beanpattern.entity.AiMagicStyle;
import com.beanpattern.mapper.AiGenerateTaskMapper;
import com.beanpattern.mapper.AiMagicStyleMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.AiGenerateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AiTaskService {

    @Autowired
    private AiGenerateTaskMapper taskMapper;

    @Autowired
    private AiMockGenerateService mockGenerateService;

    @Autowired
    private AiMagicStyleMapper aiMagicStyleMapper;

    @Autowired
    private AiTaskPublisher aiTaskPublisher;

    @Autowired
    private AiServiceProperties aiServiceProperties;

    public String createTask(AiGenerateRequest request, Long userId) {
        AiGenerateTask task = createPendingTask(request, userId);
        String taskId = task.getTaskId();

        if (aiServiceProperties.isMockEnabled() || !aiServiceProperties.isEnabled()) {
            mockGenerateService.mockAiGenerate(taskId);
            System.out.println("[Mock] 任务创建: " + taskId);
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

    public String createMockTask(AiGenerateRequest request, Long userId) {
        AiGenerateTask task = createPendingTask(request, userId);
        mockGenerateService.mockAiGenerate(task.getTaskId());
        System.out.println("[Mock] 任务创建: " + task.getTaskId());
        return task.getTaskId();
    }

    public ApiResponse<Map<String, Object>> getTaskStatus(String taskId) {
        AiGenerateTask task = taskMapper.findByTaskId(taskId);

        if (task == null) {
            return ApiResponse.fail("任务不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getTaskId());
        data.put("status", task.getStatus());

        // 返回后期处理参数，前端可直接用
        data.put("sizeMode", task.getSizeMode());
        data.put("brand", task.getBrand());
        data.put("colorCount", task.getColorCount());
        data.put("mirror", task.getMirror());

        if ("SUCCESS".equals(task.getStatus())) {
            data.put("aiImageUrl", task.getAiImageUrl());
            data.put("completedAt", task.getCompletedAt());
        } else if ("FAILED".equals(task.getStatus())) {
            data.put("errorMessage", task.getErrorMessage());
            data.put("completedAt", task.getCompletedAt());
        } else if ("PROCESSING".equals(task.getStatus())) {
            data.put("message", "AI正在生成图片...");
        }

        return ApiResponse.ok(data);
    }

    public void updateTaskStatus(String taskId, String status, String aiImageUrl, String errorMessage) {
        AiGenerateTask task = taskMapper.findByTaskId(taskId);
        if (task == null) {
            return;
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

        System.out.println("[Callback] 任务状态更新: " + taskId + ", 状态: " + status);
    }

    private AiGenerateMessage buildGenerateMessage(AiGenerateTask task, AiGenerateRequest request) {
        AiMagicStyle magicStyle = StringUtils.hasText(request.getStyle())
                ? aiMagicStyleMapper.findByName(request.getStyle())
                : null;

        String promptTemplate = magicStyle != null ? magicStyle.getPromptTemplate() : "";
        String modelKey = magicStyle != null && StringUtils.hasText(magicStyle.getModelKey())
                ? magicStyle.getModelKey()
                : aiServiceProperties.getDefaultModelKey();

        AiGenerateMessage message = new AiGenerateMessage();
        message.setTaskId(task.getTaskId());
        message.setImageUrl(task.getImageUrl());
        message.setUserPrompt(task.getPrompt());
        message.setStyle(task.getStyle());
        message.setPromptTemplate(promptTemplate);
        message.setModelKey(modelKey);
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
        task.setStyle(request.getStyle());
        task.setSizeMode(request.getSizeMode());
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
