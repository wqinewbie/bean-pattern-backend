package com.beanpattern.service;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.AnalyticsEvent;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.AnalyticsEventMapper;
import com.beanpattern.model.analytics.AnalyticsBatchRequest;
import com.beanpattern.model.analytics.AnalyticsEventRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AnalyticsService {

    private static final int MAX_BATCH_SIZE = 20;
    private static final int MAX_PARAMS_LENGTH = 8192;
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Set<String> EVENT_NAMES = Set.of(
            "page_home_view", "page_convert_view", "page_ai_generate_view", "page_preview_view",
            "page_draw_view", "page_focus_view", "page_pattern_box_view", "page_history_view",
            "page_draft_view", "page_profile_view", "convert_image_upload_click",
            "convert_image_upload_result", "convert_param_confirm", "convert_generate_result",
            "ai_image_upload_result", "ai_generate_click", "ai_generate_result", "preview_tab_switch",
            "preview_edit_click", "preview_save_click", "draw_tool_use", "draw_save_result",
            "focus_progress_mark", "focus_progress_save_result", "quota_panel_view",
            "quota_empty_panel_view", "recharge_center_view", "vip_product_click",
            "card_product_click", "payment_create_result", "payment_result", "rewarded_ad_click",
            "rewarded_ad_result", "task_panel_view", "pattern_box_save_result",
            "draft_save_result", "capacity_limit_hit", "pattern_delete_result",
            "history_restore_click", "share_initiate", "share_visit", "invite_bind_result",
            "reward_grant_result", "notification_open"
    );

    private final SessionHelper sessionHelper;
    private final AnalyticsEventWriter analyticsEventWriter;
    private final AnalyticsEventMapper analyticsEventMapper;
    private final ObjectMapper objectMapper;

    public AnalyticsService(SessionHelper sessionHelper,
                            AnalyticsEventWriter analyticsEventWriter,
                            AnalyticsEventMapper analyticsEventMapper,
                            ObjectMapper objectMapper) {
        this.sessionHelper = sessionHelper;
        this.analyticsEventWriter = analyticsEventWriter;
        this.analyticsEventMapper = analyticsEventMapper;
        this.objectMapper = objectMapper;
    }

    public int accept(AnalyticsBatchRequest body, HttpServletRequest request) {
        if (body == null || body.getEvents() == null || body.getEvents().isEmpty()) {
            return 0;
        }
        String sessionId = normalize(body.getSessionId(), 64);
        if (!StringUtils.hasText(sessionId)) {
            sessionId = "anonymous";
        }
        UserEntity user = sessionHelper.resolveUser(request);
        Long userId = user != null ? user.getId() : null;

        List<AnalyticsEvent> events = new ArrayList<>();
        int limit = Math.min(body.getEvents().size(), MAX_BATCH_SIZE);
        for (int i = 0; i < limit; i++) {
            AnalyticsEvent event = normalizeEvent(body.getEvents().get(i), sessionId, userId);
            if (event != null) {
                events.add(event);
            }
        }
        if (!events.isEmpty()) {
            analyticsEventWriter.saveEventsAsync(events);
        }
        return events.size();
    }

    public Map<String, Object> summary(LocalDate startDate, LocalDate endDate) {
        DateRange range = toRange(startDate, endDate);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("eventCount", analyticsEventMapper.countAll(range.startAt, range.endAt));
        result.put("userCount", analyticsEventMapper.countUsers(range.startAt, range.endAt));
        result.put("sessionCount", analyticsEventMapper.countSessions(range.startAt, range.endAt));
        result.put("aiGenerateSuccess", analyticsEventMapper.countByEventAndResult("ai_generate_result", "success", range.startAt, range.endAt));
        result.put("paymentSuccess", analyticsEventMapper.countByEventAndResult("payment_result", "success", range.startAt, range.endAt));
        result.put("shareVisit", analyticsEventMapper.countByEvent("share_visit", range.startAt, range.endAt));
        return result;
    }

    public Map<String, Object> listEvents(String eventName, Long userId, String page, LocalDate startDate,
                                          LocalDate endDate, int pageNo, int pageSize) {
        DateRange range = toRange(startDate, endDate);
        int safePage = Math.max(pageNo, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePage - 1) * safePageSize;
        List<AnalyticsEvent> list = analyticsEventMapper.list(eventName, userId, page, range.startAt, range.endAt, safePageSize, offset);
        long total = analyticsEventMapper.countList(eventName, userId, page, range.startAt, range.endAt);
        return Map.of("list", list, "total", total, "page", safePage, "pageSize", safePageSize);
    }

    public Map<String, Object> chart(String chartKey, String eventName, LocalDate startDate, LocalDate endDate) {
        DateRange range = toRange(startDate, endDate);
        String key = StringUtils.hasText(chartKey) ? chartKey : "overview_metrics";
        return switch (key) {
            case "event_trend" -> lineChart(key, "事件趋势", eventNameOrDefault(eventName), range);
            case "page_view_rank" -> pairChart(key, "bar", "页面访问排行", analyticsEventMapper.pageRank(range.startAt, range.endAt, 20));
            case "free_convert_funnel" -> funnelChart(key, "免费转图纸漏斗", range,
                    List.of(step("浏览图片转图纸页", "page_convert_view", null),
                            step("点击上传", "convert_image_upload_click", null),
                            step("上传成功", "convert_image_upload_result", "success"),
                            step("确认参数", "convert_param_confirm", null),
                            step("生成成功", "convert_generate_result", "success"),
                            step("预览", "page_preview_view", null)));
            case "ai_generate_funnel" -> funnelChart(key, "AI 生成漏斗", range,
                    List.of(step("浏览AI页", "page_ai_generate_view", null),
                            step("上传成功", "ai_image_upload_result", "success"),
                            step("点击生成", "ai_generate_click", null),
                            step("生成成功", "ai_generate_result", "success"),
                            step("预览", "page_preview_view", null)));
            case "ai_payment_funnel" -> funnelChart(key, "AI 付费漏斗", range,
                    List.of(step("次数不足曝光", "quota_empty_panel_view", null),
                            step("浏览充值中心", "recharge_center_view", null),
                            step("创建支付单成功", "payment_create_result", "success"),
                            step("支付成功", "payment_result", "success")));
            case "invite_funnel" -> funnelChart(key, "邀请增长漏斗", range,
                    List.of(step("发起分享", "share_initiate", null),
                            step("分享访问", "share_visit", null),
                            step("绑定成功", "invite_bind_result", "success"),
                            step("奖励到账", "reward_grant_result", "success")));
            case "source_distribution" -> pairChart(key, "pie", "图纸来源占比",
                    analyticsEventMapper.patternSourceDistribution(range.startAt, range.endAt));
            case "failure_reason_rank" -> pairChart(key, "bar", "失败原因排行",
                    analyticsEventMapper.failureReasonRank(range.startAt, range.endAt, 20));
            case "capacity_conversion" -> funnelChart(key, "容量触发转化", range,
                    List.of(step("触发容量上限", "capacity_limit_hit", null),
                            step("浏览充值中心", "recharge_center_view", null),
                            step("支付成功", "payment_result", "success")));
            case "focus_retention" -> funnelChart(key, "沉浸式拼豆使用", range,
                    List.of(step("进入沉浸式", "page_focus_view", null),
                            step("标记进度", "focus_progress_mark", null),
                            step("保存进度成功", "focus_progress_save_result", "success")));
            default -> Map.of("chartKey", "overview_metrics", "chartType", "metrics", "title", "核心指标", "summary", summary(startDate, endDate));
        };
    }

    private AnalyticsEvent normalizeEvent(AnalyticsEventRequest input, String sessionId, Long userId) {
        if (input == null || !StringUtils.hasText(input.getEventName()) || !EVENT_NAMES.contains(input.getEventName())) {
            return null;
        }
        String clientEventId = normalize(input.getClientEventId(), 64);
        if (!StringUtils.hasText(clientEventId)) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventTime = input.getEventTime() != null && input.getEventTime() > 0
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(input.getEventTime()), ZONE)
                : now;
        Map<String, Object> params = input.getParams() != null ? new LinkedHashMap<>(input.getParams()) : new LinkedHashMap<>();
        putIfAbsent(params, "ai_quota", input.getAiQuota());
        String paramsJson = toJson(params);
        if (paramsJson.length() > MAX_PARAMS_LENGTH) {
            paramsJson = "{}";
        }
        return AnalyticsEvent.builder()
                .clientEventId(clientEventId)
                .eventName(normalize(input.getEventName(), 64))
                .eventTime(eventTime)
                .serverTime(now)
                .userId(userId)
                .sessionId(sessionId)
                .page(defaultString(normalize(input.getPage(), 128), "unknown"))
                .referPage(normalize(input.getReferPage(), 128))
                .source(firstString(input.getSource(), params.get("source"), 64))
                .isLogin(input.getIsLogin())
                .isVip(input.getIsVip())
                .vipLevel(normalize(input.getVipLevel(), 32))
                .patternId(firstString(input.getPatternId(), params.get("pattern_id"), 64))
                .patternSource(firstString(input.getPatternSource(), params.get("pattern_source"), 32))
                .result(firstString(input.getResult(), params.get("result"), 32))
                .failReason(firstString(input.getFailReason(), params.get("fail_reason"), 64))
                .paramsJson(paramsJson)
                .build();
    }

    private Map<String, Object> lineChart(String key, String title, String eventName, DateRange range) {
        List<Map<String, Object>> rows = analyticsEventMapper.trendByDay(eventName, range.startAt, range.endAt);
        List<Object> xAxis = rows.stream().map(row -> row.get("label")).toList();
        List<Object> data = rows.stream().map(row -> row.get("value")).toList();
        return Map.of("chartKey", key, "chartType", "line", "title", title,
                "xAxis", xAxis, "series", List.of(Map.of("name", eventName, "data", data)));
    }

    private Map<String, Object> pairChart(String key, String type, String title, List<Map<String, Object>> rows) {
        List<Map<String, Object>> series = rows.stream()
                .map(row -> Map.of("name", row.get("label"), "value", row.get("value")))
                .toList();
        return Map.of("chartKey", key, "chartType", type, "title", title, "series", series);
    }

    private Map<String, Object> funnelChart(String key, String title, DateRange range, List<FunnelStep> steps) {
        List<Map<String, Object>> series = new ArrayList<>();
        long firstValue = 0;
        for (int i = 0; i < steps.size(); i++) {
            FunnelStep step = steps.get(i);
            long value = step.result == null
                    ? analyticsEventMapper.countByEvent(step.eventName, range.startAt, range.endAt)
                    : analyticsEventMapper.countByEventAndResult(step.eventName, step.result, range.startAt, range.endAt);
            if (i == 0) firstValue = value;
            double rate = firstValue > 0 ? (double) value / firstValue : 0;
            series.add(Map.of("name", step.name, "value", value, "rate", rate));
        }
        return Map.of("chartKey", key, "chartType", "funnel", "title", title, "series", series);
    }

    private DateRange toRange(LocalDate startDate, LocalDate endDate) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(6);
        if (start.isAfter(end)) {
            start = end;
        }
        return new DateRange(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private String toJson(Map<String, Object> params) {
        try {
            return objectMapper.writeValueAsString(params != null ? params : Map.of());
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private void putIfAbsent(Map<String, Object> params, String key, Object value) {
        if (value != null && !params.containsKey(key)) {
            params.put(key, value);
        }
    }

    private String normalize(String value, int maxLength) {
        if (!StringUtils.hasText(value)) return null;
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

    private String firstString(String value, Object fallback, int maxLength) {
        if (StringUtils.hasText(value)) return normalize(value, maxLength);
        if (fallback == null) return null;
        return normalize(String.valueOf(fallback), maxLength);
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String eventNameOrDefault(String eventName) {
        return EVENT_NAMES.contains(eventName) ? eventName : "page_home_view";
    }

    private FunnelStep step(String name, String eventName, String result) {
        return new FunnelStep(name, eventName, result);
    }

    private record DateRange(LocalDateTime startAt, LocalDateTime endAt) {}
    private record FunnelStep(String name, String eventName, String result) {}
}
