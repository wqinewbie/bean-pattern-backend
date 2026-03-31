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
            return existing;
        }
        UserEntity user = new UserEntity();
        user.setOpenId(openId);
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
}
