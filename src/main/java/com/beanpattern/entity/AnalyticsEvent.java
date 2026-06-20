package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Analytics event raw log: bp_analytics_event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {
    private Long id;
    private String clientEventId;
    private String eventName;
    private LocalDateTime eventTime;
    private LocalDateTime serverTime;
    private Long userId;
    private String sessionId;
    private String page;
    private String referPage;
    private String source;
    private Boolean isLogin;
    private Boolean isVip;
    private String vipLevel;
    private String patternId;
    private String patternSource;
    private String result;
    private String failReason;
    private String paramsJson;
    private LocalDateTime createdAt;
}
