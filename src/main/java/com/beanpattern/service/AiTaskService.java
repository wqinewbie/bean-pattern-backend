package com.beanpattern.service;

import com.beanpattern.controller.AiTaskController.AiGenerateRequest;
import com.beanpattern.entity.AiGenerateTask;
import com.beanpattern.mapper.AiGenerateTaskMapper;
import com.beanpattern.model.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * AI任务服务
 */
@Service
public class AiTaskService {

    @Autowired
    private AiGenerateTaskMapper taskMapper;

    // 测试图片URL
    private static final String[] TEST_IMAGES = {
        "https://bean-pattern-dev-1417861640.cos.ap-guangzhou.myqcloud.com/44ef16f6-eee2-42fb-8825-566c861aae7c.png"
    };

    /**
     * 创建Mock任务（临时版本）
     *
     * @param request 请求参数
     * @param userId 用户ID
     * @return 任务ID
     */
    public String createMockTask(AiGenerateRequest request, Long userId) {
        // 1. 生成任务ID
        String taskId = generateTaskId();

        // 2. 创建任务记录
        AiGenerateTask task = new AiGenerateTask();
        task.setTaskId(taskId);
        task.setUserId(userId);
        task.setPrompt(request.getPrompt());
        task.setStyle(request.getStyle());
        task.setSize(request.getSize());
        task.setBrand(request.getBrand());
        task.setColorCount(request.getColorCount());
        task.setStatus("PENDING");
        task.setCreatedAt(new Date());
        task.setUpdatedAt(new Date());

        // 3. 保存到数据库
        taskMapper.insert(task);

        // 4. 异步Mock处理
        mockAiGenerate(taskId);

        System.out.println("[Mock] 任务创建: " + taskId);

        return taskId;
    }

    /**
     * Mock AI生成（异步执行）
     *
     * @param taskId 任务ID
     */
    @Async
    public void mockAiGenerate(String taskId) {
        try {
            System.out.println("[Mock] 开始处理任务: " + taskId);

            // 先更新状态为 PROCESSING
            AiGenerateTask task = taskMapper.findByTaskId(taskId);
            if (task != null) {
                task.setStatus("PROCESSING");
                task.setUpdatedAt(new Date());
                taskMapper.updateById(task);
                System.out.println("[Mock] 任务状态更新为 PROCESSING: " + taskId);
            }

            // 模拟AI生成耗时（2-3秒）
            Thread.sleep(2000 + new Random().nextInt(1000));

            // 随机选择一个测试图片
            String testImageUrl = TEST_IMAGES[new Random().nextInt(TEST_IMAGES.length)];

            // 更新任务状态为SUCCESS
            task = taskMapper.findByTaskId(taskId);
            if (task != null) {
                task.setStatus("SUCCESS");
                task.setAiImageUrl(testImageUrl);
                task.setCompletedAt(new Date());
                task.setUpdatedAt(new Date());
                taskMapper.updateById(task);

                System.out.println("[Mock] 任务完成: " + taskId + ", URL: " + testImageUrl);
            }

        } catch (InterruptedException e) {
            System.err.println("[Mock] 任务被中断: " + taskId);
            Thread.currentThread().interrupt();

            // 更新任务状态为FAILED
            AiGenerateTask task = taskMapper.findByTaskId(taskId);
            if (task != null) {
                task.setStatus("FAILED");
                task.setErrorMessage("任务被中断");
                task.setCompletedAt(new Date());
                task.setUpdatedAt(new Date());
                taskMapper.updateById(task);
            }
        } catch (Exception e) {
            System.err.println("[Mock] 任务失败: " + taskId + ", 错误: " + e.getMessage());

            // 更新任务状态为FAILED
            AiGenerateTask task = taskMapper.findByTaskId(taskId);
            if (task != null) {
                task.setStatus("FAILED");
                task.setErrorMessage(e.getMessage() != null ? e.getMessage() : "未知错误");
                task.setCompletedAt(new Date());
                task.setUpdatedAt(new Date());
                taskMapper.updateById(task);
            }
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    public ApiResponse<Map<String, Object>> getTaskStatus(String taskId) {
        AiGenerateTask task = taskMapper.findByTaskId(taskId);

        if (task == null) {
            return ApiResponse.fail("任务不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getTaskId());
        data.put("status", task.getStatus());

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

    /**
     * 更新任务状态（回调接口使用）
     *
     * @param taskId 任务ID
     * @param status 状态
     * @param aiImageUrl 图片URL
     * @param errorMessage 错误信息
     */
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

    /**
     * 生成任务ID
     *
     * @return 任务ID
     */
    private String generateTaskId() {
        return "AI" + System.currentTimeMillis() +
               String.format("%06d", new Random().nextInt(1000000));
    }

    // ========== 升级为正式版时，修改这里 ==========

    /**
     * 创建正式任务（正式版本）
     * 升级时将 createMockTask 改为调用此方法
     */
    /*
    public String createTask(AiGenerateRequest request, Long userId) {
        // 1. 生成任务ID
        String taskId = generateTaskId();

        // 2. 创建任务记录
        AiGenerateTask task = new AiGenerateTask();
        task.setTaskId(taskId);
        task.setUserId(userId);
        task.setPrompt(request.getPrompt());
        task.setStyle(request.getStyle());
        task.setSize(request.getSize());
        task.setBrand(request.getBrand());
        task.setColorCount(request.getColorCount());
        task.setStatus("PENDING");
        task.setCreatedAt(new Date());
        task.setUpdatedAt(new Date());

        // 3. 保存到数据库
        taskMapper.insert(task);

        // 4. 发送到消息队列（正式版）
        sendToQueue(taskId, request);

        return taskId;
    }

    private void sendToQueue(String taskId, AiGenerateRequest request) {
        Map<String, Object> message = Map.of(
            "taskId", taskId,
            "prompt", request.getPrompt(),
            "style", request.getStyle(),
            "size", request.getSize(),
            "brand", request.getBrand(),
            "colorCount", request.getColorCount(),
            "timestamp", System.currentTimeMillis()
        );

        rabbitTemplate.convertAndSend(
            "ai.generate.exchange",
            "ai.generate.request",
            message
        );
    }
    */
}
