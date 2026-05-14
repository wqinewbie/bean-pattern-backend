package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.PrivilegeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/privilege")
public class PrivilegeController {

    private final SessionHelper sessionHelper;
    private final PrivilegeService privilegeService;

    public PrivilegeController(SessionHelper sessionHelper, PrivilegeService privilegeService) {
        this.sessionHelper = sessionHelper;
        this.privilegeService = privilegeService;
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getPrivilegeInfo(HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            return ApiResponse.ok(privilegeService.getUserPrivileges(user.getId()));
        } catch (Exception e) {
            return ApiResponse.fail("获取权益信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/check/pattern-box")
    public ApiResponse<Map<String, Object>> checkPatternBox(HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            return ApiResponse.ok(toLimitMap(privilegeService.getPatternBoxLimitStatus(user.getId())));
        } catch (Exception e) {
            return ApiResponse.fail("检查图纸箱容量失败: " + e.getMessage());
        }
    }

    @GetMapping("/check/draft-box")
    public ApiResponse<Map<String, Object>> checkDraftBox(HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            return ApiResponse.ok(toLimitMap(privilegeService.getDraftBoxLimitStatus(user.getId())));
        } catch (Exception e) {
            return ApiResponse.fail("检查草稿箱容量失败: " + e.getMessage());
        }
    }

    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> checkByKey(@RequestParam String key, HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            if ("pattern_box_limit".equals(key)) {
                return ApiResponse.ok(toLimitMap(privilegeService.getPatternBoxLimitStatus(user.getId())));
            }
            if ("draft_box_limit".equals(key)) {
                return ApiResponse.ok(toLimitMap(privilegeService.getDraftBoxLimitStatus(user.getId())));
            }
            if ("history_expire_days".equals(key)) {
                return ApiResponse.ok(Map.of("value", privilegeService.getHistoryExpireDays(user.getId())));
            }
            if ("watermark_control".equals(key)) {
                return ApiResponse.ok(Map.of("value", privilegeService.canControlWatermark(user.getId())));
            }
            return ApiResponse.fail("未知权益: " + key);
        } catch (Exception e) {
            return ApiResponse.fail("检查权益失败: " + e.getMessage());
        }
    }

    private Map<String, Object> toLimitMap(PrivilegeService.LimitStatus status) {
        return Map.of(
                "canAdd", status.canAdd(),
                "current", status.current(),
                "limit", status.limit(),
                "vip", status.vip()
        );
    }
}
