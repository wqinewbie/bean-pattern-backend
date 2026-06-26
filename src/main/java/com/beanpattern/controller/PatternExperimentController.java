package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.PatternExperimentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pattern/experimental")
public class PatternExperimentController {

    private final PatternExperimentService patternExperimentService;

    public PatternExperimentController(PatternExperimentService patternExperimentService) {
        this.patternExperimentService = patternExperimentService;
    }

    @PostMapping(value = "/compare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PatternExperimentService.CompareResult> compare(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "brand", defaultValue = "MARD") String brand,
            @RequestParam(value = "colorCount", defaultValue = "0") int colorCount,
            @RequestParam(value = "gridSize", defaultValue = "48") int gridSize,
            @RequestParam(value = "similarityThreshold", defaultValue = "0") int similarityThreshold,
            @RequestParam(value = "mirror", defaultValue = "false") boolean mirror) throws Exception {
        return ApiResponse.ok(patternExperimentService.compare(
                file.getBytes(),
                brand,
                colorCount,
                mirror,
                clampGridSize(gridSize),
                similarityThreshold
        ));
    }

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PatternExperimentService.PatternResult> process(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "brand", defaultValue = "MARD") String brand,
            @RequestParam(value = "colorCount", defaultValue = "0") int colorCount,
            @RequestParam(value = "gridSize", defaultValue = "48") int gridSize,
            @RequestParam(value = "similarityThreshold", defaultValue = "0") int similarityThreshold,
            @RequestParam(value = "mirror", defaultValue = "false") boolean mirror) throws Exception {
        return ApiResponse.ok(patternExperimentService.optimized(
                file.getBytes(),
                brand,
                colorCount,
                mirror,
                clampGridSize(gridSize),
                similarityThreshold
        ));
    }

    private int clampGridSize(int gridSize) {
        return Math.max(8, Math.min(160, gridSize));
    }
}
