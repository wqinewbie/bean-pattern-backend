package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.AiTaskService;
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

    /**
     * 创建AI生成任务
     *
     * @param request 请求参数
     * @param sessionId 会话ID
     * @return 任务信息
     */
    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generate(
            @RequestBody AiGenerateRequest request,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        // 获取当前用户ID（从session或token）
        Long userId = getCurrentUserId(sessionId);

        // 创建Mock任务
        String taskId = aiTaskService.createMockTask(request, userId);

        return ApiResponse.ok(Map.of(
            "taskId", taskId,
            "status", "PENDING",
            "estimatedTime", 30
        ));
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    @GetMapping("/task/{taskId}")
    public ApiResponse<Map<String, Object>> getTask(@PathVariable String taskId) {
        return aiTaskService.getTaskStatus(taskId);
    }

    /**
     * AI服务回调接口（正式版使用）
     *
     * @param body 回调数据
     * @return 处理结果
     */
    @PostMapping("/task/callback")
    public ApiResponse<Void> taskCallback(@RequestBody Map<String, Object> body) {
        String taskId = (String) body.get("taskId");
        String status = (String) body.get("status");
        String aiImageUrl = (String) body.get("aiImageUrl");
        String errorMessage = (String) body.get("errorMessage");

        aiTaskService.updateTaskStatus(taskId, status, aiImageUrl, errorMessage);

        return ApiResponse.ok();
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(String sessionId) {
        // TODO: 从session或token获取真实的userId
        // 临时返回固定值用于测试
        return 1L;
    }

    /**
     * AI生成请求参数
     */
    public static class AiGenerateRequest {
        private String prompt;
        private String style;
        private Integer size;
        private String brand;
        private Integer colorCount;

        // Getters and Setters
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

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
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
    }
}
