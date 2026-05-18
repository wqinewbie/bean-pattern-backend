package com.beanpattern.entity;

import java.time.LocalDateTime;

/**
 * 分享记录表实体：bp_share_record
 */
public class ShareRecord {
    private Long id;
    private Long userId;
    private String shareScene;
    private String targetId;
    private String shareTicket;
    private Integer visitCount;
    private Integer validVisitCount;
    private Integer rewardStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getShareScene() { return shareScene; }
    public void setShareScene(String shareScene) { this.shareScene = shareScene; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getShareTicket() { return shareTicket; }
    public void setShareTicket(String shareTicket) { this.shareTicket = shareTicket; }

    public Integer getVisitCount() { return visitCount; }
    public void setVisitCount(Integer visitCount) { this.visitCount = visitCount; }

    public Integer getValidVisitCount() { return validVisitCount; }
    public void setValidVisitCount(Integer validVisitCount) { this.validVisitCount = validVisitCount; }

    public Integer getRewardStatus() { return rewardStatus; }
    public void setRewardStatus(Integer rewardStatus) { this.rewardStatus = rewardStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
