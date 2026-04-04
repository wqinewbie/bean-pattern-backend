package com.beanpattern.config;

import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.PhoneUnboundException;
import com.beanpattern.model.ProfileIncompleteException;
import com.beanpattern.model.UnauthorizedException;
import com.beanpattern.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class SessionHelper {

    public static final String HEADER_SESSION = "X-Session-Id";

    private final UserService userService;

    public SessionHelper(UserService userService) {
        this.userService = userService;
    }

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

    public UserEntity requirePhoneBoundUser(HttpServletRequest request) {
        UserEntity user = requireUser(request);
        if (!StringUtils.hasText(user.getPhone())) {
            throw new PhoneUnboundException();
        }
        return user;
    }

    public UserEntity requireCompleteProfileUser(HttpServletRequest request) {
        UserEntity user = requirePhoneBoundUser(request);
        if (!StringUtils.hasText(user.getNickName()) || !StringUtils.hasText(user.getAvatarUrl())) {
            throw new ProfileIncompleteException();
        }
        return user;
    }
}
