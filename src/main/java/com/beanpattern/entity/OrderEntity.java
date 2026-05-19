package com.beanpattern.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderEntity {
    private Long id;
    private String orderNo;
    private Long userId;
    private String productType;
    private String packageCode;
    private String midasProductId;
    private String planName;
    private BigDecimal amount;
    private String status;
    private LocalDateTime expireAt;
    private String deliverStatus;
    private String deliverError;
    private LocalDateTime paidAt;
    private Long productId;
    private Integer vipLevelPurchased;
    private Integer vipDays;
    private String giftItems;
    private String transactionId;
    private Long couponId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public String getMidasProductId() { return midasProductId; }
    public void setMidasProductId(String midasProductId) { this.midasProductId = midasProductId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public String getDeliverStatus() { return deliverStatus; }
    public void setDeliverStatus(String deliverStatus) { this.deliverStatus = deliverStatus; }
    public String getDeliverError() { return deliverError; }
    public void setDeliverError(String deliverError) { this.deliverError = deliverError; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getVipLevelPurchased() { return vipLevelPurchased; }
    public void setVipLevelPurchased(Integer vipLevelPurchased) { this.vipLevelPurchased = vipLevelPurchased; }
    public Integer getVipDays() { return vipDays; }
    public void setVipDays(Integer vipDays) { this.vipDays = vipDays; }
    public String getGiftItems() { return giftItems; }
    public void setGiftItems(String giftItems) { this.giftItems = giftItems; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
