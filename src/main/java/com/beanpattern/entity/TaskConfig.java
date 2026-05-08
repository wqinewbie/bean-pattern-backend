package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务配置表实体：bp_task_config
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskConfig {
    private Long id;
    private String taskCode;           // 任务代码：daily_checkin/daily_share/invite_friend
    private String taskName;           // 任务名称
    private String taskType;           // 任务类型：DAILY/ONCE/UNLIMITED
    private String description;        // 任务描述
    private String rewardType;         // 奖励类型：AI_QUOTA/VIP_DAYS/COINS
    private Integer rewardValue;       // 奖励值
    private String icon;               // 任务图标URL
    private Integer sortOrder;         // 排序
    private Boolean isActive;          // 是否启用
    private String extraConfig;        // 额外配置（JSON）
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getTargetCount() { return targetCount; }
    public void setTargetCount(Integer targetCount) { this.targetCount = targetCount; }

    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }

    public Integer getRewardValue() { return rewardValue; }
    public void setRewardValue(Integer rewardValue) { this.rewardValue = rewardValue; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
