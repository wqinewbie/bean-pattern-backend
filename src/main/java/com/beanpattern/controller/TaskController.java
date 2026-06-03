package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.BpUserGift;
import com.beanpattern.entity.UserTaskProgress;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 获取任务列表（聚合后的任务中心数据）。
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getTaskList(HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        List<TaskCenterItem> tasks = taskService.getTaskCenterItems(user.getId());
        return ApiResponse.ok(Map.of("tasks", tasks));
    }

    /**
     * 获取用户任务进度。
     * 兼容旧接口，仅返回仍使用 user_task_progress 的任务数据。
     */
    @GetMapping("/progress")
    public ApiResponse<List<UserTaskProgress>> getProgress(HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        List<UserTaskProgress> progress = taskService.getUserTaskProgress(user.getId());
        return ApiResponse.ok(progress);
    }

    /**
     * 完成任务进度（触发任务计数）。
     */
    @PostMapping("/complete")
    public ApiResponse<UserTaskProgress> completeTask(@RequestBody Map<String, String> body,
                                                      HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        String taskCode = body.get("taskCode");
        if (taskCode == null || taskCode.isEmpty()) {
            return ApiResponse.fail("任务编码不能为空");
        }

        UserTaskProgress progress = taskService.incrementTaskProgress(user.getId(), taskCode);
        return ApiResponse.ok(progress);
    }

    /**
     * 领取任务奖励。
     */
    @PostMapping("/claim")
    public ApiResponse<BpUserGift> claimReward(@RequestBody Map<String, Long> body,
                                             HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        Long progressId = body.get("progressId");
        if (progressId == null) {
            return ApiResponse.fail("进度ID不能为空");
        }
        return ApiResponse.ok(taskService.claimTaskReward(user.getId(), progressId));
    }

    /**
     * 领取资格型福利任务，当前支持首冲礼包。
     */
    @PostMapping("/claim-benefit")
    public ApiResponse<BpUserGift> claimBenefit(@RequestBody Map<String, String> body,
                                              HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        String taskCode = body.get("taskCode");
        if (taskCode == null || taskCode.isEmpty()) {
            return ApiResponse.fail("任务编码不能为空");
        }

        try {
            BpUserGift gift = taskService.claimBenefitGift(user.getId(), taskCode);
            return ApiResponse.ok(gift);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
