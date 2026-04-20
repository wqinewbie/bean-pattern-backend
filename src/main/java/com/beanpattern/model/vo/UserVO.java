package com.beanpattern.model.vo;

import com.beanpattern.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 用户视图对象，对外暴露安全的用户字段（不含openId等敏感信息）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVO {

    private Long id;
    private String nickName;
    private String avatarUrl;
    private String phone;
    private Integer vipLevel;
    private String vipExpireAt;
    private Integer magicCoins;
    private Integer aiQuota;
    private Integer status;
    private String createdAt;
    
    // V6.0 新增字段
    private Integer storageQuota;     // 存储配额
    private Integer draftQuota;       // 草稿配额
    private Integer currentStorage;   // 当前存储使用量
    private Integer currentDraft;     // 当前草稿使用量
    private String aiResetAt;         // AI配额重置时间
    private String availableBrands;   // 可用品牌

    public static UserVO from(UserEntity u) {
        UserVO vo = new UserVO();
        vo.id          = u.getId();
        vo.nickName    = u.getNickName()    != null ? u.getNickName()    : "";
        vo.avatarUrl   = u.getAvatarUrl()   != null ? u.getAvatarUrl()   : "";
        vo.phone       = u.getPhone()       != null ? u.getPhone()       : "";
        vo.vipLevel    = u.getVipLevel()    != null ? u.getVipLevel()    : 0;
        vo.vipExpireAt = u.getVipExpireAt() != null ? u.getVipExpireAt().toString() : "";
        vo.magicCoins  = u.getMagicCoins()  != null ? u.getMagicCoins()  : 0;
        vo.aiQuota     = u.getAiQuota()     != null ? u.getAiQuota()     : 0;
        vo.status      = u.getStatus()      != null ? u.getStatus()      : 1;
        vo.createdAt   = u.getCreatedAt()   != null ? u.getCreatedAt().toString() : "";
        
        // V6.0 字段
        vo.storageQuota = u.getStorageQuota() != null ? u.getStorageQuota() : 0;
        vo.draftQuota = u.getDraftQuota() != null ? u.getDraftQuota() : 0;
        vo.currentStorage = u.getCurrentStorage() != null ? u.getCurrentStorage() : 0;
        vo.currentDraft = u.getCurrentDraft() != null ? u.getCurrentDraft() : 0;
        vo.aiResetAt = u.getAiResetAt() != null ? u.getAiResetAt().toString() : "";
        vo.availableBrands = u.getAvailableBrands();
        
        return vo;
    }

    public Long getId()          { return id; }
    public String getNickName()  { return nickName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPhone()     { return phone; }
    public Integer getVipLevel() { return vipLevel; }
    public String getVipExpireAt() { return vipExpireAt; }
    public Integer getMagicCoins() { return magicCoins; }
    public Integer getAiQuota()  { return aiQuota; }
    public Integer getStatus()   { return status; }
    public String getCreatedAt() { return createdAt; }
    public Integer getStorageQuota() { return storageQuota; }
    public Integer getDraftQuota() { return draftQuota; }
    public Integer getCurrentStorage() { return currentStorage; }
    public Integer getCurrentDraft() { return currentDraft; }
    public String getAiResetAt() { return aiResetAt; }
    public String getAvailableBrands() { return availableBrands; }
}
