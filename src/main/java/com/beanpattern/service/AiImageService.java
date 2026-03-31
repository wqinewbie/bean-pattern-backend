package com.beanpattern.service;

import com.beanpattern.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiImageService {

    private final AppProperties appProperties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AiImageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String processImage(String imageUrl) {
        String apiUrl = appProperties.getAi().getApiUrl();
        // 未配置 AI 地址时，直接返回原图 URL，确保前后端链路可跑通。
        if (!StringUtils.hasText(apiUrl)) {
            return imageUrl;
        }

        try {
            // 构造最小 JSON 请求体：{"imageUrl":"..."}
            String escapedUrl = imageUrl.replace("\\", "\\\\").replace("\"", "\\\"");
            String requestBody = "{\"imageUrl\":\"" + escapedUrl + "\"}";

            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));
            if (StringUtils.hasText(appProperties.getAi().getApiKey())) {
                builder.header("Authorization", "Bearer " + appProperties.getAi().getApiKey());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                // 兼容常见返回字段：resultUrl 或 url
                String resultUrl = extractJsonStringValue(response.body(), "resultUrl");
                if (!StringUtils.hasText(resultUrl)) {
                    resultUrl = extractJsonStringValue(response.body(), "url");
                }
                if (StringUtils.hasText(resultUrl)) {
                    return resultUrl;
                }
            }
        } catch (Exception ignored) {
            // AI 调用失败时降级返回原图，避免前端因异常中断。
        }
        return imageUrl;
    }

    private String extractJsonStringValue(String json, String key) {
        if (!StringUtils.hasText(json) || !StringUtils.hasText(key)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 文字生成图片：根据 prompt/style/size 生成拼豆图纸图片 URL。
     * 未配置 AI 时返回空字符串，由 Controller 降级处理。
     */
    public String generateFromText(String prompt, String style, int size) {
        String apiUrl = appProperties.getAi().getApiUrl();
        if (!StringUtils.hasText(apiUrl)) {
            return ""; // 未配置时返回空
        }
        try {
            String ep = prompt.replace("\\", "\\\\").replace("\"", "\\\"");
            String es = style.replace("\\", "\\\\").replace("\"", "\\\"");
            String body = "{\"prompt\":\"" + ep + "\",\"style\":\"" + es + "\",\"size\":" + size + "}";
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            if (StringUtils.hasText(appProperties.getAi().getApiKey())) {
                builder.header("Authorization", "Bearer " + appProperties.getAi().getApiKey());
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String resultUrl = extractJsonStringValue(response.body(), "resultUrl");
                if (!StringUtils.hasText(resultUrl)) resultUrl = extractJsonStringValue(response.body(), "url");
                if (StringUtils.hasText(resultUrl)) return resultUrl;
            }
        } catch (Exception ignored) {}
        return "";
    }
}
