package com.beanpattern.controller;

import com.beanpattern.entity.ActivityConfig;
import com.beanpattern.mapper.ActivityConfigMapper;
import com.beanpattern.model.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activities")
public class AdminActivityController {

    private final ActivityConfigMapper activityMapper;
    private final ObjectMapper objectMapper;

    public AdminActivityController(ActivityConfigMapper activityMapper) {
        this.activityMapper = activityMapper;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping
    public ApiResponse<List<ActivityConfig>> getActivities() {
        return ApiResponse.ok(activityMapper.findAll());
    }

    @PostMapping
    public ApiResponse<Void> createActivity(@RequestBody ActivityConfig activity) {
        if (activity.getRemainQuota() == null) {
            activity.setRemainQuota(activity.getTotalQuota());
        }
        if (activity.getStatus() == null) {
            activity.setStatus(true);
        }
        normalizeActivity(activity);
        validateActivity(activity);
        activityMapper.insert(activity);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateActivity(@PathVariable Long id, @RequestBody ActivityConfig activity) {
        activity.setId(id);
        normalizeActivity(activity);
        validateActivity(activity);
        activityMapper.update(activity);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        activityMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id, @RequestParam Boolean status) {
        activityMapper.updateStatus(id, status);
        return ApiResponse.ok(null);
    }

    private void normalizeActivity(ActivityConfig activity) {
        activity.setActivityType(normalizeActivityType(activity.getActivityType()));
        activity.setGiftPackageCode(StringUtils.hasText(activity.getGiftPackageCode()) ? activity.getGiftPackageCode().trim() : null);
        activity.setContentJson(normalizeJson(activity.getContentJson(), "富文本JSON结构"));
        if (!"GIFT".equals(activity.getActivityType())) {
            activity.setGiftPackageCode(null);
        }
        if (!StringUtils.hasText(activity.getPageType())) {
            activity.setPageType("RICH_TEXT");
        }
        if (!StringUtils.hasText(activity.getLimitType())) {
            activity.setLimitType("ONCE");
        }
    }

    private String normalizeActivityType(String activityType) {
        if (!StringUtils.hasText(activityType)) {
            return "CONTENT";
        }
        if ("DISCOUNT".equals(activityType) || "TASK".equals(activityType)) {
            return "CONTENT";
        }
        return activityType;
    }

    private void validateActivity(ActivityConfig activity) {
        if (!StringUtils.hasText(activity.getActivityCode())) {
            throw new IllegalArgumentException("活动编码不能为空");
        }
        if (!StringUtils.hasText(activity.getTitle())) {
            throw new IllegalArgumentException("活动标题不能为空");
        }
        if (activity.getStartAt() == null || activity.getEndAt() == null) {
            throw new IllegalArgumentException("活动开始时间和结束时间不能为空");
        }
        if (!activity.getEndAt().isAfter(activity.getStartAt())) {
            throw new IllegalArgumentException("活动结束时间必须晚于开始时间");
        }
        if (activity.getTotalQuota() == null || activity.getTotalQuota() < 0) {
            throw new IllegalArgumentException("总名额不能小于0");
        }
        if (activity.getRemainQuota() == null || activity.getRemainQuota() < 0) {
            throw new IllegalArgumentException("剩余名额不能小于0");
        }
        if (activity.getTotalQuota() > 0 && activity.getRemainQuota() > activity.getTotalQuota()) {
            throw new IllegalArgumentException("剩余名额不能大于总名额");
        }
        if ("GIFT".equals(activity.getActivityType()) && !StringUtils.hasText(activity.getGiftPackageCode())) {
            throw new IllegalArgumentException("礼品活动必须绑定礼品包");
        }
        if (!"GIFT".equals(activity.getActivityType()) && !"CONTENT".equals(activity.getActivityType())) {
            throw new IllegalArgumentException("不支持的活动类型：" + activity.getActivityType());
        }
        if ("CLAIM".equals(activity.getButtonAction()) && !"GIFT".equals(activity.getActivityType())) {
            throw new IllegalArgumentException("领取按钮只能用于已绑定礼品包的礼品活动");
        }
        String buttonAction = activity.getButtonAction();
        if ("EXTERNAL".equals(buttonAction)) {
            throw new IllegalArgumentException("活动跳转URL仅支持小程序内部页面路径");
        }
        if ("JUMP".equals(buttonAction) || "NAVIGATE".equals(buttonAction)) {
            if (!StringUtils.hasText(activity.getButtonUrl())) {
                throw new IllegalArgumentException("活动跳转URL不能为空");
            }
            if (!activity.getButtonUrl().startsWith("/pages/")) {
                throw new IllegalArgumentException("活动跳转URL仅支持小程序内部页面路径");
            }
        }
    }

    private String normalizeJson(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            objectMapper.readTree(value);
            return value;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(fieldName + "不是有效的JSON格式");
        }
    }
}
