package com.beanpattern.service;

import com.beanpattern.entity.BpUserGift;
import com.beanpattern.entity.GiftItem;
import com.beanpattern.entity.GiftType;
import com.beanpattern.mapper.BpUserGiftMapper;
import com.beanpattern.mapper.GiftItemMapper;
import com.beanpattern.mapper.GiftTypeConfigMapper;
import com.beanpattern.mapper.GiftTypeMapper;
import com.beanpattern.model.vo.GiftVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GiftService {

    private final GiftTypeMapper giftTypeMapper;
    private final GiftTypeConfigMapper giftTypeConfigMapper;
    private final GiftItemMapper giftItemMapper;
    private final BpUserGiftMapper bpUserGiftMapper;
    private final GiftPackageService giftPackageService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final AiQuotaLogService aiQuotaLogService;

    public GiftService(GiftTypeMapper giftTypeMapper,
                       GiftTypeConfigMapper giftTypeConfigMapper,
                       GiftItemMapper giftItemMapper,
                       BpUserGiftMapper bpUserGiftMapper,
                       GiftPackageService giftPackageService,
                       NotificationService notificationService,
                       UserService userService,
                       AiQuotaLogService aiQuotaLogService) {
        this.giftTypeMapper = giftTypeMapper;
        this.giftTypeConfigMapper = giftTypeConfigMapper;
        this.giftItemMapper = giftItemMapper;
        this.bpUserGiftMapper = bpUserGiftMapper;
        this.giftPackageService = giftPackageService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.aiQuotaLogService = aiQuotaLogService;
    }

    public List<GiftType> getAllActiveTypes() {
        return giftTypeMapper.findAllActive();
    }

    public List<GiftType> getTypesByCategory(String category) {
        return giftTypeMapper.findByCategory(category);
    }

    public List<GiftItem> getAvailableGifts() {
        return giftItemMapper.findAllAvailable(LocalDateTime.now());
    }

    public List<GiftItem> getAvailableGiftsByTypeId(Long giftTypeId) {
        return giftItemMapper.findAvailableByTypeId(giftTypeId, LocalDateTime.now());
    }

    public List<BpUserGift> getUserGifts(Long userId) {
        return bpUserGiftMapper.findByUserId(userId);
    }

    public List<BpUserGift> getAvailableUserGifts(Long userId) {
        return bpUserGiftMapper.findByUserIdAndStatus(userId, "UNUSED");
    }

    public List<GiftVO> getAvailableCoupons(Long userId, String productType) {
        List<GiftVO> result = new ArrayList<>();
        List<BpUserGift> gifts = bpUserGiftMapper.findByUserIdAndStatus(userId, "UNUSED");
        gifts.stream()
                .filter(gift -> gift.getExpireAt() == null || gift.getExpireAt().isAfter(LocalDateTime.now()))
                .map(GiftVO::from)
                .filter(vo -> "COUPON".equals(vo.getGiftCategory()))
                .filter(vo -> matchesCouponProductType(vo.getGiftCode(), productType))
                .forEach(result::add);
        return result;
    }

    private boolean matchesCouponProductType(String giftCode, String productType) {
        if (giftCode == null) return false;
        if ("vip".equals(productType)) {
            return "VIP_COUPON".equals(giftCode);
        } else if ("card".equals(productType)) {
            return "CARD_COUPON".equals(giftCode) || "VIP_CARD_COUPON".equals(giftCode);
        }
        return false;
    }

    @Transactional
    public boolean useGift(Long userId, Long giftId) {
        return useGift(userId, giftId, false);
    }

    @Transactional
    public boolean useGift(Long userId, Long giftId, boolean redeemNow) {
        BpUserGift gift = bpUserGiftMapper.findById(giftId);
        if (gift == null || !gift.getUserId().equals(userId) || !"UNUSED".equals(gift.getStatus())) return false;
        if (gift.getExpireAt() != null && gift.getExpireAt().isBefore(LocalDateTime.now())) return false;
        if (redeemNow && "GIFT_PACKAGE".equals(gift.getGiftType())) {
            giftPackageService.redeemPackageGift(userId, giftId);
            return true;
        }
        int updated = bpUserGiftMapper.use(giftId, null);
        if (updated <= 0) return false;
        grantDirectGiftBenefit(userId, gift);
        return true;
    }

    public GiftItem getGiftItemById(Long giftItemId) {
        return giftItemMapper.findById(giftItemId);
    }

    @Transactional
    public BpUserGift grantGift(Long userId, Long giftItemId, String source,
                                Long taskId, Long shareRecordId, Long orderId) {
        GiftItem item = giftItemMapper.findById(giftItemId);
        if (item == null) {
            throw new IllegalArgumentException("Gift item does not exist: " + giftItemId);
        }

        if (item.getTotalQuantity() > 0) {
            int updated = giftItemMapper.decrementQuantity(giftItemId);
            if (updated == 0) {
                throw new IllegalStateException("Gift stock is insufficient");
            }
        }

        LocalDateTime expireAt = item.getEndAt() != null ? item.getEndAt() : LocalDateTime.now().plusDays(30);
        BpUserGift gift = BpUserGift.builder()
                .userId(userId)
                .giftId(giftItemId)
                .giftType(item.getGiftCode())
                .giftName(item.getName())
                .giftValue(item.getValue())
                .source(source)
                .sourceId(taskId != null ? taskId : shareRecordId != null ? shareRecordId : orderId)
                .status("UNUSED")
                .expireAt(expireAt)
                .build();
        bpUserGiftMapper.insert(gift);

        try {
            String giftName = gift.getGiftName() != null ? gift.getGiftName() : "Gift";
            String desc = "You received " + giftName + "; check it in My Gifts.";
            notificationService.createGiftNotification(userId, giftName, desc, gift.getId());
        } catch (Exception ignored) {
        }

        return gift;
    }

    private void grantDirectGiftBenefit(Long userId, BpUserGift gift) {
        String giftType = normalizeCode(gift.getGiftType());
        if (!isAiQuotaGift(giftType)) {
            return;
        }
        int amount = gift.getGiftValue() != null ? gift.getGiftValue() : 0;
        if (amount <= 0) {
            return;
        }
        userService.addAiQuota(userId, amount);
        aiQuotaLogService.logChange(userId, "GIFT", amount,
                "BP_USER_GIFT", String.valueOf(gift.getId()),
                StringUtils.hasText(gift.getGiftName()) ? "Use gift for AI quota: " + gift.getGiftName() : "Use gift for AI quota");
    }

    private boolean isAiQuotaGift(String type) {
        return "AI_QUOTA".equals(type) || "AI_COUNT".equals(type) || "AI_TIMES".equals(type);
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }
}
