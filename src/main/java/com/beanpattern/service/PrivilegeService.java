package com.beanpattern.service;

import com.beanpattern.entity.PrivilegeConfig;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.PrivilegeConfigMapper;
import com.beanpattern.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权益校验服务
 */
@Service
public class PrivilegeService {

    private final PrivilegeConfigMapper privilegeConfigMapper;
    private final UserMapper userMapper;

    public PrivilegeService(PrivilegeConfigMapper privilegeConfigMapper, UserMapper userMapper) {
        this.privilegeConfigMapper = privilegeConfigMapper;
        this.userMapper = userMapper;
    }

    /**
     * 检查用户是否是会员
     */
    public boolean isVip(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            return false;
        }
        return user.getVipExpireAt() != null && user.getVipExpireAt().isAfter(LocalDateTime.now());
    }

    /**
     * 获取用户的权益值
     */
    public Map<String, Object> getUserPrivileges(Long userId) {
        boolean isVip = isVip(userId);
        List<PrivilegeConfig> configs = privilegeConfigMapper.listActive();

        Map<String, Object> privileges = new HashMap<>();
        privileges.put("isVip", isVip);

        for (PrivilegeConfig config : configs) {
            String value = isVip ? config.getVipValue() : config.getFreeValue();

            // 根据值类型转换
            Object convertedValue = convertValue(value, config.getValueType());
            privileges.put(config.getConfigKey(), convertedValue);
        }

        return privileges;
    }

    /**
     * 检查图纸箱容量是否已满
     */
    public boolean checkPatternBoxLimit(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 获取图纸箱容量限制
        PrivilegeConfig config = privilegeConfigMapper.findByKey("pattern_box_limit");
        if (config == null) {
            return true; // 如果没有配置，默认允许
        }

        boolean isVip = isVip(userId);
        int limit = Integer.parseInt(isVip ? config.getVipValue() : config.getFreeValue());
        int currentStorage = user.getCurrentStorage() != null ? user.getCurrentStorage() : 0;

        return currentStorage < limit;
    }

    /**
     * 检查草稿箱容量是否已满
     */
    public boolean checkDraftBoxLimit(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 获取草稿箱容量限制
        PrivilegeConfig config = privilegeConfigMapper.findByKey("draft_box_limit");
        if (config == null) {
            return true; // 如果没有配置，默认允许
        }

        boolean isVip = isVip(userId);
        int limit = Integer.parseInt(isVip ? config.getVipValue() : config.getFreeValue());
        int currentDraft = user.getCurrentDraft() != null ? user.getCurrentDraft() : 0;

        return currentDraft < limit;
    }

    /**
     * 获取时光机记录保留天数
     */
    public int getHistoryExpireDays(Long userId) {
        PrivilegeConfig config = privilegeConfigMapper.findByKey("history_expire_days");
        if (config == null) {
            return 7; // 默认7天
        }

        boolean isVip = isVip(userId);
        return Integer.parseInt(isVip ? config.getVipValue() : config.getFreeValue());
    }

    /**
     * 检查是否可以控制水印
     */
    public boolean canControlWatermark(Long userId) {
        PrivilegeConfig config = privilegeConfigMapper.findByKey("watermark_control");
        if (config == null) {
            return false;
        }

        boolean isVip = isVip(userId);
        String value = isVip ? config.getVipValue() : config.getFreeValue();
        return "true".equalsIgnoreCase(value);
    }

    /**
     * 转换值类型
     */
    private Object convertValue(String value, String valueType) {
        if (value == null) {
            return null;
        }

        switch (valueType) {
            case "number":
                return Integer.parseInt(value);
            case "boolean":
                return Boolean.parseBoolean(value);
            case "string":
            default:
                return value;
        }
    }
}
