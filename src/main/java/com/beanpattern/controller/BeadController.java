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
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@RestController
@RequestMapping("/api/bead")
public class BeadController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    @GetMapping("/brands")
    public ApiResponse<Map<String, Object>> getBrands() {
        List<String> brands = beadColorService.getBrandNames();
        Map<String, Object> result = new LinkedHashMap<>();
        for (String b : brands) {
            result.put(b, beadColorService.getKits(b));
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/generate-result")
    public ApiResponse<Map<String, Object>> generateResult(@RequestBody Map<String, Object> body) {
        String imageUrl = (String) body.getOrDefault("imageUrl", "");
        int gridSize = body.get("gridSize") instanceof Number n ? n.intValue() : 64;

        if (imageUrl.isBlank()) {
            return ApiResponse.fail("imageUrl is required");
        }

        try {
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
            return ApiResponse.fail("image process failed: " + e.getMessage());
        }
    }

    @PostMapping("/generate-pattern")
    public ApiResponse<Map<String, Object>> generatePattern(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<List<List<Integer>>> rawGrid = (List<List<List<Integer>>>) body.get("rgbData");
        String brand = (String) body.getOrDefault("brand", "mard");
        String algo = (String) body.getOrDefault("algo", "standard");
        int colorCount = body.get("colorCount") instanceof Number n ? n.intValue() : 0;
        String sourceUrl = (String) body.getOrDefault("sourceUrl", "");

        if (rawGrid == null || rawGrid.isEmpty()) {
            return ApiResponse.fail("rgbData is required");
        }

        try {
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

            List<List<Map<String, Object>>> mappedPixelData = new ArrayList<>();
            Map<String, Integer> statCount = new LinkedHashMap<>();
            Map<String, Map<String, Object>> statMeta = new LinkedHashMap<>();

            for (BeadColorService.BeadColor[] row : matched) {
                List<Map<String, Object>> rowList = new ArrayList<>();
                for (BeadColorService.BeadColor c : row) {
                    String hex = String.format("#%02X%02X%02X", c.r(), c.g(), c.b());
                    rowList.add(Map.of(
                            "id", c.id(),
                            "name", c.name(),
                            "r", c.r(),
                            "g", c.g(),
                            "b", c.b(),
                            "hex", hex,
                            "isExternal", false
                    ));

                    statCount.put(c.id(), statCount.getOrDefault(c.id(), 0) + 1);
                    if (!statMeta.containsKey(c.id())) {
                        statMeta.put(c.id(), Map.of(
                                "id", c.id(),
                                "name", c.name(),
                                "r", c.r(),
                                "g", c.g(),
                                "b", c.b(),
                                "hex", hex
                        ));
                    }
                }
                mappedPixelData.add(rowList);
            }

            List<Map<String, Object>> colorStats = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : statCount.entrySet()) {
                Map<String, Object> meta = statMeta.get(entry.getKey());
                Map<String, Object> row = new LinkedHashMap<>(meta);
                row.put("count", entry.getValue());
                colorStats.add(row);
            }

            Long historyId = null;
            try {
                UserEntity user = sessionHelper.resolveUser(request);
                if (user != null) {
                    BpHistory history = new BpHistory();
                    history.setUserId(user.getId());
                    history.setSourceType("LOCAL");
                    history.setBrand(brand);
                    history.setColorCount(colorStats.size());
                    history.setGridSize(rows);
                    history.setMappedPixelData(OBJECT_MAPPER.writeValueAsString(mappedPixelData));
                    history.setSourceUrl(sourceUrl);
                    bpHistoryService.insert(history);
                    historyId = history.getId();
                }
            } catch (Exception e) {
                System.err.println("save history failed: " + e.getMessage());
            }

            return ApiResponse.ok(Map.of(
                    "mappedPixelData", mappedPixelData,
                    "colorStats", colorStats,
                    "gridSize", rows,
                    "colorCount", colorStats.size(),
                    "historyId", historyId != null ? historyId : 0
            ));
        } catch (Exception e) {
            return ApiResponse.fail("generate pattern failed: " + e.getMessage());
        }
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
