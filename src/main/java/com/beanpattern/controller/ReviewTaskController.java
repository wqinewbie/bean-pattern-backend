package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.ReviewTaskSubmission;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.ReviewTaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 审核型任务接口。
 */
@RestController
@RequestMapping("/api/review-task")
public class ReviewTaskController {

    private final SessionHelper sessionHelper;
    private final ReviewTaskService reviewTaskService;

    public ReviewTaskController(SessionHelper sessionHelper, ReviewTaskService reviewTaskService) {
        this.sessionHelper = sessionHelper;
        this.reviewTaskService = reviewTaskService;
    }

    @GetMapping("/latest")
    public ApiResponse<ReviewTaskSubmission> latest(String taskCode, HttpServletRequest request) {
        Long userId = sessionHelper.requireUser(request).getId();
        return ApiResponse.ok(reviewTaskService.getLatestSubmission(userId, taskCode));
    }

    @PostMapping("/submit")
    public ApiResponse<ReviewTaskSubmission> submit(@RequestBody Map<String, String> body,
                                                    HttpServletRequest request) {
        Long userId = sessionHelper.requireUser(request).getId();
        try {
            ReviewTaskSubmission submission = reviewTaskService.submit(
                    userId,
                    body.get("taskCode"),
                    body.getOrDefault("submissionText", ""),
                    body.getOrDefault("proofImages", "")
            );
            return ApiResponse.ok(submission);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
