package com.beanpattern.config;

import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.UnauthorizedException;
import com.beanpattern.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Session 工具：从请求 Header（X-Session-Id）中解析 token，取出 openId 并获取用户实体。
 * Token 格式：Base64(openId:timestamp)，由 WechatAuthService.wxLogin() 生成。
 */
@Component
public class SessionHelper {

    public static final String HEADER_SESSION = "X-Session-Id";

    private final UserService userService;

    public SessionHelper(UserService userService) {
        this.userService = userService;
    }

    /**
     * 从请求头中解析 openId，失败返回 null。
     */
    public String resolveOpenId(HttpServletRequest request) {
        String token = request.getHeader(HEADER_SESSION);
        if (!StringUtils.hasText(token)) return null;
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            int colonIdx = decoded.lastIndexOf(':');
            if (colonIdx <= 0) return null;
            return decoded.substring(0, colonIdx);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析用户实体，未登录返回 null。
     * 适用于允许匿名访问的接口。
     */
    public UserEntity resolveUser(HttpServletRequest request) {
        String openId = resolveOpenId(request);
        if (!StringUtils.hasText(openId)) return null;
        return userService.getOrCreateByOpenId(openId);
    }

    /**
     * 解析用户实体，未登录抛出 UnauthorizedException（HTTP 401）。
     * 适用于强制登录的接口。
     */
    public UserEntity requireUser(HttpServletRequest request) {
        UserEntity user = resolveUser(request);
        if (user == null) throw new UnauthorizedException();
        return user;
    }
}
