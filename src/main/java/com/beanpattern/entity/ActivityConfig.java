package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动配置表实体：bp_activity_config
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityConfig {
    private Long id;
    private String activityCode;       // 活动编码
    private String title;              // 活动标题
    private String description;        // 活动描述
    private Long bannerId;             // 关联Banner ID
    private String activityType;       // 活动类型：GIFT/DISCOUNT/TASK
    private String giftItems;          // 礼品配置（JSON）
    private String discountConfig;     // 折扣配置（JSON）
    private String taskConfig;         // 任务配置（JSON）
    private String limitType;          // 限制类型：ONCE/DAILY/UNLIMITED
    private Integer totalQuota;        // 总名额
    private Integer remainQuota;       // 剩余名额
    private LocalDateTime startAt;     // 活动开始时间
    private LocalDateTime endAt;       // 活动结束时间
    private Boolean status;            // 状态：0下线 1上线
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
}
