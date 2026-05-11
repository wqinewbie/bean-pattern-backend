package com.beanpattern.service;

import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.GiftTypeConfig;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.GiftPackageMapper;
import com.beanpattern.mapper.GiftTypeConfigMapper;
import com.beanpattern.mapper.UserGiftMapper;
import com.beanpattern.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GiftPackageService {

    private final GiftPackageMapper giftPackageMapper;
    private final GiftTypeConfigMapper giftTypeConfigMapper;
    private final UserGiftMapper userGiftMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public GiftPackageService(GiftPackageMapper giftPackageMapper,
                              GiftTypeConfigMapper giftTypeConfigMapper,
                              UserGiftMapper userGiftMapper,
                              UserMapper userMapper) {
        this.giftPackageMapper = giftPackageMapper;
        this.giftTypeConfigMapper = giftTypeConfigMapper;
        this.userGiftMapper = userGiftMapper;
        this.userMapper = userMapper;
        this.objectMapper = new ObjectMapper();
    }

    public List<GiftPackage> listAll() {
        return giftPackageMapper.findAll();
    }

    public List<GiftPackage> listActive() {
        return giftPackageMapper.findAllActive();
    }

    public GiftPackage getByCode(String packageCode) {
        if (!StringUtils.hasText(packageCode)) return null;
        return giftPackageMapper.findByCode(packageCode.trim());
    }

    public GiftPackage save(GiftPackage giftPackage) {
        if (!StringUtils.hasText(giftPackage.getPackageCode())) {
            throw new IllegalArgumentException("礼品包编码不能为空");
        }
        if (!StringUtils.hasText(giftPackage.getName())) {
            throw new IllegalArgumentException("礼品包名称不能为空");
        }
        validateItems(giftPackage.getItemsJson());
        if (giftPackage.getStatus() == null) giftPackage.setStatus(1);
        if (giftPackage.getSortOrder() == null) giftPackage.setSortOrder(0);
        if (giftPackage.getId() == null) {
            giftPackageMapper.insert(giftPackage);
        } else {
            giftPackageMapper.update(giftPackage);
        }
        return giftPackage;
    }

    public void toggleStatus(Long id) {
        giftPackageMapper.toggleStatus(id);
    }

    @Transactional
    public void grantPackageToUser(Long userId, String packageCode) {
        GiftPackage giftPackage = getByCode(packageCode);
        if (giftPackage == null || giftPackage.getStatus() == null || giftPackage.getStatus() != 1) {
            throw new IllegalArgumentException("礼品包不存在或未启用");
        }
        grantItemsJsonToUser(userId, giftPackage.getItemsJson());
    }

    @Transactional
    public void grantItemsJsonToUser(Long userId, String itemsJson) {
        try {
            JsonNode items = objectMapper.readTree(itemsJson);
            if (!items.isArray()) {
                throw new IllegalArgumentException("礼品配置必须是数组");
            }
            for (JsonNode item : items) {
                String type = readText(item, "type", readText(item, "gift_type", ""));
                double value = readDouble(item, "value", readDouble(item, "gift_value", 0));
                grantSingle(userId, type, value);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("礼品配置格式错误: " + e.getMessage());
        }
    }

    private void validateItems(String itemsJson) {
        if (!StringUtils.hasText(itemsJson)) {
            throw new IllegalArgumentException("礼品明细不能为空");
        }
        try {
            JsonNode items = objectMapper.readTree(itemsJson);
            if (!items.isArray() || items.isEmpty()) {
                throw new IllegalArgumentException("礼品明细必须是非空数组");
            }
            for (JsonNode item : items) {
                String type = readText(item, "type", readText(item, "gift_type", ""));
                double value = readDouble(item, "value", readDouble(item, "gift_value", 0));
                if (!StringUtils.hasText(type)) throw new IllegalArgumentException("礼品类型不能为空");
                if (value <= 0) throw new IllegalArgumentException("礼品数量必须大于0");
                GiftTypeConfig typeConfig = giftTypeConfigMapper.findByCode(type);
                if (typeConfig == null) throw new IllegalArgumentException("未配置的礼品类型: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("礼品明细JSON格式错误: " + e.getMessage());
        }
    }

    private void grantSingle(Long userId, String type, double value) {
        if ("AI_QUOTA".equals(type) || "AI_COUNT".equals(type)) {
            userMapper.addAiQuota(userId, (int) value);
            return;
        }
        if ("VIP_DAYS".equals(type)) {
            userMapper.addVipDays(userId, (int) value);
            return;
        }

        GiftTypeConfig typeConfig = giftTypeConfigMapper.findByCode(type);
        if (typeConfig == null) {
            throw new IllegalArgumentException("不支持的礼品类型: " + type);
        }

        UserGift gift = new UserGift();
        gift.setUserId(userId);
        gift.setGiftItemId(null);
        gift.setGiftCode(typeConfig.getCode());
        gift.setGiftName(typeConfig.getName());
        gift.setGiftCategory(typeConfig.getGiftCategory());
        gift.setValue((int) Math.round(value * 10));
        gift.setSource("GIFT_PACKAGE");
        gift.setExpireAt(LocalDateTime.now().plusDays("VIP_TRIAL_CARD".equals(type) ? (int) value : 30));
        gift.setStatus(0);
        userGiftMapper.insert(gift);
    }

    private String readText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asText();
    }

    private double readDouble(JsonNode node, String field, double defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asDouble();
    }
}
