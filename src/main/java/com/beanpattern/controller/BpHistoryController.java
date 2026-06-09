package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.AiGenerateTask;
import com.beanpattern.entity.BpBox;
import com.beanpattern.entity.BpHistory;
import com.beanpattern.mapper.AiGenerateTaskMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.BpBoxService;
import com.beanpattern.service.BpHistoryService;
import com.beanpattern.service.PrivilegeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private final AiGenerateTaskMapper aiGenerateTaskMapper;
    private final PrivilegeService privilegeService;
    private final SessionHelper sessionHelper;

    public BpHistoryController(BpHistoryService bpHistoryService,
                                BpBoxService bpBoxService,
                                AiGenerateTaskMapper aiGenerateTaskMapper,
                                PrivilegeService privilegeService,
                                SessionHelper sessionHelper) {
        this.bpHistoryService = bpHistoryService;
        this.bpBoxService = bpBoxService;
        this.aiGenerateTaskMapper = aiGenerateTaskMapper;
        this.privilegeService = privilegeService;
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
        history.setExpiresAt(LocalDateTime.now().plusDays(privilegeService.getHistoryExpireDays(user.getId())));
        bpHistoryService.save(history);
        return ApiResponse.ok(history);
    }

    /**
     * GET /api/history/list
     * 获取时光机列表（支持分页）
     * @param page 页码，从1开始，默认1
     * @param pageSize 每页数量，默认20
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
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
        List<BpHistory> list = bpHistoryService.listByUserIdWithPage(user.getId(), pageSize, offset);
        list.forEach(history -> {
            clearStaleBoxLink(history, user.getId());
            enrichAiStyle(history);
        });
        int total = bpHistoryService.countByUserId(user.getId());
        boolean hasMore = offset + list.size() < total;

        return ApiResponse.ok(Map.of(
                "list", list,
                "total", total,
                "page", page,
                "pageSize", pageSize,
                "hasMore", hasMore
        ));
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

        enrichAiStyle(history);
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
    @Transactional
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
        if (history.getBoxId() != null) {
            BpBox existingBox = bpBoxService.getById(history.getBoxId());
            if (isActiveUserBox(existingBox, user.getId())) {
                return ApiResponse.ok(Map.of(
                        "boxId", history.getBoxId(),
                        "message", "已保存到图纸箱",
                        "alreadySaved", true
                ));
            }
            bpHistoryService.linkBoxId(historyId, null);
            history.setBoxId(null);
        }
        PrivilegeService.LimitStatus status = privilegeService.getPatternBoxLimitStatus(user.getId());
        if (!status.canAdd()) {
            return ApiResponse.fail("图纸箱容量已满（" + status.current() + "/" + status.limit() + "），请删除图纸或升级会员");
        }

        BpBox box = new BpBox();
        box.setUserId(user.getId());
        box.setSourceType(history.getSourceType());
        box.setBrand(history.getBrand());
        box.setColorCount(history.getColorCount());
        box.setName(history.getName());
        box.setGridSize(history.getGridSize());
        box.setMappedPixelData(history.getMappedPixelData());
        box.setSourceUrl(history.getSourceUrl());
        box.setAiStyle(resolveAiStyle(history));
        box.setHistoryId(historyId);
        box.setStatus(1); // 已完成
        box.setFocusCompletedCells(0);
        box.setFocusTotalCells(Math.max(1, (history.getGridSize() == null ? 1 : history.getGridSize()) * (history.getGridSize() == null ? 1 : history.getGridSize())));
        box.setFocusProgress("0");

        bpBoxService.insert(box);

        // 更新时光机关联
        bpHistoryService.linkBoxId(historyId, box.getId());

        PrivilegeService.LimitStatus afterStatus = privilegeService.getPatternBoxLimitStatus(user.getId());
        return ApiResponse.ok(Map.of(
                "boxId", box.getId(),
                "message", "已保存到图纸箱",
                "alreadySaved", false,
                "capacityFull", !afterStatus.canAdd(),
                "capacityCurrent", afterStatus.current(),
                "capacityLimit", afterStatus.limit(),
                "capacityMessage", "图纸箱容量已满（" + afterStatus.current() + "/" + afterStatus.limit() + "），请删除图纸或升级会员"
        ));
    }

    private void clearStaleBoxLink(BpHistory history, Long userId) {
        if (history == null || history.getBoxId() == null) return;
        BpBox box = bpBoxService.getById(history.getBoxId());
        if (isActiveUserBox(box, userId)) return;
        history.setBoxId(null);
    }

    private boolean isActiveUserBox(BpBox box, Long userId) {
        return box != null
                && box.getUserId() != null
                && box.getUserId().equals(userId)
                && (box.getStatus() == null || box.getStatus() != BpBox.STATUS_DELETED);
    }

    private void enrichAiStyle(BpHistory history) {
        if (history == null || history.getTaskId() == null || history.getTaskId().isBlank()) return;
        if (history.getAiStyle() != null && !history.getAiStyle().isBlank()) return;
        String sourceType = history.getSourceType() == null ? "" : history.getSourceType().toUpperCase();
        if (!sourceType.contains("AI")) return;
        AiGenerateTask task = aiGenerateTaskMapper.findByTaskId(history.getTaskId());
        if (task != null && task.getStyle() != null && !task.getStyle().isBlank()) {
            history.setAiStyle(task.getStyle());
        }
    }

    private String resolveAiStyle(BpHistory history) {
        enrichAiStyle(history);
        return history == null ? null : history.getAiStyle();
    }
}
