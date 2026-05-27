package com.beanpattern.controller;

import com.beanpattern.config.AiServiceProperties;
import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.UnauthorizedException;
import com.beanpattern.service.AiQuotaLogService;
import com.beanpattern.service.AiTaskService;
import com.beanpattern.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI生成任务控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiTaskController {

    @Autowired
    private AiTaskService aiTaskService;

    @Autowired
    private UserService userService;

    @Autowired
    private AiQuotaLogService aiQuotaLogService;

    @Autowired
    private SessionHelper sessionHelper;

    @Autowired
    private AiServiceProperties aiServiceProperties;

    /**
     * 创建AI生成任务
     */
    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generate(
            @RequestBody AiGenerateRequest request,
            HttpServletRequest httpRequest) {

        UserEntity user = sessionHelper.requireUser(httpRequest);

        boolean success = userService.useAiQuota(user.getId());
        if (!success) {
            return ApiResponse.fail(40001, "AI次数不足");
        }

        String taskId;
        try {
            taskId = aiTaskService.createTask(request, user.getId());

            boolean logged = aiQuotaLogService.tryLogChange(
                user.getId(),
                "USE",
                -1,
                "AI_GENERATE",
                taskId,
                "使用AI生成"
            );
            if (!logged) {
                throw new IllegalStateException("AI次数扣减日志重复");
            }
        } catch (Exception e) {
            userService.addAiQuota(user.getId(), 1);
            aiQuotaLogService.tryLogChange(
                user.getId(),
                "REFUND",
                1,
                "AI_GENERATE_CREATE_FAILED",
                String.valueOf(System.currentTimeMillis()),
                "AI任务创建失败，返还次数"
            );
            throw e;
        }

        return ApiResponse.ok(Map.of(
            "taskId", taskId,
            "status", "PENDING",
            "estimatedTime", 30
        ));
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/task/{taskId}")
    public ApiResponse<Map<String, Object>> getTask(@PathVariable String taskId) {
        return aiTaskService.getTaskStatus(taskId);
    }

    /**
     * AI服务回调接口
     */
    @PostMapping("/task/callback")
    public ApiResponse<Void> taskCallback(@RequestBody Map<String, Object> body,
                                          @RequestHeader(value = "X-AI-Service-Token", required = false) String token) {
        if (!aiServiceProperties.getCallbackToken().equals(token)) {
            throw new UnauthorizedException("invalid ai service token");
        }

        String taskId = (String) body.get("taskId");
        String status = (String) body.get("status");
        String aiImageUrl = (String) body.get("aiImageUrl");
        String errorMessage = (String) body.get("errorMessage");

        aiTaskService.updateTaskStatus(taskId, status, aiImageUrl, errorMessage);

        return ApiResponse.ok(null);
    }

    /**
     * AI生成请求参数
     */
    public static class AiGenerateRequest {
        private String imageUrl;
        private String prompt;
        private String style;
        private String sizeMode;
        private String brand;
        private Integer colorCount;
        private Boolean mirror;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public String getSizeMode() {
            return sizeMode;
        }

        public void setSizeMode(String sizeMode) {
            this.sizeMode = sizeMode;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public Integer getColorCount() {
            return colorCount;
        }

        public void setColorCount(Integer colorCount) {
            this.colorCount = colorCount;
        }

        public Boolean getMirror() {
            return mirror;
        }

        public void setMirror(Boolean mirror) {
            this.mirror = mirror;
        }
    }
}
