package com.beanpattern.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class BannerEntity {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private Long id;
    private String title;
    private String subTitle;
    private String imageUrl;
    private String linkType;
    private String linkValue;
    private String actionType;
    private String actionConfig;
    private String tagText;
    private Integer sortOrder;
    private Integer status;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime startAt;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime endAt;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime createdAt;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubTitle() { return subTitle; }
    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }
    public String getLinkValue() { return linkValue; }
    public void setLinkValue(String linkValue) { this.linkValue = linkValue; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getActionConfig() { return actionConfig; }
    public void setActionConfig(String actionConfig) { this.actionConfig = actionConfig; }
    public String getTagText() { return tagText; }
    public void setTagText(String tagText) { this.tagText = tagText; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
