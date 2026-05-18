package com.beanpattern.controller;

import com.beanpattern.mapper.BeadAdminMapper;
import com.beanpattern.model.ApiResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/bead")
public class AdminBeadController {

    private final BeadAdminMapper beadAdminMapper;

    public AdminBeadController(BeadAdminMapper beadAdminMapper) {
        this.beadAdminMapper = beadAdminMapper;
    }

    // ─── 品牌 ───

    @GetMapping("/brands")
    public ApiResponse<List<Map<String, Object>>> brands() {
        return ApiResponse.ok(beadAdminMapper.listBrands());
    }

    @PostMapping("/brands")
    public ApiResponse<Void> createBrand(@RequestBody Map<String, Object> body) {
        String name = ((String) body.getOrDefault("name", "")).trim();
        if (!StringUtils.hasText(name)) return ApiResponse.fail("品牌名不能为空");
        if (beadAdminMapper.countBrandByName(name) > 0) return ApiResponse.fail("品牌已存在");
        beadAdminMapper.insertBrand(name);
        return ApiResponse.ok(null);
    }

    @PutMapping("/brands/{id}")
    public ApiResponse<Void> updateBrand(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = ((String) body.getOrDefault("name", "")).trim();
        if (!StringUtils.hasText(name)) return ApiResponse.fail("品牌名不能为空");
        beadAdminMapper.updateBrand(id, name);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/brands/{id}")
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {
        beadAdminMapper.deleteBrand(id);
        return ApiResponse.ok(null);
    }

    // ─── 色盘 ───

    @GetMapping("/palettes")
    public ApiResponse<List<Map<String, Object>>> palettes() {
        return ApiResponse.ok(beadAdminMapper.listPalettes());
    }

    @PostMapping("/palettes")
    public ApiResponse<Void> createPalette(@RequestBody Map<String, Object> body) {
        String name = ((String) body.getOrDefault("name", "")).trim();
        String remark = ((String) body.getOrDefault("remark", "")).trim();
        if (!StringUtils.hasText(name)) return ApiResponse.fail("色盘名不能为空");
        if (beadAdminMapper.countPaletteByName(name) > 0) return ApiResponse.fail("色盘已存在");
        beadAdminMapper.insertPalette(name, remark);
        return ApiResponse.ok(null);
    }

    @PutMapping("/palettes/{id}")
    public ApiResponse<Void> updatePalette(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = ((String) body.getOrDefault("name", "")).trim();
        String remark = ((String) body.getOrDefault("remark", "")).trim();
        if (!StringUtils.hasText(name)) return ApiResponse.fail("色盘名不能为空");
        beadAdminMapper.updatePalette(id, name, remark);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/palettes/{id}")
    public ApiResponse<Void> deletePalette(@PathVariable Long id) {
        beadAdminMapper.deletePalette(id);
        return ApiResponse.ok(null);
    }

    // ─── 色号 ───

    @GetMapping("/colors")
    public ApiResponse<List<Map<String, Object>>> colors(@RequestParam(defaultValue = "") String q) {
        return ApiResponse.ok(beadAdminMapper.listColors(q));
    }

    @PostMapping("/colors")
    public ApiResponse<Void> createColor(@RequestBody Map<String, Object> body) {
        String code = ((String) body.getOrDefault("code", "")).trim();
        String hex = ((String) body.getOrDefault("hex", "")).trim().toUpperCase();
        int r = body.get("r") instanceof Number n ? n.intValue() : 0;
        int g = body.get("g") instanceof Number n ? n.intValue() : 0;
        int b = body.get("b") instanceof Number n ? n.intValue() : 0;
        if (!StringUtils.hasText(code)) return ApiResponse.fail("色号不能为空");
        if (!StringUtils.hasText(hex)) return ApiResponse.fail("HEX不能为空");
        if (beadAdminMapper.countColorByCode(code) > 0) return ApiResponse.fail("色号已存在");
        beadAdminMapper.insertColor(code, hex, r, g, b);
        return ApiResponse.ok(null);
    }

    @PutMapping("/colors/{id}")
    public ApiResponse<Void> updateColor(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String code = ((String) body.getOrDefault("code", "")).trim();
        String hex = ((String) body.getOrDefault("hex", "")).trim().toUpperCase();
        int r = body.get("r") instanceof Number n ? n.intValue() : 0;
        int g = body.get("g") instanceof Number n ? n.intValue() : 0;
        int b = body.get("b") instanceof Number n ? n.intValue() : 0;
        if (!StringUtils.hasText(code)) return ApiResponse.fail("色号不能为空");
        if (!StringUtils.hasText(hex)) return ApiResponse.fail("HEX不能为空");
        beadAdminMapper.updateColor(id, code, hex, r, g, b);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/colors/{id}")
    public ApiResponse<Void> deleteColor(@PathVariable Long id) {
        beadAdminMapper.deleteColor(id);
        return ApiResponse.ok(null);
    }

    // ─── 色盘批量添加色号 ───

    @PostMapping("/palettes/{id}/batch-add-colors")
    public ApiResponse<Map<String, Object>> batchAddPaletteColors(@PathVariable Long id,
                                                                   @RequestBody Map<String, Object> body) {
        if (beadAdminMapper.countPaletteById(id) <= 0) return ApiResponse.fail("色盘不存在");

        Object raw = body.get("codes");
        if (!(raw instanceof List<?> rawList) || rawList.isEmpty()) return ApiResponse.fail("codes 不能为空");

        List<String> codes = rawList.stream()
                .map(v -> v == null ? "" : String.valueOf(v).trim())
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (codes.isEmpty()) return ApiResponse.fail("codes 不能为空");

        List<Long> colorIds = beadAdminMapper.listColorIdsByCodes(codes);
        int added = 0;
        if (!colorIds.isEmpty()) {
            added = beadAdminMapper.insertPaletteColorsBatch(id, colorIds);
        }

        int found = colorIds.size();
        int missing = Math.max(codes.size() - found, 0);
        int ignored = Math.max(found - added, 0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inputCount", codes.size());
        result.put("foundCount", found);
        result.put("addedCount", added);
        result.put("ignoredCount", ignored);
        result.put("missingCount", missing);
        return ApiResponse.ok(result);
    }
}
