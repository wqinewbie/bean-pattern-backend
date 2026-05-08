package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.PrivilegeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 权益校验接口（用户端）
 */
@RestController
@RequestMapping("/api/privilege")
public class PrivilegeController {

    private final SessionHelper sessionHelper;
    private final PrivilegeService privilegeService;

    public PrivilegeController(SessionHelper sessionHelper, PrivilegeService privilegeService) {
        this.sessionHelper = sessionHelper;
        this.privilegeService = privilegeService;
    }

    /**
     * 获取用户权益信息
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getPrivilegeInfo(HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            Map<String, Object> privileges = privilegeService.getUserPrivileges(user.getId());
            return ApiResponse.ok(privileges);
        } catch (Exception e) {
            return ApiResponse.fail("获取权益信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查图纸箱容量
     */
    @GetMapping("/check/pattern-box")
    public ApiResponse<Map<String, Object>> checkPatternBox(HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            boolean canAdd = privilegeService.checkPatternBoxLimit(user.getId());
            return ApiResponse.ok(Map.of("canAdd", canAdd));
        } catch (Exception e) {
            return ApiResponse.fail("检查图纸箱容量失败: " + e.getMessage());
        }
    }

    /**
     * 检查草稿箱容量
     */
    @GetMapping("/check/draft-box")
    public ApiResponse<Map<String, Object>> checkDraftBox(HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            boolean canAdd = privilegeService.checkDraftBoxLimit(user.getId());
            return ApiResponse.ok(Map.of("canAdd", canAdd));
        } catch (Exception e) {
            return ApiResponse.fail("检查草稿箱容量失败: " + e.getMessage());
        }
    }
}
