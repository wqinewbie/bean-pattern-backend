package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户任务记录表实体：bp_user_task_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskLog {
    private Long id;
    private Long userId;               // 用户ID
    private Long taskId;               // 任务ID
    private String taskCode;           // 任务代码
    private String status;             // 状态：PENDING/COMPLETED
    private String rewardType;         // 奖励类型
    private Integer rewardValue;       // 奖励值
    private LocalDate shareDate;       // 分享日期（用于分享任务）
    private LocalDateTime completedAt; // 完成时间
    private LocalDateTime createdAt;   // 创建时间
}
