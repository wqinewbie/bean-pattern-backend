package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserGift;
import com.beanpattern.entity.UserTaskProgress;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务中心 Controller
 */
@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final SessionHelper sessionHelper;
    private final TaskService taskService;

    public TaskController(SessionHelper sessionHelper, TaskService taskService) {
        this.sessionHelper = sessionHelper;
        this.taskService = taskService;
    }

    /**
     * 获取任务列表（带用户进度）
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getTaskList(HttpServletRequest request) {
        List<TaskConfig> configs = taskService.getAllActiveTasks();
        return ApiResponse.ok(Map.of("tasks", configs));
    }

    /**
     * 获取用户任务进度
     */
    @GetMapping("/progress")
    public ApiResponse<List<UserTaskProgress>> getProgress(HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        List<UserTaskProgress> progress = taskService.getUserTaskProgress(user.getId());
        return ApiResponse.ok(progress);
    }

    /**
     * 完成任务进度（触发任务计数）
     */
    @PostMapping("/complete")
    public ApiResponse<UserTaskProgress> completeTask(@RequestBody Map<String, String> body,
                                                      HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        String taskCode = body.get("taskCode");
        if (taskCode == null || taskCode.isEmpty()) {
            return ApiResponse.fail("任务编码不能为空");
        }

        try {
            UserTaskProgress progress = taskService.incrementTaskProgress(user.getId(), taskCode);
            return ApiResponse.ok(progress);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 领取任务奖励
     */
    @PostMapping("/claim")
    public ApiResponse<UserGift> claimReward(@RequestBody Map<String, Long> body,
                                            HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        Long progressId = body.get("progressId");
        if (progressId == null) {
            return ApiResponse.fail("进度ID不能为空");
        }

        try {
            UserGift gift = taskService.claimTaskReward(user.getId(), progressId);
            return ApiResponse.ok(gift);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
