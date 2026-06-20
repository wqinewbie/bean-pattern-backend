package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.analytics.AnalyticsBatchRequest;
import com.beanpattern.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/events")
    public ApiResponse<Map<String, Object>> track(@RequestBody AnalyticsBatchRequest body,
                                                   HttpServletRequest request) {
        int accepted = analyticsService.accept(body, request);
        return ApiResponse.ok(Map.of("accepted", accepted));
    }
}
