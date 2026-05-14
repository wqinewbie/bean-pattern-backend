package com.beanpattern.model.vo;

import com.beanpattern.entity.UserEntity;

import java.time.LocalDateTime;

/**
 * 小程序会员状态数据。
 */
public class VipInfoVO {

    private boolean isVip;
    private int vipLevel;
    private String vipExpireAt;
    private int storageQuota;
    private int draftQuota;
    private int aiQuota;
    private String availableBrands;

    public static VipInfoVO from(UserEntity user, int vipLevel) {
        LocalDateTime expireAt = user.getVipExpireAt();
        VipInfoVO vo = new VipInfoVO();
        vo.vipLevel = vipLevel;
        vo.isVip = vipLevel > 0 && expireAt != null && expireAt.isAfter(LocalDateTime.now());
        vo.vipExpireAt = expireAt != null ? expireAt.toString() : "";
        vo.storageQuota = user.getStorageQuota() != null ? user.getStorageQuota() : 10;
        vo.draftQuota = user.getDraftQuota() != null ? user.getDraftQuota() : 20;
        vo.aiQuota = user.getAiQuota() != null ? user.getAiQuota() : 3;
        vo.availableBrands = user.getAvailableBrands() != null ? user.getAvailableBrands() : "[]";
        return vo;
    }

    public boolean isVip() { return isVip; }
    public boolean getIsVip() { return isVip; }
    public void setVip(boolean vip) { isVip = vip; }
    public void setIsVip(boolean vip) { isVip = vip; }
    public int getVipLevel() { return vipLevel; }
    public void setVipLevel(int vipLevel) { this.vipLevel = vipLevel; }
    public String getVipExpireAt() { return vipExpireAt; }
    public void setVipExpireAt(String vipExpireAt) { this.vipExpireAt = vipExpireAt; }
    public int getStorageQuota() { return storageQuota; }
    public void setStorageQuota(int storageQuota) { this.storageQuota = storageQuota; }
    public int getDraftQuota() { return draftQuota; }
    public void setDraftQuota(int draftQuota) { this.draftQuota = draftQuota; }
    public int getAiQuota() { return aiQuota; }
    public void setAiQuota(int aiQuota) { this.aiQuota = aiQuota; }
    public String getAvailableBrands() { return availableBrands; }
    public void setAvailableBrands(String availableBrands) { this.availableBrands = availableBrands; }
}
