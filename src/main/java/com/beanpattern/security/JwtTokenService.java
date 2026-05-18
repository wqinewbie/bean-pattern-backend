package com.beanpattern.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenService {

    private static final long TOKEN_TTL_MS = 7 * 24 * 3600_000L;
    private static final String HMAC_ALG = "HmacSHA256";

    private final SecretKey secretKey;

    public JwtTokenService(@Value("${app.jwt.secret:}") String secret) {
        if (StringUtils.hasText(secret)) {
            this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG);
        } else {
            throw new IllegalStateException("app.jwt.secret 未配置，请在 application.yaml 中设置或通过 JWT_SECRET 环境变量注入");
        }
    }

    public String generateToken(String openId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + TOKEN_TTL_MS);
        return Jwts.builder()
                .subject(openId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String verifyAndGetOpenId(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String openId = claims.getSubject();
            if (!StringUtils.hasText(openId)) {
                return null;
            }
            return openId;
        } catch (JwtException e) {
            return null;
        }
    }
}
