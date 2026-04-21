package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.BpBox;
import com.beanpattern.entity.BpDraft;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.BpBoxService;
import com.beanpattern.service.BpDraftService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 草稿箱接口
 * 存储用户的半成品图纸
 */
@RestController
@RequestMapping("/api/draft")
public class BpDraftController {

    private final BpDraftService bpDraftService;
    private final BpBoxService bpBoxService;
    private final SessionHelper sessionHelper;

    public BpDraftController(BpDraftService bpDraftService,
                              BpBoxService bpBoxService,
                              SessionHelper sessionHelper) {
        this.bpDraftService = bpDraftService;
        this.bpBoxService = bpBoxService;
        this.sessionHelper = sessionHelper;
    }

    /**
     * POST /api/draft/save
     * 保存草稿（新建或更新）
     */
    @PostMapping("/save")
    public ApiResponse<BpDraft> save(@RequestBody BpDraft draft, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        draft.setUserId(user.getId());
        bpDraftService.save(draft);
        return ApiResponse.ok(draft);
    }

    /**
     * GET /api/draft/list
     * 获取草稿箱列表
     */
    @GetMapping("/list")
    public ApiResponse<List<BpDraft>> list(HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        List<BpDraft> list = bpDraftService.listByUserId(user.getId());
        return ApiResponse.ok(list);
    }

    /**
     * GET /api/draft/detail/{id}
     * 获取草稿详情
     */
    @GetMapping("/detail/{id}")
    public ApiResponse<BpDraft> detail(@PathVariable Long id, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        BpDraft draft = bpDraftService.getById(id);
        if (draft == null) return ApiResponse.fail("草稿不存在");
        if (!draft.getUserId().equals(user.getId())) return ApiResponse.fail("无权访问");

        return ApiResponse.ok(draft);
    }

    /**
     * DELETE /api/draft/delete/{id}
     * 删除草稿
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        BpDraft draft = bpDraftService.getById(id);
        if (draft == null) return ApiResponse.fail("草稿不存在");
        if (!draft.getUserId().equals(user.getId())) return ApiResponse.fail("无权删除");

        bpDraftService.delete(id);
        return ApiResponse.ok(null);
    }

    /**
     * POST /api/draft/to-box
     * 草稿保存到图纸箱
     */
    @PostMapping("/to-box")
    public ApiResponse<Map<String, Object>> toBox(@RequestBody Map<String, Object> body,
                                                  HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        Long draftId = body.get("draftId") != null ? ((Number) body.get("draftId")).longValue() : null;
        if (draftId == null) return ApiResponse.fail("draftId 不能为空");

        BpDraft draft = bpDraftService.getById(draftId);
        if (draft == null) return ApiResponse.fail("草稿不存在");
        if (!draft.getUserId().equals(user.getId())) return ApiResponse.fail("无权操作");

        // 获取传入的名称，如果没有就用草稿的名称或默认名称
        String name = body.get("name") != null ? (String) body.get("name") : draft.getName();
        if (name == null || name.isEmpty()) {
            name = "草稿#" + System.currentTimeMillis();
        }

        // 复制到图纸箱
        BpBox box = new BpBox();
        box.setUserId(user.getId());
        box.setSourceType(draft.getSourceType());
        box.setBrand(draft.getBrand());
        box.setColorCount(draft.getColorCount());
        box.setName(name);
        box.setGridSize(draft.getGridSize());
        box.setMappedPixelData(draft.getMappedPixelData());
        box.setDraftId(draft.getId());
        box.setStatus(1); // 已完成
        box.setFocusCompletedCells(0);
        box.setFocusTotalCells(Math.max(1, (draft.getGridSize() == null ? 1 : draft.getGridSize()) * (draft.getGridSize() == null ? 1 : draft.getGridSize())));
        box.setFocusProgress("0");

        bpBoxService.insert(box);

        // 更新草稿箱关联
        bpDraftService.linkBoxId(draftId, box.getId());

        return ApiResponse.ok(Map.of(
                "boxId", box.getId(),
                "message", "已保存到图纸箱"
        ));
    }
}
