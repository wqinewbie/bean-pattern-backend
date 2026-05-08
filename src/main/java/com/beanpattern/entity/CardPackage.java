package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 次卡套餐配置表实体：bp_card_package
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardPackage {
    private Long id;
    private String packageCode;        // 套餐代码：c10/c30/c100
    private String packageName;        // 套餐名称
    private Integer aiQuota;           // AI次数
    private BigDecimal price;          // 价格
    private BigDecimal originalPrice;  // 原价
    private BigDecimal vipPrice;       // 会员价
    private String tag;                // 标签：热销/超值
    private Integer sortOrder;         // 排序
    private Boolean isActive;          // 是否启用
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
}
