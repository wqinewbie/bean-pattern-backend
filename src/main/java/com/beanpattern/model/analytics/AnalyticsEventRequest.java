package com.beanpattern.model.analytics;

import lombok.Data;

import java.util.Map;

@Data
public class AnalyticsEventRequest {
    private String clientEventId;
    private String eventName;
    private Long eventTime;
    private String page;
    private String referPage;
    private Boolean isLogin;
    private Boolean isVip;
    private String vipLevel;
    private Integer aiQuota;
    private String source;
    private String patternId;
    private String patternSource;
    private String result;
    private String failReason;
    private Map<String, Object> params;
}
