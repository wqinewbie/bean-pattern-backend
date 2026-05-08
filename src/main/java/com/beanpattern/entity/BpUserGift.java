package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户礼品表实体：bp_user_gift
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpUserGift {
    private Long id;
    private Long userId;               // 用户ID
    private Long giftId;               // 礼品ID
    private String giftType;           // 礼品类型：AI_QUOTA/VIP_DAYS/COUPON/PHYSICAL
    private String giftName;           // 礼品名称
    private Integer giftValue;         // 礼品值
    private String giftConfig;         // 礼品配置（JSON）
    private String source;             // 来源：activity/task/admin
    private Long sourceId;             // 来源ID
    private String status;             // 状态：UNUSED/USED/EXPIRED
    private LocalDateTime expireAt;    // 过期时间
    private LocalDateTime usedAt;      // 使用时间
    private String orderNo;            // 关联订单号
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
}
