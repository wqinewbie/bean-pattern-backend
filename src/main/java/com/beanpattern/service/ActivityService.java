package com.beanpattern.service;

import com.beanpattern.entity.ActivityConfig;
import com.beanpattern.entity.UserActivityLog;
import com.beanpattern.mapper.ActivityConfigMapper;
import com.beanpattern.mapper.UserActivityLogMapper;
import com.beanpattern.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public ActivityService(ActivityConfigMapper activityMapper,
                           UserActivityLogMapper logMapper,
                           UserMapper userMapper,
                           GiftPackageService giftPackageService) {
        this.activityMapper = activityMapper;
        this.logMapper = logMapper;
        this.giftPackageService = giftPackageService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取活动详情
     */
    public Map<String, Object> getActivityDetail(String activityCode, Long userId) {
        ActivityConfig activity = activityMapper.findByCode(activityCode);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartAt()) || now.isAfter(activity.getEndAt())) {
            throw new IllegalStateException("活动未开始或已结束");
        }

        boolean participated = false;
        if (userId != null) {
            UserActivityLog log = logMapper.findByUserAndActivityAndAction(
                userId, activity.getId(), "CLAIM"
            );
            participated = (log != null);
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
        result.put("totalQuota", activity.getTotalQuota());
        result.put("remainQuota", activity.getRemainQuota());
        result.put("startAt", activity.getStartAt());
        result.put("endAt", activity.getEndAt());
        result.put("participated", participated);

        return result;
    }

    /**
     * 领取活动礼品
     */
    @Transactional
    public Map<String, Object> claimActivityGift(String activityCode, Long userId) {
        ActivityConfig activity = activityMapper.findByCode(activityCode);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartAt())) {
            throw new IllegalStateException("活动未开始");
        }
        if (now.isAfter(activity.getEndAt())) {
            throw new IllegalStateException("活动已结束");
        }
        if (!activity.getStatus()) {
            throw new IllegalStateException("活动已下线");
        }

        if (activity.getTotalQuota() > 0) {
            int affected = activityMapper.decrementQuota(activity.getId());
            if (affected == 0) {
                throw new IllegalStateException("名额已抢完");
            }
        }

        try {
            UserActivityLog log = new UserActivityLog();
            log.setUserId(userId);
            log.setActivityId(activity.getId());
            log.setActivityCode(activityCode);
            log.setActionType("CLAIM");
            logMapper.insert(log);

            if ("GIFT".equals(activity.getActivityType()) && StringUtils.hasText(activity.getGiftItems())) {
                grantActivityGift(userId, activity.getGiftItems());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "领取成功！");
            return result;

        } catch (DuplicateKeyException e) {
            if (activity.getTotalQuota() > 0) {
                activityMapper.update(activity);
            }
            throw new IllegalStateException("您已领取过该活动");
        } catch (Exception e) {
            throw new RuntimeException("领取失败：" + e.getMessage());
        }
    }

    private void grantActivityGift(Long userId, String giftItems) throws Exception {
        JsonNode root = objectMapper.readTree(giftItems);
        if (root.isObject()) {
            String packageCode = readText(root, "giftPackageCode", readText(root, "packageCode", ""));
            if (StringUtils.hasText(packageCode)) {
                giftPackageService.grantPackageToUser(userId, packageCode);
                return;
            }
        }
        giftPackageService.grantItemsJsonToUser(userId, giftItems);
    }

    private String readText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asText();
    }
}
