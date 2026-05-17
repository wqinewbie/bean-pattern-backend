package com.beanpattern.entity;

import java.time.LocalDateTime;

/**
 * 用户礼品表实体：user_gift
 */
public class UserGift {
    private Long id;
    private Long userId;
    private Long giftItemId;
    private String giftCode;
    private String giftName;
    private String giftCategory;
    private Integer value;
    private String source;

    // 前端使用提示字段（非持久化）
    private String usageMode;
    private String targetTab;
    private Long taskId;
    private Long shareRecordId;
    private Long orderId;
    private LocalDateTime usedAt;
    private LocalDateTime expireAt;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getGiftItemId() { return giftItemId; }
    public void setGiftItemId(Long giftItemId) { this.giftItemId = giftItemId; }

    public String getGiftCode() { return giftCode; }
    public void setGiftCode(String giftCode) { this.giftCode = giftCode; }

    public String getGiftName() { return giftName; }
    public void setGiftName(String giftName) { this.giftName = giftName; }

    public String getGiftCategory() { return giftCategory; }
    public void setGiftCategory(String giftCategory) { this.giftCategory = giftCategory; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getUsageMode() { return usageMode; }
    public void setUsageMode(String usageMode) { this.usageMode = usageMode; }

    public String getTargetTab() { return targetTab; }
    public void setTargetTab(String targetTab) { this.targetTab = targetTab; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getShareRecordId() { return shareRecordId; }
    public void setShareRecordId(Long shareRecordId) { this.shareRecordId = shareRecordId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
