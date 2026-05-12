package com.beanpattern.service.task;

import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserTaskProgress;
import com.beanpattern.mapper.UserTaskProgressMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 兼容现有 bp_task_config + user_task_progress 的通用事件型任务。
 */
@Component
public class GenericProgressTaskHandler implements TaskHandler {

    private final UserTaskProgressMapper userTaskProgressMapper;
    private final ObjectMapper objectMapper;

    public GenericProgressTaskHandler(UserTaskProgressMapper userTaskProgressMapper) {
        this.userTaskProgressMapper = userTaskProgressMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(TaskConfig config) {
        String handlerType = readExtraText(config, "handlerType", "");
        return !StringUtils.hasText(handlerType)
                || "GENERIC_PROGRESS".equals(handlerType)
                || "EVENT_TASK".equals(handlerType);
    }

    @Override
    public TaskCenterItem buildTaskItem(Long userId, TaskConfig config) {
        UserTaskProgress progress = findProgress(userId, config.getTaskCode());
        int targetCount = progress != null && progress.getTargetCount() != null
                ? progress.getTargetCount()
                : readExtraInt(config, "targetCount", 1);
        int currentCount = progress != null && progress.getCurrentCount() != null ? progress.getCurrentCount() : 0;
        int status = progress != null && progress.getStatus() != null ? progress.getStatus() : 0;

        return TaskCenterItem.builder()
                .taskId(config.getId())
                .taskCode(config.getTaskCode())
                .taskName(config.getTaskName())
                .taskType(config.getTaskType())
                .description(config.getDescription())
                .rewardType(config.getRewardType())
                .rewardValue(config.getRewardValue())
                .icon(config.getIcon())
                .sortOrder(config.getSortOrder())
                .handlerType(readExtraText(config, "handlerType", "GENERIC_PROGRESS"))
                .bizCategory(readExtraText(config, "bizCategory", "EVENT_TASK"))
                .progressText(currentCount + "/" + targetCount)
                .status(status)
                .currentCount(currentCount)
                .targetCount(targetCount)
                .progressId(progress != null ? progress.getId() : null)
                .done(status == 2)
                .canClaim(status == 1)
                .build();
    }

    private UserTaskProgress findProgress(Long userId, String taskCode) {
        List<UserTaskProgress> progresses = userTaskProgressMapper.findByUserId(userId);
        if (progresses == null) return null;
        return progresses.stream()
                .filter(item -> taskCode.equals(item.getTaskCode()))
                .findFirst()
                .orElse(null);
    }

    private int readExtraInt(TaskConfig config, String field, int defaultValue) {
        JsonNode value = readExtraNode(config, field);
        return value == null || value.isNull() ? defaultValue : value.asInt(defaultValue);
    }

    private String readExtraText(TaskConfig config, String field, String defaultValue) {
        JsonNode value = readExtraNode(config, field);
        return value == null || value.isNull() ? defaultValue : value.asText(defaultValue);
    }

    private JsonNode readExtraNode(TaskConfig config, String field) {
        if (config == null || !StringUtils.hasText(config.getExtraConfig())) return null;
        try {
            JsonNode root = objectMapper.readTree(config.getExtraConfig());
            return root.get(field);
        } catch (Exception e) {
            return null;
        }
    }
}
