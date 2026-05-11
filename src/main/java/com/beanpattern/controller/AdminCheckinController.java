package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.CheckinService;
import org.springframework.web.bind.annotation.*;

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
     * 注意：当前配置是硬编码在 CheckinService 中的
     * 如果需要动态配置，需要创建配置表并实现相应的保存逻辑
     */
    @PostMapping("/config")
    public ApiResponse<String> saveConfig(@RequestBody Map<String, Object> config) {
        // TODO: 实现配置保存逻辑
        // 1. 创建 bp_checkin_config 表
        // 2. 保存配置到数据库
        // 3. CheckinService 从数据库读取配置
        return ApiResponse.ok("配置保存成功（当前为演示模式，实际未保存）");
    }

    /**
     * 获取签到统计
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // TODO: 实现统计逻辑
        // 1. 统计今日签到人数
        // 2. 统计累计签到用户数
        // 3. 统计今日领取奖励次数
        // 4. 统计累计发放奖励
        Map<String, Object> statistics = Map.of(
            "todayCheckinCount", 0,
            "totalCheckinUsers", 0,
            "todayClaimCount", 0,
            "totalRewardValue", 0
        );
        return ApiResponse.ok(statistics);
    }

    /**
     * 获取最近签到记录
     */
    @GetMapping("/recent")
    public ApiResponse<Map<String, Object>> getRecentCheckins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        // TODO: 实现分页查询逻辑
        // 1. 查询最近的签到记录
        // 2. 关联用户信息
        // 3. 返回分页数据
        Map<String, Object> result = Map.of(
            "list", java.util.Collections.emptyList(),
            "total", 0
        );
        return ApiResponse.ok(result);
    }
}
