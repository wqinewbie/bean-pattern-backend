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
}
