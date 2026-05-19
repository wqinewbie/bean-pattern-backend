package com.beanpattern.service;

import com.beanpattern.controller.AiTaskController.AiGenerateRequest;
import com.beanpattern.entity.AiGenerateTask;
import com.beanpattern.mapper.AiGenerateTaskMapper;
import com.beanpattern.model.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public String createMockTask(AiGenerateRequest request, Long userId) {
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

        mockGenerateService.mockAiGenerate(taskId);

        System.out.println("[Mock] 任务创建: " + taskId);

        return taskId;
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

        task.setStatus(status);
        task.setAiImageUrl(aiImageUrl);
        task.setErrorMessage(errorMessage);
        task.setCompletedAt(new Date());
        task.setUpdatedAt(new Date());
        taskMapper.updateById(task);

        System.out.println("[Callback] 任务状态更新: " + taskId + ", 状态: " + status);
    }

    private String generateTaskId() {
        return "AI" + System.currentTimeMillis() +
               String.format("%06d", new Random().nextInt(1000000));
    }
}
