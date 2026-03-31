package com.beanpattern.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值套餐实体：bp_recharge_plan
 */
public class RechargePlanEntity {
    private Long id;
    private String name;
    private String description;
    private Integer coins;
    private Integer aiQuota;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer isVip;
    private Integer vipDays;
    private String tag;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCoins() { return coins; }
    public void setCoins(Integer coins) { this.coins = coins; }
    public Integer getAiQuota() { return aiQuota; }
    public void setAiQuota(Integer aiQuota) { this.aiQuota = aiQuota; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public Integer getIsVip() { return isVip; }
    public void setIsVip(Integer isVip) { this.isVip = isVip; }
    public Integer getVipDays() { return vipDays; }
    public void setVipDays(Integer vipDays) { this.vipDays = vipDays; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
