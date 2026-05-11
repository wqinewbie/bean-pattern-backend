package com.beanpattern.entity;

import java.time.LocalDateTime;

/**
 * 用户表实体：bp_user
 */
public class UserEntity {
    private Long id;
    private String openId;
    private String unionId;
    private String nickName;
    private String avatarUrl;
    private String phone;
    private Integer gender;
    private Integer vipLevel;
    private LocalDateTime vipExpireAt;
    private Integer storageQuota;
    private Integer draftQuota;
    private Integer currentStorage;
    private Integer currentDraft;
    private LocalDateTime aiResetAt;
    private String availableBrands;
    private Integer aiQuota;
    private Integer status;
    private LocalDateTime lastVipNotifyAt;  // 上次会员到期提醒时间
    private LocalDateTime lastAiNotifyAt;   // 上次AI次数不足提醒时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }

    public String getUnionId() { return unionId; }
    public void setUnionId(String unionId) { this.unionId = unionId; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public Integer getVipLevel() { return vipLevel; }
    public void setVipLevel(Integer vipLevel) { this.vipLevel = vipLevel; }

    public LocalDateTime getVipExpireAt() { return vipExpireAt; }
    public void setVipExpireAt(LocalDateTime vipExpireAt) { this.vipExpireAt = vipExpireAt; }

    public Integer getStorageQuota() { return storageQuota; }
    public void setStorageQuota(Integer storageQuota) { this.storageQuota = storageQuota; }

    public Integer getDraftQuota() { return draftQuota; }
    public void setDraftQuota(Integer draftQuota) { this.draftQuota = draftQuota; }

    public Integer getCurrentStorage() { return currentStorage; }
    public void setCurrentStorage(Integer currentStorage) { this.currentStorage = currentStorage; }

    public Integer getCurrentDraft() { return currentDraft; }
    public void setCurrentDraft(Integer currentDraft) { this.currentDraft = currentDraft; }

    public LocalDateTime getAiResetAt() { return aiResetAt; }
    public void setAiResetAt(LocalDateTime aiResetAt) { this.aiResetAt = aiResetAt; }

    public String getAvailableBrands() { return availableBrands; }
    public void setAvailableBrands(String availableBrands) { this.availableBrands = availableBrands; }

    public Integer getAiQuota() { return aiQuota; }
    public void setAiQuota(Integer aiQuota) { this.aiQuota = aiQuota; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastVipNotifyAt() { return lastVipNotifyAt; }
    public void setLastVipNotifyAt(LocalDateTime lastVipNotifyAt) { this.lastVipNotifyAt = lastVipNotifyAt; }

    public LocalDateTime getLastAiNotifyAt() { return lastAiNotifyAt; }
    public void setLastAiNotifyAt(LocalDateTime lastAiNotifyAt) { this.lastAiNotifyAt = lastAiNotifyAt; }
}
