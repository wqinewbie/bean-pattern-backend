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

/**
 * 任务记录查询接口（给小程序「历史记录」页用）。
 * GET /api/task/list - 分页获取当前用户任务历史
 */
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

    /**
     * 获取当前用户的任务历史列表（数据库分页）。
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数，最大 50
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {

        UserEntity user = sessionHelper.requireUser(request);
        int size   = Math.min(Math.max(pageSize, 1), 50);
        int offset = (Math.max(page, 1) - 1) * size;

        List<ImageTaskEntity> pageList = taskRecordService.listByUserPage(user.getId(), offset, size);
        int total = taskRecordService.countByUser(user.getId());

        return ApiResponse.ok(Map.of(
                "list",    pageList,
                "total",   total,
                "hasMore", offset + pageList.size() < total
        ));
    }
}
