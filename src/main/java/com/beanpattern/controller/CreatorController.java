package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.CreatorPatternEntity;
import com.beanpattern.mapper.CreatorPatternMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.vo.PatternVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/creator")
public class CreatorController {

    private final CreatorPatternMapper creatorPatternMapper;
    private final SessionHelper sessionHelper;

    public CreatorController(CreatorPatternMapper creatorPatternMapper,
                             SessionHelper sessionHelper) {
        this.creatorPatternMapper = creatorPatternMapper;
        this.sessionHelper = sessionHelper;
    }

    @GetMapping("/patterns")
    public ApiResponse<List<PatternVO>> listPublic(
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(
                creatorPatternMapper.listPublic(limit).stream()
                        .map(PatternVO::from)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/my-patterns")
    public ApiResponse<List<PatternVO>> myPatterns(HttpServletRequest request) {
        var user = sessionHelper.requirePhoneBoundUser(request);
        return ApiResponse.ok(
                creatorPatternMapper.listByUser(user.getId()).stream()
                        .map(PatternVO::from)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/patterns/{id}")
    public ApiResponse<PatternVO> detail(@PathVariable Long id) {
        CreatorPatternEntity p = creatorPatternMapper.findById(id);
        if (p == null) return ApiResponse.fail("图纸不存在");
        return ApiResponse.ok(PatternVO.from(p));
    }

    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest request) {
        var user = sessionHelper.requirePhoneBoundUser(request);

        String title = (String) body.getOrDefault("title", "");
        String coverUrl = (String) body.getOrDefault("coverUrl", "");
        String patternUrl = (String) body.getOrDefault("patternUrl", "");
        if (!StringUtils.hasText(title)) return ApiResponse.fail("标题不能为空");
        if (!StringUtils.hasText(coverUrl)) return ApiResponse.fail("封面图不能为空");
        if (!StringUtils.hasText(patternUrl)) return ApiResponse.fail("图纸文件不能为空");

        CreatorPatternEntity p = new CreatorPatternEntity();
        p.setUserId(user.getId());
        p.setTitle(title);
        p.setDescription((String) body.getOrDefault("description", ""));
        p.setCoverUrl(coverUrl);
        p.setPatternUrl(patternUrl);
        p.setGridSize((String) body.getOrDefault("gridSize", ""));
        p.setCategory((String) body.getOrDefault("category", ""));
        p.setTags((String) body.getOrDefault("tags", ""));
        p.setPriceCoins(body.get("priceCoins") instanceof Number n ? n.intValue() : 0);
        p.setDifficulty(body.get("difficulty") instanceof Number n ? n.intValue() : 1);
        p.setStatus(0);

        creatorPatternMapper.insert(p);
        return ApiResponse.ok(Map.of("id", p.getId(), "status", 0, "message", "已提交审核"));
    }
}
