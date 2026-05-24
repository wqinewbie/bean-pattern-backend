package com.beanpattern.service;

import com.beanpattern.config.AppProperties;
import com.beanpattern.entity.UserEntity;
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

@Service
public class WechatSubscribeMessageService {

    private static final Logger log = LoggerFactory.getLogger(WechatSubscribeMessageService.class);

    private final AppProperties appProperties;
    private final WechatAuthService wechatAuthService;
    private final UserService userService;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public WechatSubscribeMessageService(AppProperties appProperties,
                                         WechatAuthService wechatAuthService,
                                         UserService userService) {
        this.appProperties = appProperties;
        this.wechatAuthService = wechatAuthService;
        this.userService = userService;
    }

    public void sendReviewTaskResult(Long userId, String taskCode, String result, String remark) {
        String templateId = appProperties.getWechat().getReviewTaskResultTemplateId();
        if (!StringUtils.hasText(templateId)) {
            log.info("[subscribe][skip] review task template id is empty");
            return;
        }
        UserEntity user = userService.getUserById(userId);
        if (user == null || !StringUtils.hasText(user.getOpenId())) {
            log.info("[subscribe][skip] user/openId missing userId={}", userId);
            return;
        }
        String accessToken = wechatAuthService.getAccessToken();
        if (!StringUtils.hasText(accessToken)) {
            log.warn("[subscribe][skip] access token missing");
            return;
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://api.weixin.qq.com/cgi-bin/message/subscribe/send")
                    .queryParam("access_token", accessToken)
                    .build(true)
                    .toUri();
            String userName = StringUtils.hasText(user.getNickName()) ? user.getNickName() : "用户" + userId;
            String json = "{"
                    + "\"touser\":\"" + escape(user.getOpenId()) + "\","
                    + "\"template_id\":\"" + escape(templateId) + "\","
                    + "\"page\":\"\","
                    + "\"data\":{"
                    + "\"thing1\":{\"value\":\"" + escape(limit(taskCode, 20)) + "\"},"
                    + "\"thing2\":{\"value\":\"" + escape(limit(remark, 20)) + "\"},"
                    + "\"thing3\":{\"value\":\"" + escape(limit(userName, 20)) + "\"},"
                    + "\"thing4\":{\"value\":\"" + escape(limit("请查看站内消息", 20)) + "\"}"
                    + "}"
                    + "}";
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("[subscribe][review-task] userId={}, status={}, body={}", userId, response.statusCode(), response.body());
        } catch (Exception e) {
            log.warn("[subscribe][review-task][failed] userId={}, msg={}", userId, e.getMessage());
        }
    }

    private String limit(String value, int max) {
        String text = StringUtils.hasText(value) ? value : "-";
        return text.length() > max ? text.substring(0, max) : text;
    }

    private String escape(String value) {
        return (value == null ? "" : value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
