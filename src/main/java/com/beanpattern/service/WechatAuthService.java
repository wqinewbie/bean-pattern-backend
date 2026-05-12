package com.beanpattern.service;

import com.beanpattern.config.AppProperties;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.WxLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WechatAuthService {

    private static final Logger log = LoggerFactory.getLogger(WechatAuthService.class);

    private final AppProperties appProperties;
    private final UserService userService;
    private final InviteCodeService inviteCodeService;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private volatile String cachedAccessToken;
    private volatile long cachedAccessTokenExpireAt;

    public WechatAuthService(AppProperties appProperties, UserService userService, InviteCodeService inviteCodeService) {
        this.appProperties = appProperties;
        this.userService = userService;
        this.inviteCodeService = inviteCodeService;
    }

    public WxLoginResponse wxLogin(String code, String inviteCode) {
        try {
            String openId = fetchOpenIdFromWechat(code);
            if (!StringUtils.hasText(openId)) {
                throw new IllegalStateException("无法获取微信 openid，请检查 appId/appSecret 与 code 是否有效");
            }

            UserEntity user = userService.getOrCreateByOpenId(openId);
            String selfInviteCode = inviteCodeService.ensureInviteCode(user);
            inviteCodeService.bindInviteRelationIfNeeded(user.getId(), inviteCode);

            String token = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString((openId + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
            return new WxLoginResponse(token, openId, selfInviteCode);
        } catch (Exception e) {
            log.warn("[wxLogin][failed] msg={}, codeLen={}", e.getMessage(), code == null ? 0 : code.length());
            throw new RuntimeException("wxLogin failed: " + e.getMessage(), e);
        }
    }

    public String fetchPhoneNumberByCode(String phoneCode) {
        if (!StringUtils.hasText(phoneCode)) {
            throw new IllegalArgumentException("手机号授权 code 不能为空");
        }
        String accessToken = getAccessToken();
        if (!StringUtils.hasText(accessToken)) {
            throw new IllegalStateException("无法获取微信 access_token，请检查 appId/appSecret");
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://api.weixin.qq.com/wxa/business/getuserphonenumber")
                    .queryParam("access_token", accessToken)
                    .build(true)
                    .toUri();

            String reqJson = "{\"code\":\"" + phoneCode.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(reqJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("微信手机号接口调用失败: HTTP " + response.statusCode());
            }
            String body = response.body();
            String phone = extractJsonStringValue(body, "phoneNumber");
            if (!StringUtils.hasText(phone)) {
                String errmsg = extractJsonStringValue(body, "errmsg");
                throw new IllegalStateException("微信手机号解析失败" + (StringUtils.hasText(errmsg) ? ("(" + errmsg + ")") : ""));
            }
            return phone;
        } catch (Exception e) {
            throw new RuntimeException("微信手机号解密失败: " + e.getMessage(), e);
        }
    }

    private String getAccessToken() {
        long now = Instant.now().getEpochSecond();
        if (StringUtils.hasText(cachedAccessToken) && cachedAccessTokenExpireAt > now + 60) {
            return cachedAccessToken;
        }

        String appId = appProperties.getWechat().getAppId();
        String appSecret = appProperties.getWechat().getAppSecret();
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(appSecret)) {
            return null;
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString("https://api.weixin.qq.com/cgi-bin/token")
                    .queryParam("grant_type", "client_credential")
                    .queryParam("appid", appId)
                    .queryParam("secret", appSecret)
                    .build(true)
                    .toUri();

            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String token = extractJsonStringValue(response.body(), "access_token");
                String expiresInStr = extractJsonNumberValue(response.body(), "expires_in");
                if (StringUtils.hasText(token)) {
                    long expiresIn = 7200;
                    if (StringUtils.hasText(expiresInStr)) {
                        try { expiresIn = Long.parseLong(expiresInStr); } catch (Exception ignored) {}
                    }
                    cachedAccessToken = token;
                    cachedAccessTokenExpireAt = now + expiresIn;
                    return token;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String fetchOpenIdFromWechat(String code) {
        String appId = appProperties.getWechat().getAppId();
        String appSecret = appProperties.getWechat().getAppSecret();
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(appSecret)) {
            throw new IllegalStateException("微信配置缺失: appId/appSecret 未设置");
        }
        if (!StringUtils.hasText(code)) {
            throw new IllegalStateException("微信登录 code 为空");
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString("https://api.weixin.qq.com/sns/jscode2session")
                    .queryParam("appid", appId)
                    .queryParam("secret", appSecret)
                    .queryParam("js_code", code)
                    .queryParam("grant_type", "authorization_code")
                    .build(true)
                    .toUri();
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("jscode2session HTTP " + response.statusCode() + ", body=" + shorten(body, 300));
            }

            String errcode = extractJsonNumberValue(body, "errcode");
            if (StringUtils.hasText(errcode) && !"0".equals(errcode)) {
                String errmsg = extractJsonStringValue(body, "errmsg");
                throw new IllegalStateException("微信登录失败 errcode=" + errcode + ", errmsg=" + (StringUtils.hasText(errmsg) ? errmsg : "unknown"));
            }

            String openId = extractJsonStringValue(body, "openid");
            if (!StringUtils.hasText(openId)) {
                throw new IllegalStateException("微信响应缺少 openid, body=" + shorten(body, 300));
            }
            return openId;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("调用微信登录接口异常: " + e.getMessage(), e);
        }
    }

    private String extractJsonStringValue(String json, String key) {
        if (!StringUtils.hasText(json) || !StringUtils.hasText(key)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractJsonNumberValue(String json, String key) {
        if (!StringUtils.hasText(json) || !StringUtils.hasText(key)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String shorten(String s, int maxLen) {
        if (!StringUtils.hasText(s)) return "";
        String clean = s.replaceAll("\\s+", " ").trim();
        if (clean.length() <= maxLen) return clean;
        return clean.substring(0, maxLen) + "...";
    }
}
