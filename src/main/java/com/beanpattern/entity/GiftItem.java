package com.beanpattern.entity;

import java.time.LocalDateTime;

/**
 * 礼品项表实体：bp_gift_item
 */
public class GiftItem {
    private Long id;
    private Long giftTypeId;
    private String giftCode;
    private String name;
    private Integer value;
    private Integer totalQuantity;
    private Integer remainQuantity;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGiftTypeId() { return giftTypeId; }
    public void setGiftTypeId(Long giftTypeId) { this.giftTypeId = giftTypeId; }

    public String getGiftCode() { return giftCode; }
    public void setGiftCode(String giftCode) { this.giftCode = giftCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getRemainQuantity() { return remainQuantity; }
    public void setRemainQuantity(Integer remainQuantity) { this.remainQuantity = remainQuantity; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
