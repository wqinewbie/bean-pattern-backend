package com.beanpattern.service;

import com.beanpattern.entity.GiftPackage;
import com.beanpattern.mapper.GiftPackageMapper;
import com.beanpattern.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GiftPackageService {

    private final GiftPackageMapper giftPackageMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public GiftPackageService(GiftPackageMapper giftPackageMapper, UserMapper userMapper) {
        this.giftPackageMapper = giftPackageMapper;
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
                int value = readInt(item, "value", readInt(item, "gift_value", 0));
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
                int value = readInt(item, "value", readInt(item, "gift_value", 0));
                if (!StringUtils.hasText(type)) throw new IllegalArgumentException("礼品类型不能为空");
                if (value <= 0) throw new IllegalArgumentException("礼品数量必须大于0");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("礼品明细JSON格式错误: " + e.getMessage());
        }
    }

    private void grantSingle(Long userId, String type, int value) {
        if ("AI_QUOTA".equals(type) || "AI_COUNT".equals(type)) {
            userMapper.addAiQuota(userId, value);
        } else if ("VIP_DAYS".equals(type)) {
            userMapper.addVipDays(userId, value);
        } else if ("MAGIC_COINS".equals(type)) {
            userMapper.addCoins(userId, value);
        } else {
            throw new IllegalArgumentException("不支持的礼品类型: " + type);
        }
    }

    private String readText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asText();
    }

    private int readInt(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asInt();
    }
}
