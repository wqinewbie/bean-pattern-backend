package com.beanpattern.entity;

import java.time.LocalDateTime;

/**
 * 用户VIP记录表实体：bp_user_vip_record
 */
public class UserVipRecord {
    private Long id;
    private Long userId;
    private Long productId;
    private String productCode;
    private Integer vipLevel;
    private Long orderId;
    private String orderNo;
    private LocalDateTime startAt;
    private LocalDateTime expireAt;
    private Integer aiUsedCount;
    private LocalDateTime aiResetAt;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public Integer getVipLevel() { return vipLevel; }
    public void setVipLevel(Integer vipLevel) { this.vipLevel = vipLevel; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }

    public Integer getAiUsedCount() { return aiUsedCount; }
    public void setAiUsedCount(Integer aiUsedCount) { this.aiUsedCount = aiUsedCount; }

    public LocalDateTime getAiResetAt() { return aiResetAt; }
    public void setAiResetAt(LocalDateTime aiResetAt) { this.aiResetAt = aiResetAt; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
