package com.beanpattern.controller;

import com.beanpattern.entity.ReviewTaskSubmission;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.mapper.TaskConfigMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.ReviewTaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminTaskController {

    private final TaskConfigMapper taskConfigMapper;
    private final ReviewTaskService reviewTaskService;

    public AdminTaskController(TaskConfigMapper taskConfigMapper, ReviewTaskService reviewTaskService) {
        this.taskConfigMapper = taskConfigMapper;
        this.reviewTaskService = reviewTaskService;
    }

    // ─── 任务中心 ───

    @GetMapping("/tasks")
    public ApiResponse<List<TaskConfig>> list() {
        return ApiResponse.ok(taskConfigMapper.findAll());
    }

    @PostMapping("/tasks")
    public ApiResponse<Void> create(@RequestBody TaskConfig task) {
        if (task.getTaskCode() == null || task.getTaskCode().trim().isEmpty())
            return ApiResponse.fail("任务代码不能为空");
        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty())
            return ApiResponse.fail("任务名称不能为空");
        if (taskConfigMapper.findByCode(task.getTaskCode()) != null)
            return ApiResponse.fail("任务代码已存在");
        String error = validate(task);
        if (error != null) return ApiResponse.fail(error);
        taskConfigMapper.insert(task);
        return ApiResponse.ok(null);
    }

    @PutMapping("/tasks/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody TaskConfig task) {
        if (taskConfigMapper.findById(id) == null) return ApiResponse.fail("任务不存在");
        task.setId(id);
        String error = validate(task);
        if (error != null) return ApiResponse.fail(error);
        taskConfigMapper.update(task);
        return ApiResponse.ok(null);
    }

    @PutMapping("/tasks/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        if (taskConfigMapper.findById(id) == null) return ApiResponse.fail("任务不存在");
        taskConfigMapper.updateStatus(id, isActive);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/tasks/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (taskConfigMapper.findById(id) == null) return ApiResponse.fail("任务不存在");
        taskConfigMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    private String validate(TaskConfig task) {
        if (task.getExtraConfig() == null || task.getExtraConfig().trim().isEmpty())
            return "任务必须配置奖励礼品包";
        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(task.getExtraConfig());
            String handlerType = node.path("handlerType").asText("GENERIC_PROGRESS");
            boolean needsPackage = !"CHECKIN".equals(handlerType);
            String packageCode = node.path("giftPackageCode").asText("").trim();
            if (needsPackage && packageCode.isEmpty()) return "奖励型任务必须绑定礼品包";
        } catch (Exception e) {
            return "任务扩展配置不是有效JSON";
        }
        return null;
    }

    // ─── 审核任务 ───

    @GetMapping("/review-tasks/submissions")
    public ApiResponse<List<ReviewTaskSubmission>> submissions(@RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.ok(reviewTaskService.listLatest(limit));
    }

    @PostMapping("/review-tasks/submissions/{id}/approve")
    public ApiResponse<ReviewTaskSubmission> approve(@PathVariable Long id,
                                                      @RequestBody(required = false) Map<String, String> body,
                                                      HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        return ApiResponse.ok(reviewTaskService.review(id, 1, adminId, body == null ? "" : body.getOrDefault("reviewRemark", "")));
    }

    @PostMapping("/review-tasks/submissions/{id}/reject")
    public ApiResponse<ReviewTaskSubmission> reject(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body,
                                                     HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        return ApiResponse.ok(reviewTaskService.review(id, 2, adminId, body.getOrDefault("reviewRemark", "")));
    }
}
