package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.ImageTaskEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.TaskRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 我的图纸接口（需登录，未登录返回 HTTP 401）
 * POST /api/my-pattern/save/{taskId}    - 保存到我的图纸
 * POST /api/my-pattern/unsave/{taskId}  - 取消保存
 * GET  /api/my-pattern/list             - 获取我的图纸列表
 */
@RestController
@RequestMapping("/api/my-pattern")
public class MyPatternController {

    private final SessionHelper sessionHelper;
    private final TaskRecordService taskRecordService;

    public MyPatternController(SessionHelper sessionHelper,
                               TaskRecordService taskRecordService) {
        this.sessionHelper = sessionHelper;
        this.taskRecordService = taskRecordService;
    }

    /** 保存到我的图纸 */
    @PostMapping("/save/{taskId}")
    public ApiResponse<String> save(@PathVariable Long taskId,
                                    HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        var task = taskRecordService.findById(taskId);
        if (task == null || !task.getUserId().equals(user.getId()))
            return ApiResponse.fail("图纸不存在");
        taskRecordService.markSaved(taskId, true);
        return ApiResponse.ok("ok");
    }

    /** 取消保存 */
    @PostMapping("/unsave/{taskId}")
    public ApiResponse<String> unsave(@PathVariable Long taskId,
                                      HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        var task = taskRecordService.findById(taskId);
        if (task == null || !task.getUserId().equals(user.getId()))
            return ApiResponse.fail("图纸不存在");
        taskRecordService.markSaved(taskId, false);
        return ApiResponse.ok("ok");
    }

    /** 获取我的图纸列表 */
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        var tasks = taskRecordService.listSavedByUser(user.getId(), limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ImageTaskEntity t : tasks) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("taskId",    t.getId().toString());
            m.put("sourceUrl", t.getSourceUrl()  != null ? t.getSourceUrl()  : "");
            m.put("resultUrl", t.getResultUrl()  != null ? t.getResultUrl()  : "");
            m.put("patternUrl",t.getPatternUrl() != null ? t.getPatternUrl() : "");
            m.put("colorStats",t.getColorStats() != null ? t.getColorStats() : "");
            m.put("createdAt", t.getCreatedAt()  != null ? t.getCreatedAt().toString() : "");
            result.add(m);
        }
        return ApiResponse.ok(result);
    }
}
