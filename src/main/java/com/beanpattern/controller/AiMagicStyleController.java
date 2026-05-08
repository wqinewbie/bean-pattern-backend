package com.beanpattern.controller;

import com.beanpattern.entity.AiMagicStyle;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.AiMagicStyleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI魔法风格控制器
 */
@RestController
@RequestMapping("/api")
public class AiMagicStyleController {

    @Autowired
    private AiMagicStyleService service;

    /**
     * 小程序端：获取启用的魔法风格列表
     */
    @GetMapping("/ai/magic-styles")
    public ApiResponse<List<AiMagicStyle>> getStyles() {
        List<AiMagicStyle> styles = service.getEnabledStyles();
        return ApiResponse.ok(styles);
    }

    /**
     * 管理端：获取所有魔法风格列表
     */
    @GetMapping("/admin/ai-magic-style/list")
    public ApiResponse<List<AiMagicStyle>> list() {
        List<AiMagicStyle> styles = service.getAllStyles();
        return ApiResponse.ok(styles);
    }

    /**
     * 管理端：保存魔法风格
     */
    @PostMapping("/admin/ai-magic-style/save")
    public ApiResponse<Void> save(@RequestBody AiMagicStyle style) {
        service.save(style);
        return ApiResponse.ok();
    }

    /**
     * 管理端：删除魔法风格
     */
    @DeleteMapping("/admin/ai-magic-style/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
