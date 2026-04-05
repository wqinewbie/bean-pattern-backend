package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.ImageTaskEntity;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.TaskRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final SessionHelper sessionHelper;
    private final TaskRecordService taskRecordService;

    public TaskController(SessionHelper sessionHelper,
                          TaskRecordService taskRecordService) {
        this.sessionHelper = sessionHelper;
        this.taskRecordService = taskRecordService;
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {

        UserEntity user = sessionHelper.requireUser(request);
        int size = Math.min(Math.max(pageSize, 1), 50);
        int offset = (Math.max(page, 1) - 1) * size;

        List<ImageTaskEntity> pageList = taskRecordService.listByUserPage(user.getId(), offset, size);
        int total = taskRecordService.countByUser(user.getId());

        return ApiResponse.ok(Map.of(
                "list", pageList,
                "total", total,
                "hasMore", offset + pageList.size() < total
        ));
    }
}
