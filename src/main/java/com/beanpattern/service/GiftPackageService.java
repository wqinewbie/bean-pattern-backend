package com.beanpattern.service;

import com.beanpattern.entity.BpUserGift;
import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.GiftTypeConfig;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.BpUserGiftMapper;
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

    private static final String GIFT_PACKAGE_CODE = "GIFT_PACKAGE";
    private static final String GIFT_PACKAGE_SOURCE_PREFIX = "GIFT_PACKAGE:";
    static final int DEFAULT_PACKAGE_EXPIRE_DAYS = 30;

    private final GiftPackageMapper giftPackageMapper;
    private final GiftTypeConfigMapper giftTypeConfigMapper;
    private final UserGiftMapper userGiftMapper;
    private final BpUserGiftMapper bpUserGiftMapper;
    private final UserMapper userMapper;
    private final AiQuotaLogService aiQuotaLogService;
    private final ObjectMapper objectMapper;

    public GiftPackageService(GiftPackageMapper giftPackageMapper,
                              GiftTypeConfigMapper giftTypeConfigMapper,
                              UserGiftMapper userGiftMapper,
                              BpUserGiftMapper bpUserGiftMapper,
                              UserMapper userMapper,
                              AiQuotaLogService aiQuotaLogService) {
        this.giftPackageMapper = giftPackageMapper;
        this.giftTypeConfigMapper = giftTypeConfigMapper;
        this.userGiftMapper = userGiftMapper;
        this.bpUserGiftMapper = bpUserGiftMapper;
        this.userMapper = userMapper;
        this.aiQuotaLogService = aiQuotaLogService;
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
    public UserGift grantPackageToUser(Long userId, String packageCode) {
        GiftPackage giftPackage = getByCode(packageCode);
        if (giftPackage == null || giftPackage.getStatus() == null || giftPackage.getStatus() != 1) {
            throw new IllegalArgumentException("礼品包不存在或未启用");
        }
        return createPackageGift(userId, giftPackage, "GIFT_PACKAGE:" + giftPackage.getPackageCode());
    }

    @Transactional
    public UserGift grantPackageToUser(Long userId, String packageCode, String source) {
        GiftPackage giftPackage = getByCode(packageCode);
        if (giftPackage == null || giftPackage.getStatus() == null || giftPackage.getStatus() != 1) {
            throw new IllegalArgumentException("礼品包不存在或未启用");
        }
        return createPackageGift(userId, giftPackage, source);
    }

    @Transactional
    public void redeemPackageGift(Long userId, Long userGiftId) {
        // 先查新表 bp_user_gift
        BpUserGift newGift = bpUserGiftMapper.findById(userGiftId);
        if (newGift != null && userId.equals(newGift.getUserId())) {
            if (!"UNUSED".equals(newGift.getStatus())) {
                throw new IllegalStateException("礼品已使用或不可用");
            }
            if (!GIFT_PACKAGE_CODE.equals(newGift.getGiftType())) {
                throw new IllegalArgumentException("该礼品不支持立即兑换");
            }
            if (newGift.getExpireAt() != null && newGift.getExpireAt().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("礼品已过期");
            }
            String pkgCode = resolvePackageCodeFromSource(newGift.getSource());
            GiftPackage gp = getByCode(pkgCode);
            if (gp == null || gp.getStatus() == null || gp.getStatus() != 1) {
                throw new IllegalStateException("礼品包不存在或未启用");
            }
            int updated = bpUserGiftMapper.use(userGiftId, null);
            if (updated <= 0) throw new IllegalStateException("礼品兑换失败，请稍后重试");
            userGiftMapper.use(userGiftId); // 同步旧表
            int expireDays = gp.getExpireDays() != null && gp.getExpireDays() > 0 ? gp.getExpireDays() : DEFAULT_PACKAGE_EXPIRE_DAYS;
            grantItemsJsonToUser(userId, gp.getItemsJson(), expireDays, "GIFT_PACKAGE", String.valueOf(userGiftId), "兑换礼品包：" + gp.getName());
            return;
        }

        // 回退到旧表
        UserGift gift = userGiftMapper.findById(userGiftId);
        if (gift == null || !userId.equals(gift.getUserId())) {
            throw new IllegalArgumentException("礼品不存在");
        }
        if (gift.getStatus() == null || gift.getStatus() != 0) {
            throw new IllegalStateException("礼品已使用或不可用");
        }
        if (!GIFT_PACKAGE_CODE.equals(gift.getGiftCode())) {
            throw new IllegalArgumentException("该礼品不支持立即兑换");
        }
        if (gift.getExpireAt() != null && gift.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("礼品已过期");
        }

        String packageCode = resolvePackageCode(gift);
        GiftPackage giftPackage = getByCode(packageCode);
        if (giftPackage == null || giftPackage.getStatus() == null || giftPackage.getStatus() != 1) {
            throw new IllegalStateException("礼品包不存在或未启用");
        }

        int updated = userGiftMapper.use(gift.getId());
        if (updated <= 0) {
            throw new IllegalStateException("礼品兑换失败，请稍后重试");
        }

        int expireDays = giftPackage.getExpireDays() != null && giftPackage.getExpireDays() > 0
                ? giftPackage.getExpireDays() : DEFAULT_PACKAGE_EXPIRE_DAYS;
        grantItemsJsonToUser(userId, giftPackage.getItemsJson(), expireDays, "GIFT_PACKAGE", String.valueOf(gift.getId()), "兑换礼品包：" + giftPackage.getName());
    }

    @Transactional
    public void grantItemsJsonToUser(Long userId, String itemsJson) {
        grantItemsJsonToUser(userId, itemsJson, DEFAULT_PACKAGE_EXPIRE_DAYS, "GIFT_PACKAGE", "", "发放礼品包权益");
    }

    @Transactional
    public void grantItemsJsonToUser(Long userId, String itemsJson, String bizType, String bizId, String description) {
        grantItemsJsonToUser(userId, itemsJson, DEFAULT_PACKAGE_EXPIRE_DAYS, bizType, bizId, description);
    }

    @Transactional
    public void grantItemsJsonToUser(Long userId, String itemsJson, int expireDays, String bizType, String bizId, String description) {
        try {
            JsonNode items = objectMapper.readTree(itemsJson);
            if (!items.isArray()) {
                throw new IllegalArgumentException("礼品配置必须是数组");
            }
            for (JsonNode item : items) {
                String type = readText(item, "type", readText(item, "gift_type", ""));
                double value = readDouble(item, "value", readDouble(item, "gift_value", 0));
                grantSingle(userId, type, value, expireDays, bizType, bizId, description);
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

    private UserGift createPackageGift(Long userId, GiftPackage giftPackage, String source) {
        int expireDays = giftPackage.getExpireDays() != null && giftPackage.getExpireDays() > 0
                ? giftPackage.getExpireDays() : DEFAULT_PACKAGE_EXPIRE_DAYS;
        UserGift gift = new UserGift();
        gift.setUserId(userId);
        gift.setGiftItemId(giftPackage.getId());
        gift.setGiftCode(GIFT_PACKAGE_CODE);
        gift.setGiftName(giftPackage.getName());
        gift.setGiftCategory("COUPON");
        gift.setValue(1);
        gift.setSource(source);
        gift.setExpireAt(LocalDateTime.now().plusDays(expireDays));
        gift.setStatus(0);
        userGiftMapper.insert(gift);

        // 同步写入新表 bp_user_gift
        BpUserGift newGift = BpUserGift.builder()
                .userId(userId)
                .giftId(giftPackage.getId())
                .giftType(GIFT_PACKAGE_CODE)
                .giftName(giftPackage.getName())
                .giftValue(1)
                .source(source)
                .status("UNUSED")
                .expireAt(LocalDateTime.now().plusDays(expireDays))
                .build();
        bpUserGiftMapper.insert(newGift);

        return gift;
    }

    private String resolvePackageCodeFromSource(String source) {
        if (!StringUtils.hasText(source)) return "";
        return source.startsWith(GIFT_PACKAGE_SOURCE_PREFIX) ? source.substring(GIFT_PACKAGE_SOURCE_PREFIX.length()) : "";
    }

    private String resolvePackageCode(UserGift gift) {
        String packageCode = extractPackageCode(gift.getSource());
        if (StringUtils.hasText(packageCode)) {
            return packageCode;
        }

        if (gift.getGiftItemId() != null) {
            GiftPackage giftPackage = giftPackageMapper.findById(gift.getGiftItemId());
            if (giftPackage != null && StringUtils.hasText(giftPackage.getPackageCode())) {
                return giftPackage.getPackageCode();
            }
        }

        return "";
    }

    private String extractPackageCode(String source) {
        if (!StringUtils.hasText(source)) {
            return "";
        }
        return source.startsWith(GIFT_PACKAGE_SOURCE_PREFIX)
                ? source.substring(GIFT_PACKAGE_SOURCE_PREFIX.length())
                : "";
    }

    private void grantSingle(Long userId, String type, double value, int expireDays, String bizType, String bizId, String description) {
        if ("AI_QUOTA".equals(type) || "AI_COUNT".equals(type)) {
            int amount = (int) value;
            userMapper.addAiQuota(userId, amount);
            aiQuotaLogService.logChange(userId, "GIFT", amount,
                    StringUtils.hasText(bizType) ? bizType : "GIFT_PACKAGE",
                    StringUtils.hasText(bizId) ? bizId : "",
                    StringUtils.hasText(description) ? description : "兑换礼品包获得AI次数");
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
        gift.setExpireAt(LocalDateTime.now().plusDays("VIP_TRIAL_CARD".equals(type) ? (int) value : expireDays));
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
