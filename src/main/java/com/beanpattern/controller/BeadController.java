package com.beanpattern.controller;

import com.beanpattern.mapper.BeadAdminMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.BeadColorService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bead")
public class BeadController {

    private final BeadColorService beadColorService;
    private final BeadAdminMapper beadAdminMapper;

    public BeadController(BeadColorService beadColorService,
                          BeadAdminMapper beadAdminMapper) {
        this.beadColorService = beadColorService;
        this.beadAdminMapper = beadAdminMapper;
    }

    @GetMapping("/brands")
    public ApiResponse<Map<String, Object>> getBrands() {
        List<String> brands = beadColorService.getBrandNames();
        Map<String, Object> result = new LinkedHashMap<>();
        for (String brand : brands) {
            result.put(brand, beadColorService.getKits(brand));
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/brand-list")
    public ApiResponse<List<Map<String, Object>>> brandList() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : beadAdminMapper.listBrands()) {
            data.add(Map.of(
                    "id", stringValue(item.get("id")),
                    "name", stringValue(item.get("name"))
            ));
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/palettes")
    public ApiResponse<List<Map<String, Object>>> palettesByBrand(@RequestParam("brandId") Long brandId) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : beadAdminMapper.listPalettesByBrandId(brandId)) {
            data.add(Map.of(
                    "id", stringValue(item.get("id")),
                    "name", stringValue(item.get("name"))
            ));
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/brand-kits")
    public ApiResponse<List<Map<String, Object>>> brandKits(@RequestParam(value = "brandId", required = false) Long brandId) {
        var list = brandId == null ? beadAdminMapper.listBrandKits() : beadAdminMapper.listKitsByBrandId(brandId);
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : list) {
            int count = intValue(firstNonNull(item.get("color_count"), item.get("colorCount")));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", stringValue(item.get("id")));
            if (item.get("brandId") != null) row.put("brandId", stringValue(item.get("brandId")));
            if (item.get("brandName") != null) row.put("brandName", stringValue(item.get("brandName")));
            row.put("colorCount", count);
            row.put("colorTotal", item.get("colorTotal"));
            row.put("name", count > 0 ? count + "色" : "套装");
            data.add(row);
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/brands/{brandId}/kits")
    public ApiResponse<List<Map<String, Object>>> kitsByBrand(@PathVariable Long brandId) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : beadAdminMapper.listKitsByBrandId(brandId)) {
            int count = intValue(firstNonNull(item.get("color_count"), item.get("colorCount")));
            data.add(Map.of(
                    "id", stringValue(item.get("id")),
                    "colorCount", count,
                    "colorTotal", firstNonNull(item.get("color_total"), item.get("colorTotal")),
                    "name", count > 0 ? count + "色" : "套装"
            ));
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/kits/{kitId}/palettes")
    public ApiResponse<List<Map<String, Object>>> palettesByKit(@PathVariable Long kitId) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : beadAdminMapper.listColorsByKitId(kitId)) {
            data.add(Map.of(
                    "id", stringValue(item.get("id")),
                    "name", stringValue(firstNonNull(item.get("displayName"), item.get("code"))),
                    "type", "color"
            ));
        }
        return ApiResponse.ok(data);
    }

    @GetMapping("/kits/{kitId}/colors")
    public ApiResponse<List<Map<String, Object>>> colorsByKit(@PathVariable Long kitId) {
        return ApiResponse.ok(colorRows(beadAdminMapper.listColorsByKitId(kitId)));
    }

    @GetMapping("/palettes/{id}/colors")
    public ApiResponse<List<Map<String, Object>>> paletteColors(@PathVariable Integer id) {
        return ApiResponse.ok(colorRows(beadAdminMapper.listColorsByPaletteId(id)));
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
            for (BeadColorService.BeadColor color : row) {
                rowList.add(Map.of(
                        "id", color.id(),
                        "name", color.name(),
                        "r", color.r(),
                        "g", color.g(),
                        "b", color.b()
                ));
            }
            result.add(rowList);
        }
        return ApiResponse.ok(result);
    }

    private List<Map<String, Object>> colorRows(List<Map<String, Object>> list) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (var item : list) {
            String code = stringValue(item.get("code"));
            String displayName = stringValue(firstNonNull(item.get("displayName"), code));
            String hex = stringValue(item.get("hex"));
            data.add(Map.of(
                    "id", stringValue(item.get("id")),
                    "code", code,
                    "name", displayName,
                    "displayName", displayName,
                    "hex", normalizeHex(hex),
                    "r", item.get("r"),
                    "g", item.get("g"),
                    "b", item.get("b")
            ));
        }
        data.sort(Comparator.comparing(o -> String.valueOf(o.get("code"))));
        return data;
    }

    private static Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static int intValue(Object value) {
        if (value instanceof Number n) return n.intValue();
        if (value == null) return 0;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String normalizeHex(String hex) {
        if (hex == null || hex.isBlank()) return "";
        return hex.startsWith("#") ? hex.toUpperCase() : ("#" + hex).toUpperCase();
    }
}
