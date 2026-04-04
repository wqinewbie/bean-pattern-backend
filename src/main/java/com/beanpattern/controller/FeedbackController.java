package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.FeedbackEntity;
import com.beanpattern.mapper.FeedbackMapper;
import com.beanpattern.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 帮助与反馈接口
 * POST /api/feedback/submit - 提交反馈
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackMapper feedbackMapper;
    private final SessionHelper sessionHelper;

    public FeedbackController(FeedbackMapper feedbackMapper, SessionHelper sessionHelper) {
        this.feedbackMapper = feedbackMapper;
        this.sessionHelper = sessionHelper;
    }

    @PostMapping("/submit")
    public ApiResponse<String> submit(@RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        String content = body.getOrDefault("content", "").trim();
        if (!StringUtils.hasText(content)) {
            return ApiResponse.fail("反馈内容不能为空");
        }
        var user = sessionHelper.requireCompleteProfileUser(request);
        FeedbackEntity fb = new FeedbackEntity();
        fb.setUserId(user != null ? user.getId() : null);
        fb.setContent(content);
        fb.setCategory(body.getOrDefault("category", "OTHER"));
        fb.setContact(body.getOrDefault("contact", null));
        fb.setImages(body.getOrDefault("images", null));
        feedbackMapper.insert(fb);
        return ApiResponse.ok("ok");
    }
}
