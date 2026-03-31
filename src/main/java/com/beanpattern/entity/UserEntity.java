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
    private Integer magicCoins;
    private Integer aiQuota;
    private Integer status;
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

    public Integer getMagicCoins() { return magicCoins; }
    public void setMagicCoins(Integer magicCoins) { this.magicCoins = magicCoins; }

    public Integer getAiQuota() { return aiQuota; }
    public void setAiQuota(Integer aiQuota) { this.aiQuota = aiQuota; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
