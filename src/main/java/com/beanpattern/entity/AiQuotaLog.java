package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI次数变动日志表实体：bp_ai_quota_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQuotaLog {
    private Long id;
    private Long userId;               // 用户ID
    private String changeType;         // 变更类型：PURCHASE/GIFT/USE/REFUND
    private Integer changeAmount;      // 变更数量（正数增加，负数减少）
    private Integer balanceBefore;     // 变更前余额
    private Integer balanceAfter;      // 变更后余额
    private String bizType;            // 业务类型：ORDER/ACTIVITY/ADMIN
    private String bizId;              // 业务ID
    private String description;        // 描述
    private LocalDateTime createdAt;   // 创建时间
}
