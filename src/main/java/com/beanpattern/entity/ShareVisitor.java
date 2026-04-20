package com.beanpattern.entity;

import java.time.LocalDateTime;

/**
 * 分享访客表实体：share_visitor
 */
public class ShareVisitor {
    private Long id;
    private Long shareRecordId;
    private Long shareUserId;
    private String visitorOpenid;
    private Integer isNewUser;
    private LocalDateTime visitAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getShareRecordId() { return shareRecordId; }
    public void setShareRecordId(Long shareRecordId) { this.shareRecordId = shareRecordId; }

    public Long getShareUserId() { return shareUserId; }
    public void setShareUserId(Long shareUserId) { this.shareUserId = shareUserId; }

    public String getVisitorOpenid() { return visitorOpenid; }
    public void setVisitorOpenid(String visitorOpenid) { this.visitorOpenid = visitorOpenid; }

    public Integer getIsNewUser() { return isNewUser; }
    public void setIsNewUser(Integer isNewUser) { this.isNewUser = isNewUser; }

    public LocalDateTime getVisitAt() { return visitAt; }
    public void setVisitAt(LocalDateTime visitAt) { this.visitAt = visitAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
