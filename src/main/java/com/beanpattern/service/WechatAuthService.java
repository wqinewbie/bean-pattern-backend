package com.beanpattern.service;

import com.beanpattern.config.AppProperties;
import com.beanpattern.model.WxLoginResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WechatAuthService {

    private final AppProperties appProperties;
    private final UserService userService;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public WechatAuthService(AppProperties appProperties, UserService userService) {
        this.appProperties = appProperties;
        this.userService = userService;
    }

    public WxLoginResponse wxLogin(String code) {
        try {
        // 1) 优先走真实微信 code2session，拿到 openid。
        // 2) 如果本地未配置微信参数或调用失败，则降级为 mock openid，便于联调小程序主流程。
        String openId = fetchOpenIdFromWechat(code);
        if (!StringUtils.hasText(openId)) {
            // 未配置微信参数时，用 code 作为 mock openid 种子，保证不同 code 对应不同用户
            openId = "mock_" + (StringUtils.hasText(code) ? code.substring(0, Math.min(code.length(), 16)) : "dev_001");
        }

        // 把用户落库（不存在就创建），后续任务记录都通过 userId 关联
        userService.getOrCreateByOpenId(openId);

        // 这里先生成一个简单 token（演示用途），实际项目可替换为 JWT 或服务端会话。
        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((openId + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        return new WxLoginResponse(token, openId);
        } catch (Exception e) {
            throw new RuntimeException("wxLogin failed: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    private String fetchOpenIdFromWechat(String code) {
        String appId = appProperties.getWechat().getAppId();
        String appSecret = appProperties.getWechat().getAppSecret();
        // 未配置微信参数时不报错，直接返回 null，外层会走 mock 流程。
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(appSecret)) {
            return null;
        }
        try {
            // 调用微信官方 jscode2session 接口，code 换取 openid。
            // Spring 6.2+ 使用 fromUriString，不再使用已移除的 fromHttpUrl
            URI uri = UriComponentsBuilder.fromUriString("https://api.weixin.qq.com/sns/jscode2session")
                    .queryParam("appid", appId)
                    .queryParam("secret", appSecret)
                    .queryParam("js_code", code)
                    .queryParam("grant_type", "authorization_code")
                    .build(true)
                    .toUri();
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                // 不依赖 Jackson，直接从 JSON 字符串提取 openid。
                return extractJsonStringValue(response.body(), "openid");
            }
        } catch (Exception ignored) {
            // 真实调用失败时静默降级，避免影响你本地调试上传/处理主链路。
        }
        return null;
    }

    private String extractJsonStringValue(String json, String key) {
        if (!StringUtils.hasText(json) || !StringUtils.hasText(key)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }
}
