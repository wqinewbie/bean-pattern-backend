package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.CheckinService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 签到 Controller
 */
@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final SessionHelper sessionHelper;
    private final CheckinService checkinService;

    public CheckinController(SessionHelper sessionHelper, CheckinService checkinService) {
        this.sessionHelper = sessionHelper;
        this.checkinService = checkinService;
    }

    /**
     * 获取签到状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getStatus(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        Map<String, Object> status = checkinService.getCheckinStatus(user.getId());
        return ApiResponse.ok(status);
    }

    /**
     * 执行签到
     */
    @PostMapping("/do")
    public ApiResponse<Map<String, Object>> doCheckin(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        try {
            Map<String, Object> result = checkinService.doCheckin(user.getId());
            return ApiResponse.ok(result);
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 领取签到奖励
     */
    @PostMapping("/claim")
    public ApiResponse<Map<String, Object>> claimReward(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        try {
            Map<String, Object> result = checkinService.claimReward(user.getId());
            return ApiResponse.ok(result);
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 获取签到配置
     */
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getConfig() {
        Map<String, Object> config = checkinService.getCheckinConfig();
        return ApiResponse.ok(config);
    }
}
