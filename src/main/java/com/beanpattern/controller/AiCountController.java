package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.AiQuotaLog;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.AiQuotaLogService;
import com.beanpattern.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI次数管理控制器
 */
@RestController
@RequestMapping("/api/ai-count")
public class AiCountController {

    private final SessionHelper sessionHelper;
    private final UserService userService;
    private final AiQuotaLogService aiQuotaLogService;

    public AiCountController(SessionHelper sessionHelper,
                            UserService userService,
                            AiQuotaLogService aiQuotaLogService) {
        this.sessionHelper = sessionHelper;
        this.userService = userService;
        this.aiQuotaLogService = aiQuotaLogService;
    }

    /**
     * 获取用户AI次数信息
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getAiQuotaInfo(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);

        Integer aiQuota = user.getAiQuota() != null ? user.getAiQuota() : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("aiQuota", aiQuota);
        result.put("hasQuota", aiQuota > 0);

        return ApiResponse.ok(result);
    }

    /**
     * 使用AI次数
     */
    @PostMapping("/use")
    public ApiResponse<Map<String, Object>> useAiQuota(
            @RequestBody UseAiQuotaRequest request,
            HttpServletRequest httpRequest) {

        UserEntity user = sessionHelper.requireUser(httpRequest);

        boolean success = userService.useAiQuota(user.getId());

        if (!success) {
            return ApiResponse.fail(40001, "AI次数不足");
        }

        // 记录日志
        aiQuotaLogService.logChange(
            user.getId(),
            "USE",
            -1,
            request.getBizType() != null ? request.getBizType() : "AI_GENERATE",
            request.getBizId() != null ? request.getBizId() : "",
            "使用AI生成"
        );

        // 获取更新后的配额
        UserEntity updatedUser = userService.getUserById(user.getId());
        Integer remainingQuota = updatedUser.getAiQuota() != null ? updatedUser.getAiQuota() : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("remainingQuota", remainingQuota);

        return ApiResponse.ok(result);
    }

    /**
     * 获取AI次数使用记录
     */
    @GetMapping("/logs")
    public ApiResponse<Map<String, Object>> getAiQuotaLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {

        UserEntity user = sessionHelper.requireUser(request);

        List<AiQuotaLog> logs = aiQuotaLogService.getUserLogs(user.getId(), page, pageSize);
        int total = aiQuotaLogService.getUserLogsCount(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return ApiResponse.ok(result);
    }

    /**
     * 使用AI次数请求参数
     */
    public static class UseAiQuotaRequest {
        private String bizType;
        private String bizId;

        public String getBizType() {
            return bizType;
        }

        public void setBizType(String bizType) {
            this.bizType = bizType;
        }

        public String getBizId() {
            return bizId;
        }

        public void setBizId(String bizId) {
            this.bizId = bizId;
        }
    }
}
