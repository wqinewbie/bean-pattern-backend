package com.beanpattern.model.analytics;

import lombok.Data;

import java.util.List;

@Data
public class AnalyticsBatchRequest {
    private String sessionId;
    private List<AnalyticsEventRequest> events;
}
