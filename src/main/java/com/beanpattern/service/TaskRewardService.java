package com.beanpattern.service;

import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.UserGiftMapper;
import com.beanpattern.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Task reward delivery service shared by task flows that should not depend on TaskService.
 */
@Service
public class TaskRewardService {

    private final UserGiftMapper userGiftMapper;
    private final UserMapper userMapper;

    public TaskRewardService(UserGiftMapper userGiftMapper, UserMapper userMapper) {
        this.userGiftMapper = userGiftMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserGift grantReward(Long userId, String rewardType, Integer rewardValue,
                                String source, Long taskId, Long shareRecordId, Long orderId) {
        UserGift gift = new UserGift();
        gift.setUserId(userId);
        gift.setTaskId(taskId);
        gift.setShareRecordId(shareRecordId);
        gift.setOrderId(orderId);
        gift.setSource(source);
        gift.setValue(rewardValue);

        switch (rewardType) {
            case "VIP_DAYS" -> {
                gift.setGiftCode("VIP_DAYS_" + rewardValue);
                gift.setGiftName(rewardValue + "天VIP会员");
                gift.setGiftCategory("VIP_DAYS");
                userMapper.addVipDays(userId, rewardValue);
            }
            case "AI_COUNT", "AI_QUOTA" -> {
                gift.setGiftCode("AI_COUNT_" + rewardValue);
                gift.setGiftName(rewardValue + "次AI生成");
                gift.setGiftCategory("AI_COUNT");
                userMapper.addAiQuota(userId, rewardValue);
            }
            case "COUPON" -> {
                gift.setGiftCode("COUPON_" + rewardValue);
                gift.setGiftName(rewardValue + "元优惠券");
                gift.setGiftCategory("COUPON");
                gift.setExpireAt(LocalDateTime.now().plusDays(30));
            }
            default -> throw new IllegalArgumentException("未知奖励类型: " + rewardType);
        }

        userGiftMapper.insert(gift);
        return gift;
    }
}
