package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.ImageTaskEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.TaskRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Web 端图纸相关接口
 * GET  /api/pattern/list          - 获取我的图纸列表
 * POST /api/pattern/save          - 保存图纸
 * DELETE /api/pattern/{id}        - 删除图纸
 * GET  /api/pattern/{id}          - 获取图纸详情
 */
@RestController
@RequestMapping("/api/pattern")
public class PatternController {

    private final SessionHelper sessionHelper;
    private final TaskRecordService taskRecordService;

    public PatternController(SessionHelper sessionHelper,
                             TaskRecordService taskRecordService) {
        this.sessionHelper = sessionHelper;
        this.taskRecordService = taskRecordService;
    }

    /** 获取当前用户的图纸列表 */
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest request) {
        var user = sessionHelper.resolveUser(request);
        if (user == null) return ApiResponse.ok(List.of());
        var tasks = taskRecordService.listByUser(user.getId(), limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ImageTaskEntity t : tasks) {
            if (!TaskRecordService.STATUS_SUCCESS.equals(t.getStatus())) continue;
            result.add(taskToMap(t));
        }
        return ApiResponse.ok(result);
    }

    /** 保存图纸（前端生成完毕后调用） */
    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        var user = sessionHelper.resolveUser(request);
        String sourceUrl   = (String) body.getOrDefault("sourceUrl",   "");
        String resultUrl   = (String) body.getOrDefault("resultUrl",   "");
        String patternUrl  = (String) body.getOrDefault("patternUrl",  "");
        String colorStats  = (String) body.getOrDefault("colorStats",  "");
        long userId = user != null ? user.getId() : -1L;
        var task = taskRecordService.createTask(userId, TaskRecordService.TASK_BEAD_LOCAL, sourceUrl);
        taskRecordService.markSuccess(task.getId(), resultUrl, patternUrl, colorStats);
        // reload to get full entity with timestamps
        var saved = taskRecordService.findById(task.getId());
        return ApiResponse.ok(taskToMap(saved));
    }

    /** 删除图纸 */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(
            @PathVariable("id") Long id,
            HttpServletRequest request) {
        var user = sessionHelper.resolveUser(request);
        var task = taskRecordService.findById(id);
        if (task == null) return ApiResponse.fail("图纸不存在");
        if (user == null || !task.getUserId().equals(user.getId())) {
            return ApiResponse.fail("无权限");
        }
        taskRecordService.markFailed(id, "deleted");
        return ApiResponse.ok("ok");
    }

    /** 获取单张图纸详情 */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(
            @PathVariable("id") Long id,
            HttpServletRequest request) {
        var task = taskRecordService.findById(id);
        if (task == null) return ApiResponse.fail("图纸不存在");
        return ApiResponse.ok(taskToMap(task));
    }

    private Map<String, Object> taskToMap(ImageTaskEntity t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",         t.getId().toString());
        m.put("sourceUrl",  t.getSourceUrl()  != null ? t.getSourceUrl()  : "");
        m.put("resultUrl",  t.getResultUrl()  != null ? t.getResultUrl()  : "");
        m.put("patternUrl", t.getPatternUrl() != null ? t.getPatternUrl() : "");
        m.put("colorStats", t.getColorStats() != null ? t.getColorStats() : "");
        m.put("createdAt",  t.getCreatedAt()  != null ? t.getCreatedAt().toString() : "");
        return m;
    }
}
