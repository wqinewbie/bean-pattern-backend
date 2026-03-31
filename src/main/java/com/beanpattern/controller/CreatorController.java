package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.CreatorPatternEntity;
import com.beanpattern.mapper.CreatorPatternMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.vo.PatternVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 创作者图纸市场接口
 * GET  /api/creator/patterns      - 图纸市场列表（公开上线，允许匿名）
 * GET  /api/creator/my-patterns   - 我上传的图纸（需登录，401）
 * GET  /api/creator/patterns/{id} - 图纸详情（允许匿名）
 * POST /api/creator/upload        - 上传新图纸（需登录，401）
 */
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

    /** 图纸市场（公开，允许匿名） */
    @GetMapping("/patterns")
    public ApiResponse<List<PatternVO>> listPublic(
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(
                creatorPatternMapper.listPublic(limit).stream()
                        .map(PatternVO::from)
                        .collect(Collectors.toList()));
    }

    /** 我上传的图纸（需登录） */
    @GetMapping("/my-patterns")
    public ApiResponse<List<PatternVO>> myPatterns(HttpServletRequest request) {
        var user = sessionHelper.requireUser(request);
        return ApiResponse.ok(
                creatorPatternMapper.listByUser(user.getId()).stream()
                        .map(PatternVO::from)
                        .collect(Collectors.toList()));
    }

    /** 图纸详情（允许匿名） */
    @GetMapping("/patterns/{id}")
    public ApiResponse<PatternVO> detail(@PathVariable Long id) {
        CreatorPatternEntity p = creatorPatternMapper.findById(id);
        if (p == null) return ApiResponse.fail("图纸不存在");
        return ApiResponse.ok(PatternVO.from(p));
    }

    /** 上传新图纸（需登录） */
    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest request) {
        var user = sessionHelper.requireUser(request);

        String title      = (String) body.getOrDefault("title", "");
        String coverUrl   = (String) body.getOrDefault("coverUrl", "");
        String patternUrl = (String) body.getOrDefault("patternUrl", "");
        if (!StringUtils.hasText(title))      return ApiResponse.fail("标题不能为空");
        if (!StringUtils.hasText(coverUrl))   return ApiResponse.fail("封面图不能为空");
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
