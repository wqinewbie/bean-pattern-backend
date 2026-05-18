package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.entity.UserVipRecord;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.vo.UserVO;
import com.beanpattern.service.UserService;
import com.beanpattern.service.VipService;
import com.beanpattern.service.WechatAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final SessionHelper sessionHelper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final WechatAuthService wechatAuthService;
    private final VipService vipService;

    public UserController(SessionHelper sessionHelper,
                          UserService userService,
                          UserMapper userMapper,
                          WechatAuthService wechatAuthService,
                          VipService vipService) {
        this.sessionHelper = sessionHelper;
        this.userService = userService;
        this.userMapper = userMapper;
        this.wechatAuthService = wechatAuthService;
        this.vipService = vipService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserVO> profile(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        return ApiResponse.ok(UserVO.from(user));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        return ApiResponse.ok(Map.of(
                "storageUsed", user.getCurrentStorage() != null ? user.getCurrentStorage() : 0,
                "storageQuota", user.getStorageQuota() != null ? user.getStorageQuota() : 0,
                "draftUsed", user.getCurrentDraft() != null ? user.getCurrentDraft() : 0,
                "draftQuota", user.getDraftQuota() != null ? user.getDraftQuota() : 0,
                "aiQuota", user.getAiQuota() != null ? user.getAiQuota() : 0,
                "vipLevel", user.getVipLevel() != null ? user.getVipLevel() : 0
        ));
    }

    @PostMapping("/update")
    public ApiResponse<String> update(@RequestBody Map<String, String> body,
                                      HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        userService.updateProfile(user.getId(), body.get("nickName"), body.get("avatarUrl"));
        return ApiResponse.ok("ok");
    }

    @PostMapping("/bind-phone")
    public ApiResponse<String> bindPhone(@RequestBody Map<String, String> body,
                                         HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        String phone = body.getOrDefault("phone", "").trim();
        if (phone.isEmpty()) return ApiResponse.fail("手机号不能为空");
        if (!phone.matches("^1\\d{10}$")) return ApiResponse.fail("手机号格式不正确");
        userService.bindPhone(user.getId(), phone);
        return ApiResponse.ok("ok");
    }

    @PostMapping("/bind-phone-wx")
    public ApiResponse<String> bindPhoneWx(@RequestBody Map<String, String> body,
                                           HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        String phoneCode = body.getOrDefault("code", "").trim();
        if (phoneCode.isEmpty()) return ApiResponse.fail("code不能为空");
        String phone = wechatAuthService.fetchPhoneNumberByCode(phoneCode);
        if (!phone.matches("^1\\d{10}$")) return ApiResponse.fail("微信返回手机号格式异常");
        userService.bindPhone(user.getId(), phone);
        return ApiResponse.ok(phone);
    }
    
    /**
     * 获取VIP状态详情（V6.0新接口）
     */
    @GetMapping("/vip-status")
    public ApiResponse<Map<String, Object>> getVipStatus(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        UserVipRecord vipRecord = vipService.getActiveVipRecord(user.getId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("vipLevel", user.getVipLevel() != null ? user.getVipLevel() : 0);
        result.put("vipExpireAt", user.getVipExpireAt() != null ? user.getVipExpireAt().toString() : null);
        result.put("hasActiveVip", vipRecord != null);
        
        if (vipRecord != null) {
            result.put("aiUsedCount", vipRecord.getAiUsedCount());
            result.put("aiResetAt", vipRecord.getAiResetAt() != null ? vipRecord.getAiResetAt().toString() : null);
        }
        
        // 配额信息
        result.put("storageQuota", user.getStorageQuota() != null ? user.getStorageQuota() : 10);
        result.put("currentStorage", user.getCurrentStorage() != null ? user.getCurrentStorage() : 0);
        result.put("draftQuota", user.getDraftQuota() != null ? user.getDraftQuota() : 5);
        result.put("currentDraft", user.getCurrentDraft() != null ? user.getCurrentDraft() : 0);
        result.put("availableBrands", user.getAvailableBrands());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 检查配额状态（V6.0新接口）
     */
    @GetMapping("/quota-check")
    public ApiResponse<Map<String, Object>> checkQuota(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("storageFull", userService.isStorageQuotaFull(user.getId()));
        result.put("draftFull", userService.isDraftQuotaFull(user.getId()));
        result.put("hasAiQuota", userService.hasAiQuota(user.getId()));
        
        return ApiResponse.ok(result);
    }
}
