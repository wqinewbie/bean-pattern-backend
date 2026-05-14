package com.beanpattern.controller;

import com.beanpattern.entity.CheckinConfig;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.CheckinService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 签到管理后台 Controller
 */
@RestController
@RequestMapping("/api/admin/checkin")
public class AdminCheckinController {

    private final CheckinService checkinService;

    public AdminCheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    /**
     * 获取签到配置
     */
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getConfig() {
        Map<String, Object> config = checkinService.getCheckinConfig();
        return ApiResponse.ok(config);
    }

    /**
     * 保存签到配置
     */
    @PostMapping("/config")
    public ApiResponse<Map<String, Object>> saveConfig(@RequestBody CheckinConfig config) {
        checkinService.saveConfig(config);
        return ApiResponse.ok(checkinService.getCheckinConfig());
    }

    /**
     * 获取签到统计
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        return ApiResponse.ok(checkinService.getAdminStatistics());
    }

    /**
     * 获取最近签到记录
     */
    @GetMapping("/recent")
    public ApiResponse<Map<String, Object>> getRecentCheckins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = checkinService.getAdminRecentCheckins(
                page,
                pageSize,
                parseDate(startDate),
                parseDate(endDate),
                keyword
        );
        return ApiResponse.ok(result);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }
}
