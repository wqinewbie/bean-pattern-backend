package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.AiGenerateTask;
import com.beanpattern.entity.BpBox;
import com.beanpattern.entity.BpHistory;
import com.beanpattern.mapper.AiGenerateTaskMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.BpBoxService;
import com.beanpattern.service.BpDraftService;
import com.beanpattern.service.BpHistoryService;
import com.beanpattern.service.PrivilegeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
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
    private final BpHistoryService bpHistoryService;
    private final BpDraftService bpDraftService;
    private final AiGenerateTaskMapper aiGenerateTaskMapper;
    private final PrivilegeService privilegeService;
    private final SessionHelper sessionHelper;

    public BpBoxController(BpBoxService bpBoxService,
                           BpHistoryService bpHistoryService,
                           BpDraftService bpDraftService,
                           AiGenerateTaskMapper aiGenerateTaskMapper,
                           PrivilegeService privilegeService,
                           SessionHelper sessionHelper) {
        this.bpBoxService = bpBoxService;
        this.bpHistoryService = bpHistoryService;
        this.bpDraftService = bpDraftService;
        this.aiGenerateTaskMapper = aiGenerateTaskMapper;
        this.privilegeService = privilegeService;
        this.sessionHelper = sessionHelper;
    }

    /**
     * POST /api/box/save
     * 保存图纸到图纸箱
     */
    @PostMapping("/save")
    public ApiResponse<java.util.Map<String, Object>> save(@Valid @RequestBody BpBox box, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        System.out.println("=== /box/save ===");
        System.out.println("用户ID: " + user.getId());
        System.out.println("图纸名称: " + box.getName());
        System.out.println("来源类型: " + box.getSourceType());
        
        if (box.getId() != null) {
            BpBox existing = bpBoxService.getById(box.getId());
            if (existing == null) return ApiResponse.fail("图纸不存在");
            if (existing.getUserId() == null || !existing.getUserId().equals(user.getId())) return ApiResponse.fail("无权操作");
        } else {
            PrivilegeService.LimitStatus status = privilegeService.getPatternBoxLimitStatus(user.getId());
            if (!status.canAdd()) {
                return ApiResponse.fail("图纸箱容量已满（" + status.current() + "/" + status.limit() + "），请删除图纸或升级会员");
            }
        }

        box.setUserId(user.getId());
        box.setAiStyle(resolveAiStyle(box));
        System.out.println("设置的 userId: " + box.getUserId());
        
        bpBoxService.save(box);
        if (box.getHistoryId() != null && box.getId() != null) {
            BpHistory history = bpHistoryService.getById(box.getHistoryId());
            if (history != null && user.getId().equals(history.getUserId())) {
                bpHistoryService.linkBoxId(box.getHistoryId(), box.getId());
            }
        }
        System.out.println("保存后的图纸ID: " + box.getId());

        PrivilegeService.LimitStatus afterStatus = privilegeService.getPatternBoxLimitStatus(user.getId());
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", box.getId());
        result.put("boxId", box.getId());
        result.put("capacityFull", !afterStatus.canAdd());
        result.put("capacityCurrent", afterStatus.current());
        result.put("capacityLimit", afterStatus.limit());
        result.put("capacityMessage", "图纸箱容量已满（" + afterStatus.current() + "/" + afterStatus.limit() + "），请删除图纸或升级会员");
        return ApiResponse.ok(result);
    }

    /**
     * GET /api/box/list
     * 获取图纸箱列表（支持分页）
     * @param page 页码，从1开始，默认1
     * @param pageSize 每页数量，默认20
     */
    @GetMapping("/list")
    public ApiResponse<java.util.Map<String, Object>> list(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        if (pageSize > 100) pageSize = 100; // 限制最大每页数量

        int offset = (page - 1) * pageSize;
        List<BpBox> list = bpBoxService.listByUserIdWithPage(user.getId(), pageSize, offset);
        int total = bpBoxService.countByUserId(user.getId());
        boolean hasMore = offset + list.size() < total;

        return ApiResponse.ok(java.util.Map.of(
                "list", list,
                "total", total,
                "page", page,
                "pageSize", pageSize,
                "hasMore", hasMore
        ));
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

        enrichAiStyle(box);
        return ApiResponse.ok(box);
    }

    /**
     * DELETE /api/box/delete/{id}
     * 删除图纸
     */
    @DeleteMapping("/delete/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        BpBox box = bpBoxService.getById(id);
        if (box == null) return ApiResponse.fail("图纸不存在");
        if (box.getUserId() == null || !box.getUserId().equals(user.getId())) return ApiResponse.fail("无权删除");

        bpBoxService.delete(id);
        bpHistoryService.clearBoxId(id);
        bpDraftService.clearBoxId(id);
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
        if (box.getId() == null) return ApiResponse.fail("id 不能为空");
        if (box.getName() == null || box.getName().trim().isEmpty()) return ApiResponse.fail("name 不能为空");

        BpBox existing = bpBoxService.getById(box.getId());
        if (existing == null) return ApiResponse.fail("图纸不存在");
        if (existing.getUserId() == null || !existing.getUserId().equals(user.getId())) return ApiResponse.fail("无权修改");

        // 只更新名称，避免传输大量 mappedPixelData
        bpBoxService.updateName(box.getId(), box.getName().trim());
        return ApiResponse.ok(null);
    }

    @PutMapping("/rename")
    public ApiResponse<Void> rename(@RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        Long id = parseLong(body.get("id"));
        String name = body.get("name") != null ? String.valueOf(body.get("name")) : null;

        if (id == null) return ApiResponse.fail("id 不能为空");
        if (name == null || name.trim().isEmpty()) return ApiResponse.fail("name 不能为空");

        BpBox existing = bpBoxService.getById(id);
        if (existing == null) return ApiResponse.fail("图纸不存在");
        if (existing.getUserId() == null || !existing.getUserId().equals(user.getId())) return ApiResponse.fail("无权修改");

        bpBoxService.updateName(id, name.trim());
        return ApiResponse.ok(null);
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            String text = String.valueOf(value).trim();
            return text.isEmpty() ? null : Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void enrichAiStyle(BpBox box) {
        if (box == null || (box.getAiStyle() != null && !box.getAiStyle().isBlank())) return;
        box.setAiStyle(resolveAiStyle(box));
    }

    private String resolveAiStyle(BpBox box) {
        if (box != null && box.getAiStyle() != null && !box.getAiStyle().isBlank()) {
            return box.getAiStyle();
        }
        if (box == null || box.getHistoryId() == null) return null;
        String sourceType = box.getSourceType() == null ? "" : box.getSourceType().toUpperCase();
        if (!sourceType.contains("AI")) return null;
        BpHistory history = bpHistoryService.getById(box.getHistoryId());
        if (history == null || history.getTaskId() == null || history.getTaskId().isBlank()) return null;
        if (history.getAiStyle() != null && !history.getAiStyle().isBlank()) {
            return history.getAiStyle();
        }
        AiGenerateTask task = aiGenerateTaskMapper.findByTaskId(history.getTaskId());
        if (task != null && task.getStyle() != null && !task.getStyle().isBlank()) {
            return task.getStyle();
        }
        return null;
    }

    /**
     * POST /api/box/progress
     * 保存沉浸模式进度（V6.0支持新字段）
     */
    @PostMapping("/progress")
    public ApiResponse<Void> saveProgress(@RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        var user = sessionHelper.requireCompleteProfileUser(request);
        if (user == null) return ApiResponse.fail("请先登录");

        Long boxId = body.get("boxId") != null ? ((Number) body.get("boxId")).longValue() : null;

        if (boxId == null) return ApiResponse.fail("boxId 不能为空");

        BpBox box = bpBoxService.getById(boxId);
        if (box == null) return ApiResponse.fail("图纸不存在");
        if (box.getUserId() == null || !box.getUserId().equals(user.getId())) return ApiResponse.fail("无权修改");

        // V6.0 新增字段（只在请求中包含时才设置，避免用 null 覆盖已有值）
        if (body.containsKey("focusProgress") && body.get("focusProgress") != null) {
            box.setFocusProgress((String) body.get("focusProgress"));
        }
        if (body.containsKey("focusCompletedCells") && body.get("focusCompletedCells") != null) {
            box.setFocusCompletedCells(((Number) body.get("focusCompletedCells")).intValue());
        }
        if (body.containsKey("focusTotalCells") && body.get("focusTotalCells") != null) {
            box.setFocusTotalCells(((Number) body.get("focusTotalCells")).intValue());
        }
        
        bpBoxService.update(box);

        return ApiResponse.ok(null);
    }
}
