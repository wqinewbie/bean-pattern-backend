package com.beanpattern.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderEntity {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long planId;
    private String planName;
    private BigDecimal amount;
    private String status;
    
    // V6.0 新增字段
    private Long productId;           // VIP产品ID
    private Integer vipLevelPurchased; // 购买的VIP等级
    private Integer vipDays;          // VIP天数
    private String giftItems;         // 赠品JSON字符串

    // 会员系统新增字段
    private String productType;       // 商品类型：vip/card/gift
    private String packageCode;       // 套餐代码
    private LocalDateTime expireAt;   // 订单过期时间（创建后10分钟）
    private String deliverStatus;     // 发货状态：PENDING/SUCCESS/FAILED
    private String deliverError;      // 发货失败原因
    private LocalDateTime paidAt;     // 支付时间
    private String transactionId;     // 微信支付交易号

    private LocalDateTime createdAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getVipLevelPurchased() { return vipLevelPurchased; }
    public void setVipLevelPurchased(Integer vipLevelPurchased) { this.vipLevelPurchased = vipLevelPurchased; }
    public Integer getVipDays() { return vipDays; }
    public void setVipDays(Integer vipDays) { this.vipDays = vipDays; }
    public String getGiftItems() { return giftItems; }
    public void setGiftItems(String giftItems) { this.giftItems = giftItems; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public String getDeliverStatus() { return deliverStatus; }
    public void setDeliverStatus(String deliverStatus) { this.deliverStatus = deliverStatus; }
    public String getDeliverError() { return deliverError; }
    public void setDeliverError(String deliverError) { this.deliverError = deliverError; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
