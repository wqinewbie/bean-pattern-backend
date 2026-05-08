package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员数据统计表实体：bp_vip_statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VipStatistics {
    private Long id;
    private LocalDate statDate;        // 统计日期
    private Integer newVipCount;       // 新增会员数
    private Integer renewVipCount;     // 续费会员数
    private Integer expireVipCount;    // 到期会员数
    private Integer totalVipCount;     // 累计会员数
    private Integer activeVipCount;    // 活跃会员数
    private BigDecimal revenue;        // 当日收入
    private Integer cardPurchaseCount; // 次卡购买次数
    private BigDecimal cardRevenue;    // 次卡收入
    private LocalDateTime createdAt;   // 创建时间
}
