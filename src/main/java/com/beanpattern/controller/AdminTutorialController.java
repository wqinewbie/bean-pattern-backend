package com.beanpattern.controller;

import com.beanpattern.entity.TutorialEntity;
import com.beanpattern.mapper.TutorialMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.ImageUploadResponse;
import com.beanpattern.service.ImageStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/tutorials")
public class AdminTutorialController {

    private final TutorialMapper tutorialMapper;
    private final ImageStorageService imageStorageService;

    public AdminTutorialController(TutorialMapper tutorialMapper, ImageStorageService imageStorageService) {
        this.tutorialMapper = tutorialMapper;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(tutorialMapper.listAll().stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("title", t.getTitle());
            m.put("description", t.getDescription() != null ? t.getDescription() : "");
            m.put("videoUrl", t.getVideoUrl() != null ? t.getVideoUrl() : "");
            m.put("thumbnailUrl", t.getThumbnailUrl() != null ? t.getThumbnailUrl() : "");
            m.put("sortOrder", t.getSortOrder());
            m.put("status", t.getStatus());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> body) {
        TutorialEntity t = new TutorialEntity();
        t.setTitle((String) body.getOrDefault("title", ""));
        t.setDescription((String) body.getOrDefault("description", ""));
        t.setVideoUrl((String) body.getOrDefault("videoUrl", ""));
        t.setThumbnailUrl((String) body.getOrDefault("thumbnailUrl", ""));
        t.setSortOrder(body.get("sortOrder") instanceof Number n ? n.intValue() : 0);
        t.setStatus(1);
        tutorialMapper.insert(t);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        TutorialEntity t = new TutorialEntity();
        t.setId(id);
        t.setTitle((String) body.getOrDefault("title", ""));
        t.setDescription((String) body.getOrDefault("description", ""));
        t.setVideoUrl((String) body.getOrDefault("videoUrl", ""));
        t.setThumbnailUrl((String) body.getOrDefault("thumbnailUrl", ""));
        t.setSortOrder(body.get("sortOrder") instanceof Number n ? n.intValue() : 0);
        tutorialMapper.update(t);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/toggle")
    public ApiResponse<Void> toggle(@PathVariable Long id) {
        tutorialMapper.toggleStatus(id);
        return ApiResponse.ok(null);
    }

    @PostMapping(value = "/video-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(imageStorageService.store(file));
    }
}
