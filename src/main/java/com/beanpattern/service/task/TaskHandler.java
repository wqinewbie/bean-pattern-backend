package com.beanpattern.service.task;

import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.TaskConfig;

/**
 * 任务中心能力处理器。
 */
public interface TaskHandler {

    /**
     * 当前处理器是否支持该任务配置。
     */
    boolean supports(TaskConfig config);

    /**
     * 构建用户在任务中心看到的任务项。
     */
    TaskCenterItem buildTaskItem(Long userId, TaskConfig config);
}
