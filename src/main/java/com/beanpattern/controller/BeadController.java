package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.ImageTaskEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.AiImageService;
import com.beanpattern.service.BeadColorService;
import com.beanpattern.service.TaskRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bead")
public class BeadController {

    private final SessionHelper sessionHelper;
    private final TaskRecordService taskRecordService;
    private final AiImageService aiImageService;
    private final BeadColorService beadColorService;

    public BeadController(SessionHelper sessionHelper,
                          TaskRecordService taskRecordService,
                          AiImageService aiImageService,
                          BeadColorService beadColorService) {
        this.sessionHelper = sessionHelper;
        this.taskRecordService = taskRecordService;
        this.aiImageService = aiImageService;
        this.beadColorService = beadColorService;
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

    /** POST /api/bead/pattern-local */
    @PostMapping("/pattern-local")
    public ApiResponse<Map<String, Object>> patternLocal(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String imageUrl   = body.getOrDefault("imageUrl", "");
        String resultUrl  = body.getOrDefault("resultUrl", "");
        String patternUrl = body.getOrDefault("patternUrl", "");
        String colorStats = body.getOrDefault("colorStats", "");
        var user = sessionHelper.requireCompleteProfileUser(request);
        long taskId = -1;
        if (user != null && !imageUrl.isBlank()) {
            var task = taskRecordService.createTask(
                    user.getId(), TaskRecordService.TASK_BEAD_LOCAL, imageUrl);
            taskId = task.getId();
            if (!resultUrl.isBlank()) {
                taskRecordService.markSuccess(taskId, resultUrl, patternUrl, colorStats);
            }
        }
        return ApiResponse.ok(Map.of(
                "taskId", taskId,
                "resultUrl", resultUrl,
                "patternUrl", patternUrl,
                "colorStats", colorStats
        ));
    }

    /** POST /api/bead/pattern-ai */
    @PostMapping("/pattern-ai")
    public ApiResponse<Map<String, Object>> patternAi(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String imageUrl = body.getOrDefault("imageUrl", "");
        if (imageUrl.isBlank()) return ApiResponse.fail("imageUrl 不能为空");
        var user = sessionHelper.requireCompleteProfileUser(request);
        long taskId = -1;
        if (user != null) {
            var task = taskRecordService.createTask(
                    user.getId(), TaskRecordService.TASK_BEAD_AI, imageUrl);
            taskId = task.getId();
        }
        try {
            String resultUrl = aiImageService.processImage(imageUrl);
            if (taskId > 0) taskRecordService.markSuccess(taskId, resultUrl);
            return ApiResponse.ok(Map.of(
                    "taskId", taskId,
                    "resultUrl", resultUrl,
                    "patternUrl", ""
            ));
        } catch (Exception e) {
            if (taskId > 0) taskRecordService.markFailed(taskId, e.getMessage());
            return ApiResponse.fail("AI 处理失败：" + e.getMessage());
        }
    }

    /** GET /api/bead/task/{id} */
    @GetMapping("/task/{id}")
    public ApiResponse<Map<String, Object>> taskDetail(
            @PathVariable("id") Long id,
            HttpServletRequest request) {
        ImageTaskEntity task = taskRecordService.findById(id);
        if (task == null) return ApiResponse.fail("任务不存在");
        return ApiResponse.ok(Map.of(
                "taskId",      task.getId(),
                "taskType",    task.getTaskType(),
                "status",      task.getStatus(),
                "originalUrl", task.getSourceUrl()  != null ? task.getSourceUrl()  : "",
                "resultUrl",   task.getResultUrl()  != null ? task.getResultUrl()  : "",
                "patternUrl",  task.getPatternUrl() != null ? task.getPatternUrl() : "",
                "colorStats",  task.getColorStats() != null ? task.getColorStats() : ""
        ));
    }

    /** POST /api/bead/pattern-ai-text - 文字生成拼豆图纸 */
    @PostMapping("/pattern-ai-text")
    public ApiResponse<Map<String, Object>> patternAiText(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String prompt = (String) body.getOrDefault("prompt", "");
        String style  = (String) body.getOrDefault("style",  "标准");
        int size = body.get("size") instanceof Number n ? n.intValue() : 64;
        if (prompt.isBlank()) return ApiResponse.fail("prompt 不能为空");
        var user = sessionHelper.requireCompleteProfileUser(request);
        long taskId = -1;
        if (user != null) {
            var task = taskRecordService.createTask(
                    user.getId(), TaskRecordService.TASK_BEAD_AI,
                    "text:" + prompt.substring(0, Math.min(prompt.length(), 50)));
            taskId = task.getId();
        }
        try {
            String resultUrl = aiImageService.generateFromText(prompt, style, size);
            if (resultUrl.isBlank()) {
                if (taskId > 0) taskRecordService.markFailed(taskId, "AI未配置");
                return ApiResponse.fail("AI 服务暂未配置，请联系管理员");
            }
            if (taskId > 0) taskRecordService.markSuccess(taskId, resultUrl);
            return ApiResponse.ok(Map.of(
                    "taskId",     taskId,
                    "resultUrl",  resultUrl,
                    "patternUrl", "",
                    "colorStats", ""
            ));
        } catch (Exception e) {
            if (taskId > 0) taskRecordService.markFailed(taskId, e.getMessage());
            return ApiResponse.fail("AI 生成失败：" + e.getMessage());
        }
    }
}
