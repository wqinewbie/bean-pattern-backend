package com.beanpattern.entity;

import java.time.LocalDateTime;

/**
 * 邀请关系表。
 */
public class UserInviteRelation {
    private Long id;
    private Long inviterUserId;
    private Long inviteeUserId;
    private String inviteCode;
    private Integer status;
    private String inviteeNickName;
    private String inviteeAvatarUrl;
    private LocalDateTime firstPaidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInviterUserId() { return inviterUserId; }
    public void setInviterUserId(Long inviterUserId) { this.inviterUserId = inviterUserId; }
    public Long getInviteeUserId() { return inviteeUserId; }
    public void setInviteeUserId(Long inviteeUserId) { this.inviteeUserId = inviteeUserId; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getInviteeNickName() { return inviteeNickName; }
    public void setInviteeNickName(String inviteeNickName) { this.inviteeNickName = inviteeNickName; }
    public String getInviteeAvatarUrl() { return inviteeAvatarUrl; }
    public void setInviteeAvatarUrl(String inviteeAvatarUrl) { this.inviteeAvatarUrl = inviteeAvatarUrl; }
    public LocalDateTime getFirstPaidAt() { return firstPaidAt; }
    public void setFirstPaidAt(LocalDateTime firstPaidAt) { this.firstPaidAt = firstPaidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
