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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ActivityService {

    private final ActivityConfigMapper activityMapper;
    private final UserActivityLogMapper logMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public ActivityService(ActivityConfigMapper activityMapper,
                          UserActivityLogMapper logMapper,
                          UserMapper userMapper) {
        this.activityMapper = activityMapper;
        this.logMapper = logMapper;
        this.userMapper = userMapper;
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

        // 检查活动是否有效
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartAt()) || now.isAfter(activity.getEndAt())) {
            throw new IllegalStateException("活动未开始或已结束");
        }

        // 检查用户是否已参与
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
        // 1. 查询活动
        ActivityConfig activity = activityMapper.findByCode(activityCode);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        // 2. 检查活动有效性
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

        // 3. 检查名额
        if (activity.getTotalQuota() > 0) {
            int affected = activityMapper.decrementQuota(activity.getId());
            if (affected == 0) {
                throw new IllegalStateException("名额已抢完");
            }
        }

        try {
            // 4. 记录参与日志（唯一索引防止重复）
            UserActivityLog log = new UserActivityLog();
            log.setUserId(userId);
            log.setActivityId(activity.getId());
            log.setActivityCode(activityCode);
            log.setActionType("CLAIM");
            logMapper.insert(log);

            // 5. 发放礼品
            if ("GIFT".equals(activity.getActivityType()) && activity.getGiftItems() != null) {
                JsonNode gifts = objectMapper.readTree(activity.getGiftItems());
                for (JsonNode gift : gifts) {
                    String type = gift.get("gift_type").asText();
                    int value = gift.get("gift_value").asInt();

                    if ("AI_QUOTA".equals(type)) {
                        userMapper.addAiQuota(userId, value);
                    } else if ("VIP_DAYS".equals(type)) {
                        userMapper.addVipDays(userId, value);
                    } else if ("MAGIC_COINS".equals(type)) {
                        userMapper.addCoins(userId, value);
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "领取成功！");
            return result;

        } catch (DuplicateKeyException e) {
            // 回滚名额
            if (activity.getTotalQuota() > 0) {
                activityMapper.update(activity);
            }
            throw new IllegalStateException("您已领取过该活动");
        } catch (Exception e) {
            throw new RuntimeException("领取失败：" + e.getMessage());
        }
    }
}
