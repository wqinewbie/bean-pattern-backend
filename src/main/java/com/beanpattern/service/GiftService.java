package com.beanpattern.service;

import com.beanpattern.entity.GiftItem;
import com.beanpattern.entity.GiftType;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.GiftItemMapper;
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
    private final GiftItemMapper giftItemMapper;
    private final UserGiftMapper userGiftMapper;

    public GiftService(GiftTypeMapper giftTypeMapper,
                       GiftItemMapper giftItemMapper,
                       UserGiftMapper userGiftMapper) {
        this.giftTypeMapper = giftTypeMapper;
        this.giftItemMapper = giftItemMapper;
        this.userGiftMapper = userGiftMapper;
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
        return userGiftMapper.findByUserId(userId);
    }

    /**
     * 获取用户可用的礼品
     */
    public List<UserGift> getAvailableUserGifts(Long userId) {
        return userGiftMapper.findAvailableByUserId(userId, 0, LocalDateTime.now());
    }

    /**
     * 使用礼品（将状态改为已使用）
     */
    @Transactional
    public boolean useGift(Long userId, Long giftId) {
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
        // 直接按ID查找礼品项
        GiftItem item = giftItemMapper.findById(giftItemId);
        if (item == null) {
            // 直接创建礼品记录（无库存管理）
            UserGift gift = new UserGift();
            gift.setUserId(userId);
            gift.setGiftItemId(giftItemId);
            gift.setSource(source);
            gift.setTaskId(taskId);
            gift.setShareRecordId(shareRecordId);
            gift.setOrderId(orderId);
            gift.setStatus(0);
            userGiftMapper.insert(gift);
            return gift;
        }

        // 扣减库存
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
        
        // 计算过期时间（默认30天）
        if (item.getEndAt() != null) {
            gift.setExpireAt(item.getEndAt());
        } else {
            gift.setExpireAt(LocalDateTime.now().plusDays(30));
        }
        
        gift.setStatus(0);
        userGiftMapper.insert(gift);
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
}
