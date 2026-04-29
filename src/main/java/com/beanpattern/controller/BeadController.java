package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.AiImageService;
import com.beanpattern.service.BeadColorService;
import com.beanpattern.mapper.BeadAdminMapper;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bead")
public class BeadController {

    private final AiImageService aiImageService;
    private final BeadColorService beadColorService;
    private final BeadAdminMapper beadAdminMapper;

    public BeadController(AiImageService aiImageService,
                          BeadColorService beadColorService,
                          BeadAdminMapper beadAdminMapper) {
        this.aiImageService = aiImageService;
        this.beadColorService = beadColorService;
        this.beadAdminMapper = beadAdminMapper;
    }

    @GetMapping("/brands")
    public ApiResponse<Map<String, Object>> getBrands() {
        List<String> brands = beadColorService.getBrandNames();
        Map<String, Object> result = new LinkedHashMap<>();
        for (String b : brands) {
            result.put(b, beadColorService.getKits(b));
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/brand-list")
    public ApiResponse<List<Map<String, Object>>> brandList() {
        var list = beadAdminMapper.listBrands();
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : list) {
            Object id = item.get("id");
            Object name = item.get("name");
            data.add(Map.of(
                    "id", id == null ? "" : String.valueOf(id),
                    "name", name == null ? "" : String.valueOf(name)
            ));
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/palettes")
    public ApiResponse<List<Map<String, Object>>> palettesByBrand(@RequestParam("brandId") Long brandId) {
        var list = beadAdminMapper.listPalettesByBrandId(brandId);
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : list) {
            Object id = item.get("id");
            Object name = item.get("name");
            data.add(Map.of(
                    "id", id == null ? "" : String.valueOf(id),
                    "name", name == null ? "" : String.valueOf(name)
            ));
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/brand-kits")
    public ApiResponse<List<Map<String, Object>>> brandKits(@RequestParam("brandId") Long brandId) {
        var list = beadAdminMapper.listPalettesByBrandId(brandId);
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : list) {
            Object id = item.get("id");
            Object name = item.get("name");
            data.add(Map.of(
                    "id", id == null ? "" : String.valueOf(id),
                    "name", name == null ? "" : String.valueOf(name)
            ));
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/brands/{brandId}/kits")
    public ApiResponse<List<Map<String, Object>>> kitsByBrand(@PathVariable Long brandId) {
        var list = beadAdminMapper.listKitsByBrandId(brandId);
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : list) {
            Object id = item.get("id");
            Object colorCount = item.get("color_count");
            if (colorCount == null) colorCount = item.get("colorCount");
            int count = 0;
            if (colorCount instanceof Number n) count = n.intValue();
            else if (colorCount != null) {
                try { count = Integer.parseInt(String.valueOf(colorCount)); } catch (Exception ignored) {}
            }
            data.add(Map.of(
                    "id", id == null ? "" : String.valueOf(id),
                    "colorCount", count,
                    "name", count > 0 ? (count + "色") : "套餐"
            ));
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/kits/{kitId}/palettes")
    public ApiResponse<List<Map<String, Object>>> palettesByKit(@PathVariable Long kitId) {
        var list = beadAdminMapper.listPalettesByKitId(kitId);
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : list) {
            Object id = item.get("id");
            Object name = item.get("name");
            data.add(Map.of(
                    "id", id == null ? "" : String.valueOf(id),
                    "name", name == null ? "" : String.valueOf(name)
            ));
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/palettes/{id}/colors")
    public ApiResponse<List<Map<String, Object>>> paletteColors(@PathVariable Integer id) {
        var list = beadAdminMapper.listColorsByPaletteId(id);
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : list) {
            String code = item.get("code") == null ? "" : String.valueOf(item.get("code"));
            String hex = item.get("hex") == null ? "" : String.valueOf(item.get("hex"));
            data.add(Map.of(
                    "id", item.get("id") == null ? "" : String.valueOf(item.get("id")),
                    "code", code,
                    "name", code,
                    "hex", hex.startsWith("#") ? hex.toUpperCase() : ("#" + hex).toUpperCase(),
                    "r", item.get("r"),
                    "g", item.get("g"),
                    "b", item.get("b")
            ));
        }
        data.sort(Comparator.comparing(o -> String.valueOf(o.get("code"))));
        return ApiResponse.ok(data);
    }

    @PostMapping("/generate-result")
    public ApiResponse<Map<String, Object>> generateResult(@RequestBody Map<String, Object> body) {
        return ApiResponse.fail("该接口已停用");
    }

    @PostMapping("/generate-pattern")
    public ApiResponse<Map<String, Object>> generatePattern(@RequestBody Map<String, Object> body) {
        return ApiResponse.fail("该接口已停用");
    }

    @PostMapping("/match-colors")
    public ApiResponse<List<List<Map<String, Object>>>> matchColors(@RequestBody Map<String, Object> body) {
        String brand = (String) body.getOrDefault("brand", "mard");
        String algo = (String) body.getOrDefault("algo", "standard");
        int colorCount = body.get("colorCount") instanceof Number n ? n.intValue() : 0;

        @SuppressWarnings("unchecked")
        List<List<List<Integer>>> rawGrid = (List<List<List<Integer>>>) body.get("grid");
        if (rawGrid == null || rawGrid.isEmpty()) {
            return ApiResponse.fail("grid is required");
        }

        int rows = rawGrid.size();
        int cols = rawGrid.get(0).size();
        int[][][] rgbGrid = new int[rows][cols][3];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                List<Integer> px = rawGrid.get(y).get(x);
                rgbGrid[y][x][0] = px.get(0);
                rgbGrid[y][x][1] = px.get(1);
                rgbGrid[y][x][2] = px.get(2);
            }
        }

        BeadColorService.BeadColor[][] matched = beadColorService.matchGrid(rgbGrid, brand, colorCount, algo);
        List<List<Map<String, Object>>> result = new ArrayList<>();
        for (BeadColorService.BeadColor[] row : matched) {
            List<Map<String, Object>> rowList = new ArrayList<>();
            for (BeadColorService.BeadColor c : row) {
                rowList.add(Map.of(
                        "id", c.id(),
                        "name", c.name(),
                        "r", c.r(),
                        "g", c.g(),
                        "b", c.b()
                ));
            }
            result.add(rowList);
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/pattern-ai-text")
    public ApiResponse<Map<String, Object>> patternAiText(@RequestBody Map<String, Object> body) {
        String prompt = (String) body.getOrDefault("prompt", "");
        String style = (String) body.getOrDefault("style", "standard");
        int size = body.get("size") instanceof Number n ? n.intValue() : 64;
        if (prompt.isBlank()) {
            return ApiResponse.fail("prompt is required");
        }

        try {
            String resultUrl = aiImageService.generateFromText(prompt, style, size);
            if (resultUrl.isBlank()) {
                return ApiResponse.fail("AI service is not configured");
            }
            return ApiResponse.ok(Map.of(
                    "resultUrl", resultUrl,
                    "patternUrl", "",
                    "colorStats", ""
            ));
        } catch (Exception e) {
            return ApiResponse.fail("AI generation failed: " + e.getMessage());
        }
    }
}
