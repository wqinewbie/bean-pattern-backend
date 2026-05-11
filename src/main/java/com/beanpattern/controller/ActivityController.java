package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 活动中心 Controller（用户端）
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final SessionHelper sessionHelper;
    private final ActivityService activityService;

    public ActivityController(SessionHelper sessionHelper, ActivityService activityService) {
        this.sessionHelper = sessionHelper;
        this.activityService = activityService;
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{code}")
    public ApiResponse<Map<String, Object>> getActivity(@PathVariable String code, HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.resolveUser(request);
            Long userId = user != null ? user.getId() : null;

            Map<String, Object> activity = activityService.getActivityDetail(code, userId);
            return ApiResponse.ok(activity);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取活动详情失败：" + e.getMessage());
        }
    }

    /**
     * 记录活动浏览
     */
    @PostMapping("/view")
    public ApiResponse<String> recordView(@RequestBody Map<String, String> body, HttpServletRequest request) {
        UserEntity user = sessionHelper.resolveUser(request);
        String activityCode = body.get("activityCode");

        // TODO: 实现浏览记录
        // 1. 记录到 bp_user_activity_log 表（action_type = VIEW）
        // 2. 更新活动的浏览次数

        return ApiResponse.ok("记录成功");
    }

    /**
     * 领取活动礼品
     */
    @PostMapping("/claim")
    public ApiResponse<Map<String, Object>> claimGift(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            String activityCode = body.get("activityCode");

            Map<String, Object> result = activityService.claimActivityGift(activityCode, user.getId());
            return ApiResponse.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("领取失败：" + e.getMessage());
        }
    }
}
