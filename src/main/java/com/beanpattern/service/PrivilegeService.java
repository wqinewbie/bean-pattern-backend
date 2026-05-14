package com.beanpattern.service;

import com.beanpattern.entity.PrivilegeConfig;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.BpBoxMapper;
import com.beanpattern.mapper.BpDraftMapper;
import com.beanpattern.mapper.PrivilegeConfigMapper;
import com.beanpattern.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrivilegeService {

    private final PrivilegeConfigMapper privilegeConfigMapper;
    private final UserMapper userMapper;
    private final BpBoxMapper bpBoxMapper;
    private final BpDraftMapper bpDraftMapper;

    public PrivilegeService(PrivilegeConfigMapper privilegeConfigMapper,
                            UserMapper userMapper,
                            BpBoxMapper bpBoxMapper,
                            BpDraftMapper bpDraftMapper) {
        this.privilegeConfigMapper = privilegeConfigMapper;
        this.userMapper = userMapper;
        this.bpBoxMapper = bpBoxMapper;
        this.bpDraftMapper = bpDraftMapper;
    }

    public record LimitStatus(boolean canAdd, int current, int limit, boolean vip) {}

    public boolean isVip(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            return false;
        }
        return user.getVipExpireAt() != null && user.getVipExpireAt().isAfter(LocalDateTime.now());
    }

    public Map<String, Object> getUserPrivileges(Long userId) {
        ensureUserExists(userId);
        boolean vip = isVip(userId);
        List<PrivilegeConfig> configs = privilegeConfigMapper.listActive();

        Map<String, Object> privileges = new HashMap<>();
        privileges.put("isVip", vip);

        for (PrivilegeConfig config : configs) {
            String value = vip ? config.getVipValue() : config.getFreeValue();
            privileges.put(config.getConfigKey(), convertValue(value, config.getValueType()));
        }

        privileges.put("pattern_box_status", getPatternBoxLimitStatus(userId));
        privileges.put("draft_box_status", getDraftBoxLimitStatus(userId));
        return privileges;
    }

    public boolean checkPatternBoxLimit(Long userId) {
        return getPatternBoxLimitStatus(userId).canAdd();
    }

    public boolean checkDraftBoxLimit(Long userId) {
        return getDraftBoxLimitStatus(userId).canAdd();
    }

    public LimitStatus getPatternBoxLimitStatus(Long userId) {
        ensureUserExists(userId);
        boolean vip = isVip(userId);
        int limit = getNumberPrivilege("pattern_box_limit", vip);
        int current = bpBoxMapper.countByUserId(userId);
        return new LimitStatus(current < limit, current, limit, vip);
    }

    public LimitStatus getDraftBoxLimitStatus(Long userId) {
        ensureUserExists(userId);
        boolean vip = isVip(userId);
        int limit = getNumberPrivilege("draft_box_limit", vip);
        int current = bpDraftMapper.countByUserId(userId);
        return new LimitStatus(current < limit, current, limit, vip);
    }

    public int getHistoryExpireDays(Long userId) {
        ensureUserExists(userId);
        return getNumberPrivilege("history_expire_days", isVip(userId));
    }

    public boolean canControlWatermark(Long userId) {
        ensureUserExists(userId);
        PrivilegeConfig config = requireActiveConfig("watermark_control");
        String value = isVip(userId) ? config.getVipValue() : config.getFreeValue();
        return "true".equalsIgnoreCase(value);
    }

    private void ensureUserExists(Long userId) {
        if (userMapper.findById(userId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    private int getNumberPrivilege(String key, boolean vip) {
        PrivilegeConfig config = requireActiveConfig(key);
        String value = vip ? config.getVipValue() : config.getFreeValue();
        return Integer.parseInt(value);
    }

    private PrivilegeConfig requireActiveConfig(String key) {
        PrivilegeConfig config = privilegeConfigMapper.findByKey(key);
        if (config == null || !Boolean.TRUE.equals(config.getIsActive())) {
            throw new IllegalStateException("权益配置未启用: " + key);
        }
        return config;
    }

    private Object convertValue(String value, String valueType) {
        if (value == null) {
            return null;
        }

        return switch (valueType) {
            case "number" -> Integer.parseInt(value);
            case "boolean" -> Boolean.parseBoolean(value);
            default -> value;
        };
    }
}
