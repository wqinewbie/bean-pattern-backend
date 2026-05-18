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
        UserEntity user = sessionHelper.resolveUser(request);
        Long userId = user != null ? user.getId() : null;
        return ApiResponse.ok(activityService.getActivityDetail(code, userId));
    }

    /**
     * 记录活动浏览
     */
    @PostMapping("/view")
    public ApiResponse<String> recordView(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String activityCode = body.get("activityCode");
        if (activityCode == null || activityCode.isEmpty()) {
            return ApiResponse.fail("activityCode不能为空");
        }
        return ApiResponse.ok("ok");
    }

    /**
     * 领取活动礼品
     */
    @PostMapping("/claim")
    public ApiResponse<Map<String, Object>> claimGift(@RequestBody Map<String, String> body, HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        String activityCode = body.get("activityCode");
        return ApiResponse.ok(activityService.claimActivityGift(activityCode, user.getId()));
    }
}
