package com.beanpattern.service;

import com.beanpattern.config.AiServiceProperties;
import com.beanpattern.entity.AiGenerateTask;
import com.beanpattern.mapper.AiGenerateTaskMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AiTaskTimeoutService {

    private final AiGenerateTaskMapper taskMapper;
    private final AiTaskService aiTaskService;
    private final UserService userService;
    private final AiQuotaLogService aiQuotaLogService;
    private final AiServiceProperties aiServiceProperties;

    public AiTaskTimeoutService(AiGenerateTaskMapper taskMapper,
                                AiTaskService aiTaskService,
                                UserService userService,
                                AiQuotaLogService aiQuotaLogService,
                                AiServiceProperties aiServiceProperties) {
        this.taskMapper = taskMapper;
        this.aiTaskService = aiTaskService;
        this.userService = userService;
        this.aiQuotaLogService = aiQuotaLogService;
        this.aiServiceProperties = aiServiceProperties;
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void failTimedOutTasks() {
        long timeoutMs = Math.max(1, aiServiceProperties.getTaskTimeoutMinutes()) * 60_000L;
        Date cutoff = new Date(System.currentTimeMillis() - timeoutMs);
        List<AiGenerateTask> tasks = taskMapper.findTimedOutActiveTasks(cutoff, 50);

        for (AiGenerateTask task : tasks) {
            String reason = "AI任务超时（超过" + aiServiceProperties.getTaskTimeoutMinutes() + "分钟）";
            aiTaskService.updateTaskStatus(task.getTaskId(), "FAILED", null, reason);
            refundQuotaOnce(task, reason);
        }
    }

    private void refundQuotaOnce(AiGenerateTask task, String reason) {
        if (aiQuotaLogService.hasLoggedBiz(task.getUserId(), "REFUND", "AI_GENERATE_TIMEOUT", task.getTaskId())) {
            return;
        }
        userService.addAiQuota(task.getUserId(), 1);
        boolean logged = aiQuotaLogService.tryLogChange(
                task.getUserId(),
                "REFUND",
                1,
                "AI_GENERATE_TIMEOUT",
                task.getTaskId(),
                reason + "，返还次数"
        );
        if (!logged) {
            userService.addAiQuota(task.getUserId(), -1);
        }
    }
}
