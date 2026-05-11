package com.beanpattern.entity;

import java.time.LocalDateTime;

public class GiftTypeConfig {
    private Long id;
    private String code;
    private String name;
    private String giftCategory;
    private String description;
    private String iconUrl;
    private Integer sortOrder;
    private Integer status;
    private String valueType;
    private String targetProductType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGiftCategory() { return giftCategory; }
    public void setGiftCategory(String giftCategory) { this.giftCategory = giftCategory; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getValueType() { return valueType; }
    public void setValueType(String valueType) { this.valueType = valueType; }
    public String getTargetProductType() { return targetProductType; }
    public void setTargetProductType(String targetProductType) { this.targetProductType = targetProductType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
