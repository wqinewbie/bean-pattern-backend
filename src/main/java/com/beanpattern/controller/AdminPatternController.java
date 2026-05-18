package com.beanpattern.controller;

import com.beanpattern.mapper.CreatorPatternMapper;
import com.beanpattern.model.ApiResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/patterns")
public class AdminPatternController {

    private final CreatorPatternMapper creatorPatternMapper;

    public AdminPatternController(CreatorPatternMapper creatorPatternMapper) {
        this.creatorPatternMapper = creatorPatternMapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(defaultValue = "") String status) {
        var all = creatorPatternMapper.listAll();
        var filtered = all.stream().filter(p ->
                !StringUtils.hasText(status) || String.valueOf(p.getStatus()).equals(status)
        ).collect(Collectors.toList());
        int total = filtered.size();
        var paged = filtered.stream().skip((long) (page - 1) * pageSize).limit(pageSize)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("title", p.getTitle());
                    m.put("coverUrl", p.getCoverUrl() != null ? p.getCoverUrl() : "");
                    m.put("category", p.getCategory() != null ? p.getCategory() : "");
                    m.put("priceCoins", p.getPriceCoins());
                    m.put("downloadCount", p.getDownloadCount());
                    m.put("status", p.getStatus());
                    m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
                    return m;
                }).collect(Collectors.toList());
        return ApiResponse.ok(Map.of("list", paged, "total", total));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        creatorPatternMapper.updateStatus(id, 1, null);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        creatorPatternMapper.updateStatus(id, 3, body.getOrDefault("reason", ""));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/toggle-online")
    public ApiResponse<Void> toggle(@PathVariable Long id) {
        var p = creatorPatternMapper.findById(id);
        if (p == null) return ApiResponse.fail("图纸不存在");
        creatorPatternMapper.updateStatus(id, p.getStatus() == 1 ? 2 : 1, null);
        return ApiResponse.ok(null);
    }
}
