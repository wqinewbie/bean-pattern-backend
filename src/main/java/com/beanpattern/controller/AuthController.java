package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.WxLoginRequest;
import com.beanpattern.model.WxLoginResponse;
import com.beanpattern.service.WechatAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final WechatAuthService wechatAuthService;

    public AuthController(WechatAuthService wechatAuthService) {
        this.wechatAuthService = wechatAuthService;
    }

    @PostMapping({"/wx-login", "/wechat-login"})
    public ApiResponse<WxLoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.ok(wechatAuthService.wxLogin(request.getCode(), request.getInviteCode()));
    }

    @PostMapping("/login")
    public ApiResponse<WxLoginResponse> login(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.ok(wechatAuthService.wxLogin(request.getCode(), request.getInviteCode()));
    }
}
