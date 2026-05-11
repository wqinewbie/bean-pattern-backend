package com.beanpattern.entity;

import java.time.LocalDateTime;

public class GiftPackage {
    private Long id;
    private String packageCode;
    private String name;
    private String description;
    private String itemsJson;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getItemsJson() { return itemsJson; }
    public void setItemsJson(String itemsJson) { this.itemsJson = itemsJson; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
