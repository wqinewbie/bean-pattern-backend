package com.beanpattern.controller;

import com.beanpattern.entity.AiSizePreset;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.AiSizePresetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AiSizePresetController {

    private final AiSizePresetService service;

    public AiSizePresetController(AiSizePresetService service) {
        this.service = service;
    }

    @GetMapping("/ai/size-presets")
    public ApiResponse<List<AiSizePreset>> enabledPresets() {
        return ApiResponse.ok(service.getEnabledPresets());
    }

    @GetMapping("/admin/ai-size-presets")
    public ApiResponse<List<AiSizePreset>> list() {
        return ApiResponse.ok(service.getAllPresets());
    }

    @PostMapping("/admin/ai-size-presets")
    public ApiResponse<Void> create(@RequestBody AiSizePreset preset) {
        preset.setId(null);
        service.save(preset);
        return ApiResponse.ok(null);
    }

    @PutMapping("/admin/ai-size-presets/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody AiSizePreset preset) {
        preset.setId(id);
        service.save(preset);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/admin/ai-size-presets/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok(null);
    }
}
