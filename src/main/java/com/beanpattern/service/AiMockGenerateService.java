package com.beanpattern.service;

import com.beanpattern.entity.AiGenerateTask;
import com.beanpattern.mapper.AiGenerateTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
public class AiMockGenerateService {

    private static final String[] TEST_IMAGES = {
        "https://bean-pattern-dev-1417861640.cos.ap-guangzhou.myqcloud.com/44ef16f6-eee2-42fb-8825-566c861aae7c.png"
    };

    private static final long TASK_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

    @Autowired
    private AiGenerateTaskMapper taskMapper;

    @Async
    public void mockAiGenerate(String taskId) {
        try {
            AiGenerateTask task = taskMapper.findByTaskId(taskId);
            if (task == null) return;

            long elapsed = System.currentTimeMillis() - task.getCreatedAt().getTime();
            if (elapsed > TASK_TIMEOUT_MS) {
                task.setStatus("FAILED");
                task.setErrorMessage("任务超时（超过" + (TASK_TIMEOUT_MS / 60000) + "分钟）");
                task.setCompletedAt(new Date());
                task.setUpdatedAt(new Date());
                taskMapper.updateById(task);
                System.err.println("[Mock] 任务超时取消: " + taskId);
                return;
            }

            System.out.println("[Mock] 开始处理任务: " + taskId);
            task.setStatus("PROCESSING");
            task.setUpdatedAt(new Date());
            taskMapper.updateById(task);
            System.out.println("[Mock] 任务状态更新为 PROCESSING: " + taskId);

            Thread.sleep(2000 + new Random().nextInt(1000));

            String testImageUrl = TEST_IMAGES[new Random().nextInt(TEST_IMAGES.length)];

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
            failTask(taskId, "任务被中断");
        } catch (Exception e) {
            System.err.println("[Mock] 任务失败: " + taskId + ", 错误: " + e.getMessage());
            failTask(taskId, e.getMessage() != null ? e.getMessage() : "未知错误");
        }
    }

    private void failTask(String taskId, String errorMessage) {
        AiGenerateTask task = taskMapper.findByTaskId(taskId);
        if (task != null) {
            task.setStatus("FAILED");
            task.setErrorMessage(errorMessage);
            task.setCompletedAt(new Date());
            task.setUpdatedAt(new Date());
            taskMapper.updateById(task);
        }
    }
}
