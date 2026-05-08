package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户活动参与记录表实体：bp_user_activity_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLog {
    private Long id;
    private Long userId;               // 用户ID
    private Long activityId;           // 活动ID
    private String activityCode;       // 活动编码
    private String actionType;         // 操作类型：VIEW/CLAIM/COMPLETE
    private String rewardType;         // 奖励类型：GIFT/COINS/AI_QUOTA/VIP_DAYS
    private Integer rewardValue;       // 奖励值
    private Long giftId;               // 礼品ID
    private LocalDateTime createdAt;   // 创建时间
}
