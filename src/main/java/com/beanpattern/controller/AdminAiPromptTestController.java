package com.beanpattern.controller;

import com.beanpattern.controller.AiTaskController.AiGenerateRequest;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.AiTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai-prompt-test")
public class AdminAiPromptTestController {

    @Autowired
    private AiTaskService aiTaskService;

    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generate(@RequestBody AiGenerateRequest request) {
        String taskId = aiTaskService.createAdminPromptTestTask(request);
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("status", "PENDING");
        data.put("estimatedTime", 30);
        return ApiResponse.ok(data);
    }

    @GetMapping("/task/{taskId}")
    public ApiResponse<Map<String, Object>> getTask(@PathVariable String taskId) {
        return aiTaskService.getTaskStatus(taskId);
    }
}
