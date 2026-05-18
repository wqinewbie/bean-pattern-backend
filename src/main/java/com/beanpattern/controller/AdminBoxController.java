package com.beanpattern.controller;

import com.beanpattern.mapper.BpBoxMapper;
import com.beanpattern.mapper.BpDraftMapper;
import com.beanpattern.mapper.BpHistoryMapper;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.model.ApiResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminBoxController {

    private final BpBoxMapper bpBoxMapper;
    private final BpDraftMapper bpDraftMapper;
    private final BpHistoryMapper bpHistoryMapper;
    private final UserMapper userMapper;

    public AdminBoxController(BpBoxMapper bpBoxMapper, BpDraftMapper bpDraftMapper,
                              BpHistoryMapper bpHistoryMapper, UserMapper userMapper) {
        this.bpBoxMapper = bpBoxMapper;
        this.bpDraftMapper = bpDraftMapper;
        this.bpHistoryMapper = bpHistoryMapper;
        this.userMapper = userMapper;
    }

    // ─── 图纸箱 ───

    @GetMapping("/user-boxes")
    public ApiResponse<Map<String, Object>> boxes(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(defaultValue = "") String q) {
        int offset = (page - 1) * pageSize;
        var list = bpBoxMapper.listAllWithPage(pageSize, offset);
        int total = bpBoxMapper.countAll();

        var result = list.stream().map(box -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", box.getId());
            m.put("userId", box.getUserId());
            var user = userMapper.findById(box.getUserId());
            m.put("userName", user != null ? user.getNickName() : "用户#" + box.getUserId());
            m.put("name", box.getName() != null ? box.getName() : "");
            m.put("sourceType", box.getSourceType() != null ? box.getSourceType() : "");
            m.put("brand", box.getBrand() != null ? box.getBrand() : "");
            m.put("colorCount", box.getColorCount() != null ? box.getColorCount() : 0);
            m.put("gridSize", box.getGridSize() != null ? box.getGridSize() : 0);
            m.put("coverUrl", box.getCoverUrl() != null ? box.getCoverUrl() : "");
            m.put("sourceUrl", box.getSourceUrl() != null ? box.getSourceUrl() : "");
            m.put("createdAt", box.getCreatedAt() != null ? box.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());

        if (StringUtils.hasText(q)) {
            result = result.stream().filter(m ->
                ((String) m.get("userName")).contains(q) ||
                ((String) m.get("name")).contains(q) ||
                String.valueOf(m.get("userId")).contains(q)
            ).collect(Collectors.toList());
            total = result.size();
        }

        return ApiResponse.ok(Map.of("list", result, "total", total));
    }

    @GetMapping("/user-boxes/{id}")
    public ApiResponse<Map<String, Object>> boxDetail(@PathVariable Long id) {
        var box = bpBoxMapper.findById(id);
        if (box == null) return ApiResponse.fail("图纸不存在");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", box.getId());
        m.put("userId", box.getUserId());
        var user = userMapper.findById(box.getUserId());
        m.put("userName", user != null ? user.getNickName() : "用户#" + box.getUserId());
        m.put("name", box.getName() != null ? box.getName() : "");
        m.put("sourceType", box.getSourceType() != null ? box.getSourceType() : "");
        m.put("brand", box.getBrand() != null ? box.getBrand() : "");
        m.put("colorCount", box.getColorCount() != null ? box.getColorCount() : 0);
        m.put("gridSize", box.getGridSize() != null ? box.getGridSize() : 0);
        m.put("coverUrl", box.getCoverUrl() != null ? box.getCoverUrl() : "");
        m.put("sourceUrl", box.getSourceUrl() != null ? box.getSourceUrl() : "");
        m.put("mappedPixelData", box.getMappedPixelData() != null ? box.getMappedPixelData() : "");
        m.put("createdAt", box.getCreatedAt() != null ? box.getCreatedAt().toString() : "");
        return ApiResponse.ok(m);
    }

    @DeleteMapping("/user-boxes/{id}")
    public ApiResponse<Void> deleteBox(@PathVariable Long id) {
        var box = bpBoxMapper.findById(id);
        if (box == null) return ApiResponse.fail("图纸不存在");
        bpBoxMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    // ─── 草稿箱 ───

    @GetMapping("/user-drafts")
    public ApiResponse<Map<String, Object>> drafts(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(defaultValue = "") String q) {
        int offset = (page - 1) * pageSize;
        var list = bpDraftMapper.listAllWithPage(pageSize, offset);
        int total = bpDraftMapper.countAll();

        var result = list.stream().map(draft -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", draft.getId());
            m.put("userId", draft.getUserId());
            var user = userMapper.findById(draft.getUserId());
            m.put("userName", user != null ? user.getNickName() : "用户#" + draft.getUserId());
            m.put("name", draft.getName() != null ? draft.getName() : "");
            m.put("sourceType", draft.getSourceType() != null ? draft.getSourceType() : "");
            m.put("brand", draft.getBrand() != null ? draft.getBrand() : "");
            m.put("colorCount", draft.getColorCount() != null ? draft.getColorCount() : 0);
            m.put("gridSize", draft.getGridSize() != null ? draft.getGridSize() : 0);
            m.put("createdAt", draft.getCreatedAt() != null ? draft.getCreatedAt().toString() : "");
            m.put("updatedAt", draft.getUpdatedAt() != null ? draft.getUpdatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());

        if (StringUtils.hasText(q)) {
            result = result.stream().filter(m ->
                ((String) m.get("userName")).contains(q) ||
                ((String) m.get("name")).contains(q) ||
                String.valueOf(m.get("userId")).contains(q)
            ).collect(Collectors.toList());
            total = result.size();
        }

        return ApiResponse.ok(Map.of("list", result, "total", total));
    }

    @GetMapping("/user-drafts/{id}")
    public ApiResponse<Map<String, Object>> draftDetail(@PathVariable Long id) {
        var draft = bpDraftMapper.findById(id);
        if (draft == null) return ApiResponse.fail("草稿不存在");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", draft.getId());
        m.put("userId", draft.getUserId());
        var user = userMapper.findById(draft.getUserId());
        m.put("userName", user != null ? user.getNickName() : "用户#" + draft.getUserId());
        m.put("name", draft.getName() != null ? draft.getName() : "");
        m.put("sourceType", draft.getSourceType() != null ? draft.getSourceType() : "");
        m.put("brand", draft.getBrand() != null ? draft.getBrand() : "");
        m.put("colorCount", draft.getColorCount() != null ? draft.getColorCount() : 0);
        m.put("gridSize", draft.getGridSize() != null ? draft.getGridSize() : 0);
        m.put("mappedPixelData", draft.getMappedPixelData() != null ? draft.getMappedPixelData() : "");
        m.put("createdAt", draft.getCreatedAt() != null ? draft.getCreatedAt().toString() : "");
        m.put("updatedAt", draft.getUpdatedAt() != null ? draft.getUpdatedAt().toString() : "");
        return ApiResponse.ok(m);
    }

    @DeleteMapping("/user-drafts/{id}")
    public ApiResponse<Void> deleteDraft(@PathVariable Long id) {
        var draft = bpDraftMapper.findById(id);
        if (draft == null) return ApiResponse.fail("草稿不存在");
        bpDraftMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    // ─── 时光机 ───

    @GetMapping("/user-history")
    public ApiResponse<Map<String, Object>> history(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    @RequestParam(defaultValue = "") String q) {
        int offset = (page - 1) * pageSize;
        var list = bpHistoryMapper.listAllWithPage(pageSize, offset);
        int total = bpHistoryMapper.countAll();

        var result = list.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.getId());
            m.put("userId", h.getUserId());
            var user = userMapper.findById(h.getUserId());
            m.put("userName", user != null ? user.getNickName() : "用户#" + h.getUserId());
            m.put("name", h.getName() != null ? h.getName() : "");
            m.put("sourceType", h.getSourceType() != null ? h.getSourceType() : "");
            m.put("brand", h.getBrand() != null ? h.getBrand() : "");
            m.put("colorCount", h.getColorCount() != null ? h.getColorCount() : 0);
            m.put("gridSize", h.getGridSize() != null ? h.getGridSize() : 0);
            m.put("sourceUrl", h.getSourceUrl() != null ? h.getSourceUrl() : "");
            m.put("boxId", h.getBoxId());
            m.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");
            m.put("expiresAt", h.getExpiresAt() != null ? h.getExpiresAt().toString() : "");
            return m;
        }).collect(Collectors.toList());

        if (StringUtils.hasText(q)) {
            result = result.stream().filter(m ->
                ((String) m.get("userName")).contains(q) ||
                ((String) m.get("name")).contains(q) ||
                String.valueOf(m.get("userId")).contains(q)
            ).collect(Collectors.toList());
            total = result.size();
        }

        return ApiResponse.ok(Map.of("list", result, "total", total));
    }

    @DeleteMapping("/user-history/{id}")
    public ApiResponse<Void> deleteHistory(@PathVariable Long id) {
        var h = bpHistoryMapper.findById(id);
        if (h == null) return ApiResponse.fail("记录不存在");
        bpHistoryMapper.deleteById(id);
        return ApiResponse.ok(null);
    }
}
