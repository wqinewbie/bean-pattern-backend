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

        System.out.println("=== /box/save ===");
        System.out.println("用户ID: " + user.getId());
        System.out.println("图纸名称: " + box.getName());
        System.out.println("来源类型: " + box.getSourceType());
        
        box.setUserId(user.getId());
        System.out.println("设置的 userId: " + box.getUserId());
        
        bpBoxService.save(box);
        System.out.println("保存后的图纸ID: " + box.getId());
        
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
        
        System.out.println("=== /box/detail ===");
        System.out.println("请求用户ID: " + user.getId());
        System.out.println("图纸对象: " + box);
        System.out.println("图纸ID: " + id + ", userId: " + box.getUserId() + ", name: " + box.getName());
        
        if (box.getUserId() == null) {
            System.out.println("错误：图纸的 userId 为 null");
            return ApiResponse.fail("图纸数据异常，请重新保存");
        }
        if (!box.getUserId().equals(user.getId())) {
            System.out.println("错误：用户ID不匹配");
            return ApiResponse.fail("无权访问");
        }

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
