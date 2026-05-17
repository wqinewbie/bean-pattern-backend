package com.beanpattern.service;

import com.beanpattern.entity.GiftItem;
import com.beanpattern.entity.GiftType;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.GiftItemMapper;
import com.beanpattern.mapper.GiftTypeConfigMapper;
import com.beanpattern.mapper.GiftTypeMapper;
import com.beanpattern.mapper.UserGiftMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 礼品服务
 */
@Service
public class GiftService {

    private final GiftTypeMapper giftTypeMapper;
    private final GiftTypeConfigMapper giftTypeConfigMapper;
    private final GiftItemMapper giftItemMapper;
    private final UserGiftMapper userGiftMapper;
    private final GiftPackageService giftPackageService;
    private final NotificationService notificationService;

    public GiftService(GiftTypeMapper giftTypeMapper,
                       GiftTypeConfigMapper giftTypeConfigMapper,
                       GiftItemMapper giftItemMapper,
                       UserGiftMapper userGiftMapper,
                       GiftPackageService giftPackageService,
                       NotificationService notificationService) {
        this.giftTypeMapper = giftTypeMapper;
        this.giftTypeConfigMapper = giftTypeConfigMapper;
        this.giftItemMapper = giftItemMapper;
        this.userGiftMapper = userGiftMapper;
        this.giftPackageService = giftPackageService;
        this.notificationService = notificationService;
    }

    /**
     * 获取所有启用的礼品类型
     */
    public List<GiftType> getAllActiveTypes() {
        return giftTypeMapper.findAllActive();
    }

    /**
     * 根据分类获取礼品类型
     */
    public List<GiftType> getTypesByCategory(String category) {
        return giftTypeMapper.findByCategory(category);
    }

    /**
     * 获取所有可用礼品项
     */
    public List<GiftItem> getAvailableGifts() {
        return giftItemMapper.findAllAvailable(LocalDateTime.now());
    }

    /**
     * 根据类型ID获取可用礼品
     */
    public List<GiftItem> getAvailableGiftsByTypeId(Long giftTypeId) {
        return giftItemMapper.findAvailableByTypeId(giftTypeId, LocalDateTime.now());
    }

    /**
     * 获取用户的礼品列表
     */
    public List<UserGift> getUserGifts(Long userId) {
        List<UserGift> gifts = userGiftMapper.findByUserId(userId);
        gifts.forEach(this::fillUsageHint);
        return gifts;
    }

    /**
     * 获取用户可用的礼品
     */
    public List<UserGift> getAvailableUserGifts(Long userId) {
        return userGiftMapper.findAvailableByUserId(userId, 0, LocalDateTime.now());
    }

    /**
     * 获取用户可用的优惠券（按商品类型过滤）
     */
    public List<UserGift> getAvailableCoupons(Long userId, String productType) {
        List<UserGift> allGifts = userGiftMapper.findAvailableByUserId(userId, 0, LocalDateTime.now());

        // 根据商品类型过滤优惠券
        return allGifts.stream()
                .filter(gift -> "COUPON".equals(gift.getGiftCategory()))
                .filter(gift -> {
                    String giftCode = gift.getGiftCode();
                    if ("vip".equals(productType)) {
                        return "VIP_COUPON".equals(giftCode);
                    } else if ("card".equals(productType)) {
                        return "CARD_COUPON".equals(giftCode) || "VIP_CARD_COUPON".equals(giftCode);
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 使用礼品（将状态改为已使用）
     */
    @Transactional
    public boolean useGift(Long userId, Long giftId) {
        return useGift(userId, giftId, false);
    }

    @Transactional
    public boolean useGift(Long userId, Long giftId, boolean redeemNow) {
        UserGift gift = userGiftMapper.findById(giftId);
        if (gift == null) {
            return false;
        }
        if (!gift.getUserId().equals(userId)) {
            return false;
        }
        if (gift.getStatus() != 0) {
            return false;
        }
        if (gift.getExpireAt() != null && gift.getExpireAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (redeemNow && "GIFT_PACKAGE".equals(gift.getGiftCode())) {
            giftPackageService.redeemPackageGift(userId, giftId);
            return true;
        }

        userGiftMapper.use(giftId);
        return true;
    }

    /**
     * 根据ID获取礼品项
     */
    public GiftItem getGiftItemById(Long giftItemId) {
        return giftItemMapper.findById(giftItemId);
    }

    /**
     * 发放礼品给用户
     */
    @Transactional
    public UserGift grantGift(Long userId, Long giftItemId, String source,
                              Long taskId, Long shareRecordId, Long orderId) {
        GiftItem item = giftItemMapper.findById(giftItemId);
        if (item == null) {
            UserGift gift = new UserGift();
            gift.setUserId(userId);
            gift.setGiftItemId(giftItemId);
            gift.setSource(source);
            gift.setTaskId(taskId);
            gift.setShareRecordId(shareRecordId);
            gift.setOrderId(orderId);
            gift.setStatus(0);
            userGiftMapper.insert(gift);

            try {
                notificationService.createGiftNotification(userId, "礼品", "您获得了新礼品，请在我的礼品中查看", gift.getId());
            } catch (Exception ignored) {
                // 通知发送失败不影响主流程
            }

            return gift;
        }

        if (item.getTotalQuantity() > 0) {
            int updated = giftItemMapper.decrementQuantity(giftItemId);
            if (updated == 0) {
                throw new IllegalStateException("礼品库存不足");
            }
        }

        UserGift gift = new UserGift();
        gift.setUserId(userId);
        gift.setGiftItemId(giftItemId);
        gift.setGiftCode(item.getGiftCode());
        gift.setGiftName(item.getName());
        gift.setGiftCategory(getGiftCategory(item.getGiftTypeId()));
        gift.setValue(item.getValue());
        gift.setSource(source);
        gift.setTaskId(taskId);
        gift.setShareRecordId(shareRecordId);
        gift.setOrderId(orderId);

        if (item.getEndAt() != null) {
            gift.setExpireAt(item.getEndAt());
        } else {
            gift.setExpireAt(LocalDateTime.now().plusDays(30));
        }

        gift.setStatus(0);
        userGiftMapper.insert(gift);

        // 发送礼品到账通知
        try {
            String giftName = gift.getGiftName() != null ? gift.getGiftName() : "礼品";
            String desc = "您获得了" + giftName + "，请在我的礼品中查看";
            notificationService.createGiftNotification(userId, giftName, desc, gift.getId());
        } catch (Exception ignored) {
            // 通知发送失败不影响主流程
        }

        return gift;
    }

    /**
     * 获取礼品分类
     */
    private String getGiftCategory(Long giftTypeId) {
        if (giftTypeId == null) {
            return "OTHER";
        }
        GiftType type = giftTypeMapper.findById(giftTypeId);
        return type != null ? type.getGiftCategory() : "OTHER";
    }

    private void fillUsageHint(UserGift gift) {
        if (gift == null) return;

        gift.setUsageMode("DIRECT_USE");
        gift.setTargetTab(null);

        String giftCode = gift.getGiftCode() == null ? "" : gift.getGiftCode().toUpperCase();
        if ("VIP_COUPON".equals(giftCode)) {
            gift.setUsageMode("JUMP_VIP");
            gift.setTargetTab("vip");
            return;
        }
        if ("CARD_COUPON".equals(giftCode) || "VIP_CARD_COUPON".equals(giftCode)) {
            gift.setUsageMode("JUMP_VIP");
            gift.setTargetTab("cards");
            return;
        }

        if (gift.getGiftItemId() != null) {
            GiftItem item = giftItemMapper.findById(gift.getGiftItemId());
            if (item != null && item.getGiftTypeId() != null) {
                com.beanpattern.entity.GiftTypeConfig typeConfig = giftTypeConfigMapper.findById(item.getGiftTypeId());
                if (typeConfig != null) {
                    String targetProductType = typeConfig.getTargetProductType();
                    if ("vip".equalsIgnoreCase(targetProductType)) {
                        gift.setUsageMode("JUMP_VIP");
                        gift.setTargetTab("vip");
                    } else if ("card".equalsIgnoreCase(targetProductType)) {
                        gift.setUsageMode("JUMP_VIP");
                        gift.setTargetTab("cards");
                    }
                }
            }
        }
    }
}
