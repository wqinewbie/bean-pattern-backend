package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @deprecated 旧任务接口，已废弃。请使用 /api/box/list 代替
 */
@RestController
@RequestMapping("/api/task")
@Deprecated
public class TaskController {

    private final SessionHelper sessionHelper;

    public TaskController(SessionHelper sessionHelper) {
        this.sessionHelper = sessionHelper;
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(HttpServletRequest request) {
        // 旧接口已废弃，返回空列表
        return ApiResponse.ok(Map.of(
                "list", List.of(),
                "total", 0,
                "hasMore", false
        ));
    }
}
