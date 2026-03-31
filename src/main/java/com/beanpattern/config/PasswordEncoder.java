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
        // 兼容旧 MD5 密码（迁移期间）：MD5 长度固定 32 位且不含 $
        if (encodedPassword.length() == 32 && !encodedPassword.startsWith("$")) {
            return md5(rawPassword).equals(encodedPassword);
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /** 是否为旧 MD5 格式（需要升级） */
    public boolean isLegacyMd5(String encodedPassword) {
        return encodedPassword != null
                && encodedPassword.length() == 32
                && !encodedPassword.startsWith("$");
    }

    private static String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
}
