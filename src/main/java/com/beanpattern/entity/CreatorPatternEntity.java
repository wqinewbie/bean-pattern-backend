package com.beanpattern.entity;

import java.time.LocalDateTime;

public class CreatorPatternEntity {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String coverUrl;
    private String patternUrl;
    private String gridSize;
    private Integer difficulty;
    private Integer priceCoins;
    private Integer downloadCount;
    private Integer likeCount;
    private Integer status;
    private String rejectReason;
    private String category;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getPatternUrl() { return patternUrl; }
    public void setPatternUrl(String patternUrl) { this.patternUrl = patternUrl; }
    public String getGridSize() { return gridSize; }
    public void setGridSize(String gridSize) { this.gridSize = gridSize; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public Integer getPriceCoins() { return priceCoins; }
    public void setPriceCoins(Integer priceCoins) { this.priceCoins = priceCoins; }
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
