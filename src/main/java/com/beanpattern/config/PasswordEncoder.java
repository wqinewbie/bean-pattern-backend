package com.beanpattern.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具：统一使用 BCrypt 加密。
 * BCrypt 每次加密结果不同，验证时使用 matches() 方法。
 * 强度默认为 10（约 100ms/次），可根据服务器性能调整。
 */
@Component
public class PasswordEncoder {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    /** 加密密码 */
    public String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** 校验密码是否匹配 */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) return false;
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /** 识别历史 MD5（32位十六进制） */
    public boolean isLegacyMd5(String encodedPassword) {
        if (encodedPassword == null) return false;
        return encodedPassword.matches("^[a-fA-F0-9]{32}$");
    }
}
