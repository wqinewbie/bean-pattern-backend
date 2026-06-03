package com.beanpattern.service;

import com.beanpattern.entity.ActivityConfig;
import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.RewardItem;
import com.beanpattern.entity.UserActivityLog;
import com.beanpattern.entity.BpUserGift;
import com.beanpattern.mapper.ActivityConfigMapper;
import com.beanpattern.mapper.UserActivityLogMapper;
import com.beanpattern.service.task.GiftPackageRewardHelper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ActivityService {

    private final ActivityConfigMapper activityMapper;
    private final UserActivityLogMapper logMapper;
    private final GiftPackageService giftPackageService;

    public ActivityService(ActivityConfigMapper activityMapper,
                           UserActivityLogMapper logMapper,
                           GiftPackageService giftPackageService) {
        this.activityMapper = activityMapper;
        this.logMapper = logMapper;
        this.giftPackageService = giftPackageService;
    }

    public Map<String, Object> getActivityDetail(String activityCode, Long userId) {
        ActivityConfig activity = activityMapper.findByCode(activityCode);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(activity.getStatus())) {
            throw new IllegalStateException("活动已下架");
        }
        if (activity.getStartAt() != null && now.isBefore(activity.getStartAt())) {
            throw new IllegalStateException("活动未开始");
        }
        if (activity.getEndAt() != null && now.isAfter(activity.getEndAt())) {
            throw new IllegalStateException("活动已过期");
        }

        boolean participated = false;
        if (userId != null) {
            participated = hasClaimed(userId, activity);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("activityCode", activity.getActivityCode());
        result.put("title", activity.getTitle());
        result.put("description", activity.getDescription());
        result.put("coverImage", activity.getCoverImage());
        result.put("contentHtml", activity.getContentHtml());
        result.put("buttonText", activity.getButtonText());
        result.put("buttonAction", activity.getButtonAction());
        result.put("buttonUrl", activity.getButtonUrl());
        result.put("activityType", activity.getActivityType());
        result.put("giftPackageCode", activity.getGiftPackageCode());
        result.put("totalQuota", activity.getTotalQuota());
        result.put("remainQuota", activity.getRemainQuota());
        result.put("startAt", activity.getStartAt());
        result.put("endAt", activity.getEndAt());
        result.put("participated", participated);
        return result;
    }

    @Transactional
    public Map<String, Object> claimActivityGift(String activityCode, Long userId) {
        ActivityConfig activity = activityMapper.findByCode(activityCode);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }
        if (!"GIFT".equals(activity.getActivityType())) {
            throw new IllegalStateException("该活动不支持领取礼品包");
        }
        if (!StringUtils.hasText(activity.getGiftPackageCode())) {
            throw new IllegalStateException("活动未绑定礼品包");
        }

        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartAt() != null && now.isBefore(activity.getStartAt())) {
            throw new IllegalStateException("活动未开始");
        }
        if (activity.getEndAt() != null && now.isAfter(activity.getEndAt())) {
            throw new IllegalStateException("活动已结束");
        }
        if (!Boolean.TRUE.equals(activity.getStatus())) {
            throw new IllegalStateException("活动已下线");
        }

        enforceClaimLimit(userId, activity);

        if (activity.getTotalQuota() != null && activity.getTotalQuota() > 0) {
            int affected = activityMapper.decrementQuota(activity.getId());
            if (affected == 0) {
                throw new IllegalStateException("名额已抢完");
            }
        }

        try {
            BpUserGift packageGift = giftPackageService.grantPackageToUser(
                    userId,
                    activity.getGiftPackageCode(),
                    "ACTIVITY:" + activity.getActivityCode()
            );

            // 从礼品包获取实际奖励信息用于日志记录
            GiftPackage giftPackage = giftPackageService.getByCode(activity.getGiftPackageCode());
            RewardItem rewardInfo = giftPackage != null
                ? GiftPackageRewardHelper.parseRewardInfo(giftPackage)
                : new RewardItem("GIFT_PACKAGE", 1, "礼品包");

            UserActivityLog log = new UserActivityLog();
            log.setUserId(userId);
            log.setActivityId(activity.getId());
            log.setActivityCode(activityCode);
            log.setActionType("CLAIM");
            log.setRewardType(rewardInfo.getType());
            log.setRewardValue(rewardInfo.getValue());
            log.setGiftId(packageGift.getId());
            logMapper.insert(log);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "领取成功，已放入我的礼品包");
            result.put("giftId", packageGift.getId());
            result.put("giftName", packageGift.getGiftName());
            result.put("claimMode", "PACKAGE_STORED");
            return result;
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("您已领取过该活动");
        }
    }

    private boolean hasClaimed(Long userId, ActivityConfig activity) {
        String limitType = activity.getLimitType() == null ? "ONCE" : activity.getLimitType();
        if ("UNLIMITED".equals(limitType)) {
            return false;
        }
        if ("DAILY".equals(limitType)) {
            return logMapper.countTodayByUserAndActivityAndAction(userId, activity.getId(), "CLAIM") > 0;
        }
        return logMapper.countByUserAndActivityAndAction(userId, activity.getId(), "CLAIM") > 0;
    }

    private void enforceClaimLimit(Long userId, ActivityConfig activity) {
        if (hasClaimed(userId, activity)) {
            throw new IllegalStateException("DAILY".equals(activity.getLimitType()) ? "今日已领取，明天再来吧" : "您已领取过该活动");
        }
    }
}
