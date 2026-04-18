package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.BpBox;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.BpBoxService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图纸箱接口
 * 存储用户的成品图纸
 */
@RestController
@RequestMapping("/api/box")
public class BpBoxController {

    private final BpBoxService bpBoxService;
    private final SessionHelper sessionHelper;

    public BpBoxController(BpBoxService bpBoxService, SessionHelper sessionHelper) {
        this.bpBoxService = bpBoxService;
        this.sessionHelper = sessionHelper;
    }

    /**
     * POST /api/box/save
     * 保存图纸到图纸箱
     */
    @PostMapping("/save")
    public ApiResponse<BpBox> save(@RequestBody BpBox box, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        box.setUserId(user.getId());
        bpBoxService.save(box);
        return ApiResponse.ok(box);
    }

    /**
     * GET /api/box/list
     * 获取图纸箱列表
     */
    @GetMapping("/list")
    public ApiResponse<List<BpBox>> list(HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        List<BpBox> list = bpBoxService.listByUserId(user.getId());
        return ApiResponse.ok(list);
    }

    /**
     * GET /api/box/detail/{id}
     * 获取图纸详情
     */
    @GetMapping("/detail/{id}")
    public ApiResponse<BpBox> detail(@PathVariable Long id, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        BpBox box = bpBoxService.getById(id);
        if (box == null) return ApiResponse.fail("图纸不存在");
        if (box.getUserId() == null || !box.getUserId().equals(user.getId())) return ApiResponse.fail("无权访问");

        return ApiResponse.ok(box);
    }

    /**
     * DELETE /api/box/delete/{id}
     * 删除图纸
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        BpBox box = bpBoxService.getById(id);
        if (box == null) return ApiResponse.fail("图纸不存在");
        if (box.getUserId() == null || !box.getUserId().equals(user.getId())) return ApiResponse.fail("无权删除");

        bpBoxService.delete(id);
        return ApiResponse.ok(null);
    }

    /**
     * PUT /api/box/update
     * 更新图纸信息（名称等）
     */
    @PutMapping("/update")
    public ApiResponse<Void> update(@RequestBody BpBox box, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        BpBox existing = bpBoxService.getById(box.getId());
        if (existing == null) return ApiResponse.fail("图纸不存在");
        if (existing.getUserId() == null || !existing.getUserId().equals(user.getId())) return ApiResponse.fail("无权修改");

        bpBoxService.update(box);
        return ApiResponse.ok(null);
    }

    /**
     * POST /api/box/progress
     * 保存沉浸模式进度
     */
    @PostMapping("/progress")
    public ApiResponse<Void> saveProgress(@RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        Long boxId = body.get("boxId") != null ? ((Number) body.get("boxId")).longValue() : null;
        String progressData = body.get("progressData") != null ? (String) body.get("progressData") : null;

        if (boxId == null) return ApiResponse.fail("boxId 不能为空");

        BpBox box = bpBoxService.getById(boxId);
        if (box == null) return ApiResponse.fail("图纸不存在");
        if (box.getUserId() == null || !box.getUserId().equals(user.getId())) return ApiResponse.fail("无权修改");

        // 只更新进度数据
        box.setProgressData(progressData);
        bpBoxService.update(box);

        return ApiResponse.ok(null);
    }
}
