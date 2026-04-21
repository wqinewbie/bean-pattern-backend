package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.BpBox;
import com.beanpattern.entity.BpHistory;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.BpBoxService;
import com.beanpattern.service.BpHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 时光机接口
 * 存储用户的生成操作日志
 */
@RestController
@RequestMapping("/api/history")
public class BpHistoryController {

    private final BpHistoryService bpHistoryService;
    private final BpBoxService bpBoxService;
    private final SessionHelper sessionHelper;

    public BpHistoryController(BpHistoryService bpHistoryService,
                                BpBoxService bpBoxService,
                                SessionHelper sessionHelper) {
        this.bpHistoryService = bpHistoryService;
        this.bpBoxService = bpBoxService;
        this.sessionHelper = sessionHelper;
    }

    /**
     * POST /api/history/save
     * 保存到时光机
     */
    @PostMapping("/save")
    public ApiResponse<BpHistory> save(@RequestBody BpHistory history, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        history.setUserId(user.getId());
        bpHistoryService.save(history);
        return ApiResponse.ok(history);
    }

    /**
     * GET /api/history/list
     * 获取时光机列表
     */
    @GetMapping("/list")
    public ApiResponse<List<BpHistory>> list(HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        List<BpHistory> list = bpHistoryService.listByUserId(user.getId());
        return ApiResponse.ok(list);
    }

    /**
     * GET /api/history/detail/{id}
     * 获取时光机记录详情
     */
    @GetMapping("/detail/{id}")
    public ApiResponse<BpHistory> detail(@PathVariable Long id, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        BpHistory history = bpHistoryService.getById(id);
        if (history == null) return ApiResponse.fail("记录不存在");
        
        System.out.println("=== /history/detail ===");
        System.out.println("请求用户ID: " + user.getId());
        System.out.println("记录用户ID: " + history.getUserId());
        System.out.println("记录ID: " + id);
        
        if (history.getUserId() == null) {
            System.out.println("错误：记录的 userId 为 null");
            return ApiResponse.fail("记录数据异常");
        }
        if (!history.getUserId().equals(user.getId())) {
            System.out.println("错误：用户ID不匹配");
            return ApiResponse.fail("无权访问");
        }

        return ApiResponse.ok(history);
    }

    /**
     * DELETE /api/history/delete/{id}
     * 删除时光机记录
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        BpHistory history = bpHistoryService.getById(id);
        if (history == null) return ApiResponse.fail("记录不存在");
        if (history.getUserId() == null || !history.getUserId().equals(user.getId())) return ApiResponse.fail("无权删除");

        bpHistoryService.delete(id);
        return ApiResponse.ok(null);
    }

    /**
     * POST /api/history/to-box
     * 时光机保存到图纸箱
     */
    @PostMapping("/to-box")
    public ApiResponse<Map<String, Object>> toBox(@RequestBody Map<String, Long> body,
                                                    HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        Long historyId = body.get("historyId");
        if (historyId == null) return ApiResponse.fail("historyId 不能为空");

        BpHistory history = bpHistoryService.getById(historyId);
        if (history == null) return ApiResponse.fail("记录不存在");
        if (history.getUserId() == null || !history.getUserId().equals(user.getId())) return ApiResponse.fail("无权操作");

        // 复制到图纸箱
        BpBox box = new BpBox();
        box.setUserId(user.getId());
        box.setSourceType(history.getSourceType());
        box.setBrand(history.getBrand());
        box.setColorCount(history.getColorCount());
        box.setName(history.getName());
        box.setGridSize(history.getGridSize());
        box.setMappedPixelData(history.getMappedPixelData());
        box.setSourceUrl(history.getSourceUrl());
        box.setStatus(1); // 已完成

        bpBoxService.insert(box);

        // 更新时光机关联
        bpHistoryService.linkBoxId(historyId, box.getId());

        return ApiResponse.ok(Map.of(
                "boxId", box.getId(),
                "message", "已保存到图纸箱"
        ));
    }
}
