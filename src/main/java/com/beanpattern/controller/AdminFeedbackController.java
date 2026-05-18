package com.beanpattern.controller;

import com.beanpattern.mapper.FeedbackMapper;
import com.beanpattern.model.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/feedback")
public class AdminFeedbackController {

    private final FeedbackMapper feedbackMapper;

    public AdminFeedbackController(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(feedbackMapper.listAll().stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("user", f.getUserId() != null ? "用户#" + f.getUserId() : "匿名");
            m.put("content", f.getContent());
            m.put("category", f.getCategory());
            m.put("status", f.getStatus());
            m.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<Void> close(@PathVariable Long id) {
        feedbackMapper.updateStatus(id, 3);
        return ApiResponse.ok(null);
    }
}
