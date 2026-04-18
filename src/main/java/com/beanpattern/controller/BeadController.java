package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.BpHistory;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.AiImageService;
import com.beanpattern.service.BeadColorService;
import com.beanpattern.service.BpHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bead")
public class BeadController {

    private final SessionHelper sessionHelper;
    private final AiImageService aiImageService;
    private final BeadColorService beadColorService;
    private final BpHistoryService bpHistoryService;

    public BeadController(SessionHelper sessionHelper,
                          AiImageService aiImageService,
                          BeadColorService beadColorService,
                          BpHistoryService bpHistoryService) {
        this.sessionHelper = sessionHelper;
        this.aiImageService = aiImageService;
        this.beadColorService = beadColorService;
        this.bpHistoryService = bpHistoryService;
    }

    /** GET /api/bead/brands */
    @GetMapping("/brands")
    public ApiResponse<Map<String, Object>> getBrands() {
        List<String> brands = beadColorService.getBrandNames();
        Map<String, Object> result = new LinkedHashMap<>();
        for (String b : brands) result.put(b, beadColorService.getKits(b));
        return ApiResponse.ok(result);
    }

    /** GET /api/bead/colors?brand=mard&colorCount=48 */
    @GetMapping("/colors")
    public ApiResponse<List<BeadColorService.BeadColor>> getColors(
            @RequestParam(defaultValue = "mard") String brand,
            @RequestParam(defaultValue = "0") int colorCount) {
        return ApiResponse.ok(beadColorService.getColors(brand, colorCount));
    }

    /**
     * POST /api/bead/generate-result
     * 原图 → 效果图数据
     * body: { imageUrl: "xxx", gridSize: 64 }
     * 返回: { rgbData: [[[r,g,b],...],...], gridSize: 64 }
     */
    @PostMapping("/generate-result")
    public ApiResponse<Map<String, Object>> generateResult(
            @RequestBody Map<String, Object> body) {
        String imageUrl = (String) body.getOrDefault("imageUrl", "");
        int gridSize = body.get("gridSize") instanceof Number n ? n.intValue() : 64;
        
        if (imageUrl.isBlank()) {
            return ApiResponse.fail("imageUrl 不能为空");
        }
        
        try {
            // 下载并处理图片，返回 rgbData
            int[][][] rgbData = beadColorService.downloadAndResize(imageUrl, gridSize);
            
            List<List<List<Integer>>> result = new ArrayList<>();
            for (int[][] row : rgbData) {
                List<List<Integer>> rowList = new ArrayList<>();
                for (int[] pixel : row) {
                    rowList.add(List.of(pixel[0], pixel[1], pixel[2]));
                }
                result.add(rowList);
            }
            
            return ApiResponse.ok(Map.of(
                    "rgbData", result,
                    "gridSize", gridSize
            ));
        } catch (Exception e) {
            return ApiResponse.fail("处理图片失败：" + e.getMessage());
        }
    }

    /**
     * POST /api/bead/generate-pattern
     * 效果图数据 → 色号图数据
     * body: { rgbData: [[[r,g,b],...],...], brand: "mard", colorCount: 48, algo: "standard" }
     * 返回: { gridData: [[0,1,2],...], colorPalette: [{id, name, r, g, b, count},...] }
     */
    @PostMapping("/generate-pattern")
    public ApiResponse<Map<String, Object>> generatePattern(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<List<List<Integer>>> rawGrid = (List<List<List<Integer>>>) body.get("rgbData");
        String brand = (String) body.getOrDefault("brand", "mard");
        String algo = (String) body.getOrDefault("algo", "standard");
        int colorCount = body.get("colorCount") instanceof Number n ? n.intValue() : 0;
        
        if (rawGrid == null || rawGrid.isEmpty()) {
            return ApiResponse.fail("rgbData 不能为空");
        }
        
        try {
            // 转换 RGB 数据
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
            
            // 颜色匹配
            BeadColorService.BeadColor[][] matched = beadColorService.matchGrid(rgbGrid, brand, colorCount, algo);
            
            // 构建 gridData 和 colorPalette
            List<List<Integer>> gridData = new ArrayList<>();
            Map<String, BeadColorService.BeadColor> colorMap = new LinkedHashMap<>();
            Map<String, Integer> colorIndexMap = new LinkedHashMap<>(); // 记录每个颜色 id 对应的索引
            
            for (BeadColorService.BeadColor[] row : matched) {
                List<Integer> rowList = new ArrayList<>();
                for (BeadColorService.BeadColor c : row) {
                    if (!colorIndexMap.containsKey(c.id())) {
                        // 新颜色，添加到 colorMap 和 colorIndexMap
                        int newIndex = colorIndexMap.size();
                        colorIndexMap.put(c.id(), newIndex);
                        colorMap.put(c.id(), c);
                    }
                    // 使用该颜色对应的索引
                    rowList.add(colorIndexMap.get(c.id()));
                }
                gridData.add(rowList);
            }
            
            // 构建效果图用的 rgbData（已经被替换为拼豆颜色）
            List<List<List<Integer>>> effectRgbData = new ArrayList<>();
            for (BeadColorService.BeadColor[] row : matched) {
                List<List<Integer>> rowList = new ArrayList<>();
                for (BeadColorService.BeadColor c : row) {
                    rowList.add(List.of(c.r(), c.g(), c.b()));
                }
                effectRgbData.add(rowList);
            }
            
            // 构建 colorPalette
            List<Map<String, Object>> colorPalette = new ArrayList<>();
            int index = 0;
            for (BeadColorService.BeadColor c : colorMap.values()) {
                // 统计该颜色的数量
                int count = 0;
                for (BeadColorService.BeadColor[] row : matched) {
                    for (BeadColorService.BeadColor pixel : row) {
                        if (pixel.id().equals(c.id())) count++;
                    }
                }
                colorPalette.add(Map.of(
                        "id", c.id(),
                        "index", index++,
                        "name", c.name(),
                        "r", c.r(),
                        "g", c.g(),
                        "b", c.b(),
                        "count", count
                ));
            }
            
            // 保存到时光机（后端直接处理）
            Long historyId = null;
            try {
                // 获取当前用户
                UserEntity user = sessionHelper.resolveUser(request);
                if (user != null) {
                    System.out.println("=== 保存到时光机，用户ID: " + user.getId());
                    BpHistory history = new BpHistory();
                    history.setUserId(user.getId());
                    history.setSourceType("LOCAL");
                    history.setBrand(brand);
                    history.setColorCount(colorPalette.size());
                    history.setGridSize(rows);
                    history.setGridData(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(gridData));
                    history.setColorPalette(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(colorPalette));
                    history.setRgbData(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(effectRgbData));
                    // 使用 insert() 方法，会自动设置过期时间
                    int result = bpHistoryService.insert(history);
                    historyId = history.getId();
                    System.out.println("=== 时光机保存成功，ID: " + historyId + "，影响行数: " + result);
                } else {
                    System.out.println("=== 保存到时光机失败：用户未登录");
                }
            } catch (Exception e) {
                // 保存失败不影响主流程，记录日志
                System.err.println("=== 保存到时光机异常: " + e.getMessage());
                e.printStackTrace();
            }
            
            return ApiResponse.ok(Map.of(
                    "gridData", gridData,
                    "colorPalette", colorPalette,
                    "effectRgbData", effectRgbData,
                    "gridSize", rows,
                    "colorCount", colorPalette.size(),
                    "historyId", historyId != null ? historyId : 0
            ));
        } catch (Exception e) {
            return ApiResponse.fail("生成色号图失败：" + e.getMessage());
        }
    }

    /**
     * POST /api/bead/match-colors
     * body: { brand: "mard", algo: "standard", grid: [[[r,g,b],...], ...] }
     */
    @PostMapping("/match-colors")
    public ApiResponse<List<List<Map<String, Object>>>> matchColors(
            @RequestBody Map<String, Object> body) {
        String brand = (String) body.getOrDefault("brand", "mard");
        String algo  = (String) body.getOrDefault("algo",  "standard");
        int colorCount = body.get("colorCount") instanceof Number n ? n.intValue() : 0;
        @SuppressWarnings("unchecked")
        List<List<List<Integer>>> rawGrid = (List<List<List<Integer>>>) body.get("grid");
        if (rawGrid == null || rawGrid.isEmpty()) {
            return ApiResponse.fail("grid 不能为空");
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
                rowList.add(Map.of("id", c.id(), "name", c.name(),
                        "r", c.r(), "g", c.g(), "b", c.b()));
            }
            result.add(rowList);
        }
        return ApiResponse.ok(result);
    }

    /** POST /api/bead/pattern-ai-text - 文字生成拼豆图纸 */
    @PostMapping("/pattern-ai-text")
    public ApiResponse<Map<String, Object>> patternAiText(
            @RequestBody Map<String, Object> body) {
        String prompt = (String) body.getOrDefault("prompt", "");
        String style  = (String) body.getOrDefault("style",  "标准");
        int size = body.get("size") instanceof Number n ? n.intValue() : 64;
        if (prompt.isBlank()) return ApiResponse.fail("prompt 不能为空");
        
        try {
            String resultUrl = aiImageService.generateFromText(prompt, style, size);
            if (resultUrl.isBlank()) {
                return ApiResponse.fail("AI 服务暂未配置，请联系管理员");
            }
            return ApiResponse.ok(Map.of(
                    "resultUrl",  resultUrl,
                    "patternUrl", "",
                    "colorStats", ""
            ));
        } catch (Exception e) {
            return ApiResponse.fail("AI 生成失败：" + e.getMessage());
        }
    }
}
