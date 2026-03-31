package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.ImageTaskMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.vo.UserVO;
import com.beanpattern.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户相关接口
 * GET  /api/user/profile    - 获取用户信息（需登录，401）
 * GET  /api/user/stats      - 获取用户统计（需登录，401）
 * POST /api/user/update     - 更新昵称和头像（需登录，401）
 * POST /api/user/bind-phone - 绑定手机号（需登录，401）
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final SessionHelper sessionHelper;
    private final UserService userService;
    private final ImageTaskMapper imageTaskMapper;

    public UserController(SessionHelper sessionHelper,
                          UserService userService,
                          ImageTaskMapper imageTaskMapper) {
        this.sessionHelper = sessionHelper;
        this.userService = userService;
        this.imageTaskMapper = imageTaskMapper;
    }

    @GetMapping("/profile")
    public ApiResponse<UserVO> profile(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        return ApiResponse.ok(UserVO.from(user));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total",   imageTaskMapper.countByUser(user.getId()));
        data.put("success", imageTaskMapper.countSuccessByUser(user.getId()));
        data.put("ai",      imageTaskMapper.countAiByUser(user.getId()));
        return ApiResponse.ok(data);
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
        userService.bindPhone(user.getId(), phone);
        return ApiResponse.ok("ok");
    }
}
