package com.beanpattern.service;

import com.beanpattern.entity.BpUserGift;
import com.beanpattern.entity.GiftItem;
import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.GiftType;
import com.beanpattern.entity.GiftTypeConfig;
import com.beanpattern.mapper.BpUserGiftMapper;
import com.beanpattern.mapper.GiftPackageMapper;
import com.beanpattern.mapper.GiftTypeConfigMapper;
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
    private final BpUserGiftMapper bpUserGiftMapper;
    private final UserService userService;
    private final AiQuotaLogService aiQuotaLogService;
    private final ObjectMapper objectMapper;

    public GiftPackageService(GiftPackageMapper giftPackageMapper,
                              GiftTypeConfigMapper giftTypeConfigMapper,
                              BpUserGiftMapper bpUserGiftMapper,
                              UserService userService,
                              AiQuotaLogService aiQuotaLogService) {
        this.giftPackageMapper = giftPackageMapper;
        this.giftTypeConfigMapper = giftTypeConfigMapper;
        this.bpUserGiftMapper = bpUserGiftMapper;
        this.userService = userService;
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
            throw new IllegalArgumentException("Gift package code cannot be empty");
        }
        if (!StringUtils.hasText(giftPackage.getName())) {
            throw new IllegalArgumentException("Gift package name cannot be empty");
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
    public BpUserGift grantPackageToUser(Long userId, String packageCode) {
        GiftPackage giftPackage = requireActivePackage(packageCode);
        return createPackageGift(userId, giftPackage, GIFT_PACKAGE_SOURCE_PREFIX + giftPackage.getPackageCode());
    }

    @Transactional
    public BpUserGift grantPackageToUser(Long userId, String packageCode, String source) {
        GiftPackage giftPackage = requireActivePackage(packageCode);
        return createPackageGift(userId, giftPackage, source);
    }

    @Transactional
    public void redeemPackageGift(Long userId, Long userGiftId) {
        BpUserGift gift = bpUserGiftMapper.findById(userGiftId);
        if (gift == null || !userId.equals(gift.getUserId())) {
            throw new IllegalArgumentException("Gift does not exist");
        }
        if (!"UNUSED".equals(gift.getStatus())) {
            throw new IllegalStateException("Gift is already used or unavailable");
        }
        if (!GIFT_PACKAGE_CODE.equals(gift.getGiftType())) {
            throw new IllegalArgumentException("Gift cannot be redeemed as a package");
        }
        if (gift.getExpireAt() != null && gift.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Gift has expired");
        }

        GiftPackage giftPackage = resolveGiftPackage(gift);
        if (giftPackage == null || giftPackage.getStatus() == null || giftPackage.getStatus() != 1) {
            throw new IllegalStateException("Gift package does not exist or is disabled");
        }

        int updated = bpUserGiftMapper.use(userGiftId, null);
        if (updated <= 0) {
            throw new IllegalStateException("Gift redemption failed, please try again later");
        }

        int expireDays = giftPackage.getExpireDays() != null && giftPackage.getExpireDays() > 0
                ? giftPackage.getExpireDays() : DEFAULT_PACKAGE_EXPIRE_DAYS;
        grantItemsJsonToUser(userId, giftPackage.getItemsJson(), expireDays,
                GIFT_PACKAGE_CODE, String.valueOf(gift.getId()), "Redeem gift package: " + giftPackage.getName());
    }

    @Transactional
    public void grantItemsJsonToUser(Long userId, String itemsJson) {
        grantItemsJsonToUser(userId, itemsJson, DEFAULT_PACKAGE_EXPIRE_DAYS, GIFT_PACKAGE_CODE, "", "Grant gift package benefits");
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
                throw new IllegalArgumentException("Gift config must be an array");
            }
            for (JsonNode item : items) {
                String type = readText(item, "type", readText(item, "gift_type", ""));
                double value = readDouble(item, "value", readDouble(item, "gift_value", 0));
                grantSingle(userId, type, value, expireDays, bizType, bizId, description);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid gift config: " + e.getMessage());
        }
    }

    private GiftPackage requireActivePackage(String packageCode) {
        GiftPackage giftPackage = getByCode(packageCode);
        if (giftPackage == null || giftPackage.getStatus() == null || giftPackage.getStatus() != 1) {
            throw new IllegalArgumentException("Gift package does not exist or is disabled");
        }
        return giftPackage;
    }

    private void validateItems(String itemsJson) {
        if (!StringUtils.hasText(itemsJson)) {
            throw new IllegalArgumentException("Gift items cannot be empty");
        }
        try {
            JsonNode items = objectMapper.readTree(itemsJson);
            if (!items.isArray() || items.isEmpty()) {
                throw new IllegalArgumentException("Gift items must be a non-empty array");
            }
            for (JsonNode item : items) {
                String type = readText(item, "type", readText(item, "gift_type", ""));
                double value = readDouble(item, "value", readDouble(item, "gift_value", 0));
                if (!StringUtils.hasText(type)) throw new IllegalArgumentException("Gift type cannot be empty");
                if (value <= 0) throw new IllegalArgumentException("Gift value must be greater than 0");
                GiftTypeConfig typeConfig = giftTypeConfigMapper.findByCode(type);
                if (typeConfig == null) throw new IllegalArgumentException("Gift type is not configured: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid gift items JSON: " + e.getMessage());
        }
    }

    private BpUserGift createPackageGift(Long userId, GiftPackage giftPackage, String source) {
        int expireDays = giftPackage.getExpireDays() != null && giftPackage.getExpireDays() > 0
                ? giftPackage.getExpireDays() : DEFAULT_PACKAGE_EXPIRE_DAYS;
        BpUserGift gift = BpUserGift.builder()
                .userId(userId)
                .giftId(giftPackage.getId())
                .giftType(GIFT_PACKAGE_CODE)
                .giftName(giftPackage.getName())
                .giftValue(1)
                .source(source)
                .status("UNUSED")
                .expireAt(LocalDateTime.now().plusDays(expireDays))
                .build();
        bpUserGiftMapper.insert(gift);
        return gift;
    }

    private GiftPackage resolveGiftPackage(BpUserGift gift) {
        String packageCode = resolvePackageCodeFromSource(gift.getSource());
        if (StringUtils.hasText(packageCode)) {
            GiftPackage giftPackage = getByCode(packageCode);
            if (giftPackage != null) {
                return giftPackage;
            }
        }
        if (gift.getGiftId() != null) {
            return giftPackageMapper.findById(gift.getGiftId());
        }
        return null;
    }

    private String resolvePackageCodeFromSource(String source) {
        if (!StringUtils.hasText(source)) return "";
        return source.startsWith(GIFT_PACKAGE_SOURCE_PREFIX) ? source.substring(GIFT_PACKAGE_SOURCE_PREFIX.length()) : "";
    }

    private void grantSingle(Long userId, String type, double value, int expireDays, String bizType, String bizId, String description) {
        if ("AI_QUOTA".equals(type) || "AI_COUNT".equals(type)) {
            int amount = (int) value;
            userService.addAiQuota(userId, amount);
            aiQuotaLogService.logChange(userId, "GIFT", amount,
                    StringUtils.hasText(bizType) ? bizType : GIFT_PACKAGE_CODE,
                    StringUtils.hasText(bizId) ? bizId : "",
                    StringUtils.hasText(description) ? description : "Redeem gift package for AI quota");
            return;
        }
        if ("VIP_DAYS".equals(type)) {
            userService.addVipDays(userId, (int) value);
            return;
        }

        GiftTypeConfig typeConfig = giftTypeConfigMapper.findByCode(type);
        if (typeConfig == null) {
            throw new IllegalArgumentException("Unsupported gift type: " + type);
        }

        int giftValue = (int) Math.round(value * 10);
        LocalDateTime expires = LocalDateTime.now().plusDays("VIP_TRIAL_CARD".equals(type) ? (int) value : expireDays);
        BpUserGift gift = BpUserGift.builder()
                .userId(userId)
                .giftId(null)
                .giftType(typeConfig.getCode())
                .giftName(typeConfig.getName())
                .giftValue(giftValue)
                .source(StringUtils.hasText(bizType) ? bizType : GIFT_PACKAGE_CODE)
                .sourceId(parseLongOrNull(bizId))
                .status("UNUSED")
                .expireAt(expires)
                .build();
        bpUserGiftMapper.insert(gift);
    }

    private Long parseLongOrNull(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
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
