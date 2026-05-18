package com.beanpattern.config;

import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ProfileIncompleteException;
import com.beanpattern.model.UnauthorizedException;
import com.beanpattern.security.JwtTokenService;
import com.beanpattern.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SessionHelper {

    public static final String HEADER_SESSION = "X-Session-Id";

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    public SessionHelper(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    public String resolveOpenId(HttpServletRequest request) {
        String token = request.getHeader(HEADER_SESSION);
        if (!StringUtils.hasText(token)) return null;
        return jwtTokenService.verifyAndGetOpenId(token);
    }

    public UserEntity getUser(HttpServletRequest request) {
        return resolveUser(request);
    }

    public UserEntity resolveUser(HttpServletRequest request) {
        String openId = resolveOpenId(request);
        if (!StringUtils.hasText(openId)) return null;
        return userService.getOrCreateByOpenId(openId);
    }

    public UserEntity requireUser(HttpServletRequest request) {
        UserEntity user = resolveUser(request);
        if (user == null) throw new UnauthorizedException();
        return user;
    }

    public UserEntity requireCompleteProfileUser(HttpServletRequest request) {
        UserEntity user = requireUser(request);
        if (!StringUtils.hasText(user.getNickName()) || !StringUtils.hasText(user.getAvatarUrl())) {
            throw new ProfileIncompleteException();
        }
        return user;
    }
}
