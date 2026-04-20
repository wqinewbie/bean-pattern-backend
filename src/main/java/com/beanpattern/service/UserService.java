package com.beanpattern.service;

import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务：负责用户创建、查询、资料更新。
 */
@Service
public class UserService {

    public static final String DEFAULT_NICK_NAME = "魔法师小豆";
    public static final String DEFAULT_AVATAR_URL = "https://dummyimage.com/200x200/ffe9c2/8b5e3c.png&text=%E8%B1%86";
    
    // 默认配额（非VIP用户）
    public static final int DEFAULT_STORAGE_QUOTA = 10;
    public static final int DEFAULT_DRAFT_QUOTA = 5;
    public static final int DEFAULT_AI_QUOTA = 5;

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据 openId 获取用户；不存在则创建。
     * 后续所有图片/图纸任务都需要落库并关联到用户。
     */
    @Transactional
    public UserEntity getOrCreateByOpenId(String openId) {
        if (!StringUtils.hasText(openId)) {
            throw new IllegalArgumentException("openId is empty");
        }
        UserEntity existing = userMapper.findByOpenId(openId);
        if (existing != null) {
            userMapper.touch(existing.getId());
            if (!StringUtils.hasText(existing.getNickName()) || !StringUtils.hasText(existing.getAvatarUrl())) {
                String nick = StringUtils.hasText(existing.getNickName()) ? existing.getNickName() : DEFAULT_NICK_NAME;
                String avatar = StringUtils.hasText(existing.getAvatarUrl()) ? existing.getAvatarUrl() : DEFAULT_AVATAR_URL;
                userMapper.updateProfile(existing.getId(), nick, avatar);
            }
            return existing;
        }
        UserEntity user = new UserEntity();
        user.setOpenId(openId);
        user.setNickName(DEFAULT_NICK_NAME);
        user.setAvatarUrl(DEFAULT_AVATAR_URL);
        user.setStorageQuota(DEFAULT_STORAGE_QUOTA);
        user.setDraftQuota(DEFAULT_DRAFT_QUOTA);
        user.setCurrentStorage(0);
        user.setCurrentDraft(0);
        user.setAiQuota(DEFAULT_AI_QUOTA);
        userMapper.insert(user);
        return user;
    }

    /**
     * 更新用户昵称和头像（微信授权后调用）。
     */
    @Transactional
    public void updateProfile(Long id, String nickName, String avatarUrl) {
        if (id == null) return;
        // 只在有值时更新，防止覆盖为空
        if (!StringUtils.hasText(nickName)) return;
        userMapper.updateProfile(id, nickName, avatarUrl);
    }

    /**
     * 绑定手机号。
     */
    @Transactional
    public void bindPhone(Long id, String phone) {
        if (id == null || !StringUtils.hasText(phone)) return;
        userMapper.updatePhone(id, phone);
    }
    
    /**
     * 检查用户存储配额是否已满
     */
    public boolean isStorageQuotaFull(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) return true;
        int quota = user.getStorageQuota() != null ? user.getStorageQuota() : DEFAULT_STORAGE_QUOTA;
        int current = user.getCurrentStorage() != null ? user.getCurrentStorage() : 0;
        return current >= quota;
    }
    
    /**
     * 检查用户草稿配额是否已满
     */
    public boolean isDraftQuotaFull(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) return true;
        int quota = user.getDraftQuota() != null ? user.getDraftQuota() : DEFAULT_DRAFT_QUOTA;
        int current = user.getCurrentDraft() != null ? user.getCurrentDraft() : 0;
        return current >= quota;
    }
    
    /**
     * 检查用户AI配额是否可用
     */
    public boolean hasAiQuota(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) return false;
        int quota = user.getAiQuota() != null ? user.getAiQuota() : DEFAULT_AI_QUOTA;
        return quota > 0;
    }
    
    /**
     * 使用AI配额
     */
    @Transactional
    public boolean useAiQuota(Long userId) {
        if (!hasAiQuota(userId)) {
            return false;
        }
        userMapper.addAiQuota(userId, -1);
        return true;
    }
    
    /**
     * 增加存储使用量
     */
    @Transactional
    public void incrementStorageUsage(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) return;
        int current = user.getCurrentStorage() != null ? user.getCurrentStorage() : 0;
        userMapper.updateCurrentStorage(userId, current + 1);
    }
    
    /**
     * 减少存储使用量
     */
    @Transactional
    public void decrementStorageUsage(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) return;
        int current = user.getCurrentStorage() != null ? user.getCurrentStorage() : 0;
        if (current > 0) {
            userMapper.updateCurrentStorage(userId, current - 1);
        }
    }
    
    /**
     * 增加草稿使用量
     */
    @Transactional
    public void incrementDraftUsage(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) return;
        int current = user.getCurrentDraft() != null ? user.getCurrentDraft() : 0;
        userMapper.updateCurrentDraft(userId, current + 1);
    }
    
    /**
     * 减少草稿使用量
     */
    @Transactional
    public void decrementDraftUsage(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) return;
        int current = user.getCurrentDraft() != null ? user.getCurrentDraft() : 0;
        if (current > 0) {
            userMapper.updateCurrentDraft(userId, current - 1);
        }
    }
    
    /**
     * 获取用户完整信息
     */
    public UserEntity getUserById(Long userId) {
        return userMapper.findById(userId);
    }
}
