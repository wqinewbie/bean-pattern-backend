package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @deprecated 旧图纸接口，已废弃。请使用 /api/box/* 代替
 */
@RestController
@RequestMapping("/api/pattern")
@Deprecated
public class PatternController {

    /** 获取当前用户的图纸列表 */
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(List.of());
    }

    /** 保存图纸 */
    @PostMapping("/save")
    public ApiResponse<Void> save() {
        return ApiResponse.fail("已废弃，请使用 /api/box/save");
    }

    /** 删除图纸 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete() {
        return ApiResponse.fail("已废弃，请使用 /api/box/delete/{id}");
    }

    /** 获取单张图纸详情 */
    @GetMapping("/{id}")
    public ApiResponse<Void> detail() {
        return ApiResponse.fail("已废弃，请使用 /api/box/detail/{id}");
    }
}
