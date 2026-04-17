package com.beanpattern.controller;

import com.beanpattern.mapper.TutorialMapper;
import com.beanpattern.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 小程序教程接口
 * GET /api/tutorial/list - 获取上线的教程列表
 */
@RestController
@RequestMapping("/api/tutorial")
public class TutorialController {

    private final TutorialMapper tutorialMapper;

    public TutorialController(TutorialMapper tutorialMapper) {
        this.tutorialMapper = tutorialMapper;
    }

    @GetMapping("/list")
    public ApiResponse<List<TutorialVO>> list() {
        return ApiResponse.ok(
                tutorialMapper.listActive().stream()
                        .map(TutorialVO::from)
                        .collect(Collectors.toList())
        );
    }

    public static class TutorialVO {
        private Long id;
        private String title;
        private String description;
        private String videoUrl;
        private String thumbnailUrl;

        public static TutorialVO from(com.beanpattern.entity.TutorialEntity e) {
            TutorialVO vo = new TutorialVO();
            vo.id = e.getId();
            vo.title = e.getTitle();
            vo.description = e.getDescription();
            vo.videoUrl = e.getVideoUrl();
            vo.thumbnailUrl = e.getThumbnailUrl();
            return vo;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    }
}
