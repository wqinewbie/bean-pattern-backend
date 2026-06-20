package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(analyticsService.summary(startDate, endDate));
    }

    @GetMapping("/events")
    public ApiResponse<Map<String, Object>> events(@RequestParam(defaultValue = "") String eventName,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(defaultValue = "") String page,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                    @RequestParam(defaultValue = "1") int pageNo,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(analyticsService.listEvents(eventName, userId, page, startDate, endDate, pageNo, pageSize));
    }

    @GetMapping("/charts")
    public ApiResponse<Map<String, Object>> chart(@RequestParam(defaultValue = "overview_metrics") String chartKey,
                                                   @RequestParam(defaultValue = "") String eventName,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(analyticsService.chart(chartKey, eventName, startDate, endDate));
    }
}
