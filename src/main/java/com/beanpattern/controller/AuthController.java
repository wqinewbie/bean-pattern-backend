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

    /** 标准路径：/api/auth/wx-login */
    @PostMapping("/wx-login")
    public ApiResponse<WxLoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.ok(wechatAuthService.wxLogin(request.getCode()));
    }

    /** 别名：/api/auth/login，供小程序端 home.js 调用 */
    @PostMapping("/login")
    public ApiResponse<WxLoginResponse> login(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.ok(wechatAuthService.wxLogin(request.getCode()));
    }
}
