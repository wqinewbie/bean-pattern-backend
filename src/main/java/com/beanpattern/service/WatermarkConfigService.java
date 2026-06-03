package com.beanpattern.service;

import com.beanpattern.entity.WatermarkConfig;
import com.beanpattern.entity.UserWatermarkConfig;
import com.beanpattern.mapper.WatermarkConfigMapper;
import com.beanpattern.mapper.UserWatermarkConfigMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WatermarkConfigService {

    private final WatermarkConfigMapper mapper;
    private final UserWatermarkConfigMapper userMapper;
    private final UserService userService;
    private final VipService vipService;

    public WatermarkConfigService(WatermarkConfigMapper mapper,
                                  UserWatermarkConfigMapper userMapper,
                                  UserService userService,
                                  VipService vipService) {
        this.mapper = mapper;
        this.userMapper = userMapper;
        this.userService = userService;
        this.vipService = vipService;
    }

    /**
     * 获取全局水印配置
     */
    public WatermarkConfig getConfig() {
        WatermarkConfig config = mapper.getConfig();
        if (config == null) {
            config = getDefaultConfig();
        }
        return config;
    }

    /**
     * 保存全局水印配置
     */
    public WatermarkConfig save(WatermarkConfig config) {
        normalize(config);
        WatermarkConfig existing = mapper.getConfig();
        if (existing != null) {
            config.setId(existing.getId());
            mapper.update(config);
        } else {
            mapper.insert(config);
        }
        return config;
    }

    /**
     * 获取用户水印配置（小程序端调用）
     */
    public Map<String, Object> getUserConfig(Long userId) {
        // 1. 获取全局配置
        WatermarkConfig globalConfig = getConfig();
        
        // 2. 检查VIP状态
        boolean isVip = vipService.isVip(userId);

        // 3. 获取用户个人配置
        UserWatermarkConfig userConfig = userMapper.getByUserId(userId);
        
        // 4. 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("appName", globalConfig.getAppName());
        result.put("isVip", isVip);
        result.put("canCustomize", isVip);
        
        // 5. 水印配置
        Map<String, Object> watermark = new HashMap<>();
        
        if (isVip && userConfig != null) {
            // VIP用户使用个人配置
            watermark.put("enabled", userConfig.getEnabled() == 1);
            watermark.put("text", userConfig.getCustomText() != null 
                ? userConfig.getCustomText() 
                : globalConfig.getDefaultText());
        } else {
            // 普通用户强制使用默认配置
            watermark.put("enabled", true);
            watermark.put("text", globalConfig.getDefaultText());
        }
        
        // 样式配置（所有用户统一）
        watermark.put("fontSize", globalConfig.getFontSize());
        watermark.put("color", globalConfig.getColor());
        watermark.put("angle", globalConfig.getAngle());
        watermark.put("spacingXRatio", globalConfig.getSpacingXRatio());
        watermark.put("spacingYRatio", globalConfig.getSpacingYRatio());
        watermark.put("opacity", globalConfig.getOpacity());
        
        result.put("watermark", watermark);
        
        return result;
    }

    /**
     * 保存用户水印配置（VIP专属）
     */
    public void saveUserConfig(Long userId, Integer enabled, String customText) {
        // 检查VIP权限
        boolean isVip = vipService.isVip(userId);

        if (!isVip) {
            throw new IllegalArgumentException("仅VIP用户可以自定义水印");
        }
        
        String normalizedText = customText == null ? null : customText.trim();
        if (normalizedText != null && normalizedText.length() > 128) {
            throw new IllegalArgumentException("水印文字不能超过128个字符");
        }

        UserWatermarkConfig config = userMapper.getByUserId(userId);
        if (config == null) {
            config = new UserWatermarkConfig();
            config.setUserId(userId);
            config.setEnabled(enabled != null ? enabled : 1);
            config.setCustomText(normalizedText);
            userMapper.insert(config);
        } else {
            config.setEnabled(enabled != null ? enabled : config.getEnabled());
            config.setCustomText(normalizedText);
            userMapper.updateByUserId(config);
        }
    }

    /**
     * 获取默认配置
     */
    public WatermarkConfig getDefaultConfig() {
        WatermarkConfig config = new WatermarkConfig();
        config.setAppName("拼豆魔法屋");
        config.setDefaultText("拼豆魔法屋出品");
        config.setFontSize(24);
        config.setColor("rgba(100,100,100,0.25)");
        config.setAngle(-30);
        config.setSpacingXRatio(0.22);
        config.setSpacingYRatio(0.18);
        config.setOpacity(0.25);
        return config;
    }

    private void normalize(WatermarkConfig config) {
        WatermarkConfig defaults = getDefaultConfig();
        if (config.getAppName() == null || config.getAppName().isBlank()) {
            config.setAppName(defaults.getAppName());
        }
        if (config.getDefaultText() == null || config.getDefaultText().isBlank()) {
            config.setDefaultText(defaults.getDefaultText());
        }
        if (config.getFontSize() == null || config.getFontSize() < 10) {
            config.setFontSize(defaults.getFontSize());
        }
        if (config.getColor() == null || config.getColor().isBlank()) {
            config.setColor(defaults.getColor());
        }
        if (config.getAngle() == null) {
            config.setAngle(defaults.getAngle());
        }
        if (config.getSpacingXRatio() == null || config.getSpacingXRatio() <= 0) {
            config.setSpacingXRatio(defaults.getSpacingXRatio());
        }
        if (config.getSpacingYRatio() == null || config.getSpacingYRatio() <= 0) {
            config.setSpacingYRatio(defaults.getSpacingYRatio());
        }
        if (config.getOpacity() == null || config.getOpacity() < 0 || config.getOpacity() > 1) {
            config.setOpacity(defaults.getOpacity());
        }
    }
}
