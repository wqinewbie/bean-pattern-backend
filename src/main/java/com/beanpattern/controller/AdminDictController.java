package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.dict.DictOption;
import com.beanpattern.service.DictService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dict")
public class AdminDictController {
    private final DictService dictService;

    public AdminDictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/options")
    public ApiResponse<List<DictOption>> options(@RequestParam String dictType) {
        return ApiResponse.ok(dictService.getOptions(dictType));
    }

    @GetMapping("/options/batch")
    public ApiResponse<Map<String, List<DictOption>>> optionsBatch(@RequestParam String dictTypes) {
        List<String> types = Arrays.stream(dictTypes.split(","))
                .map(String::trim)
                .filter(type -> !type.isEmpty())
                .distinct()
                .toList();
        return ApiResponse.ok(dictService.getOptionsBatch(types));
    }

    /**
     * 在库中增删改字典项后调用，无需重启即可刷新管理端下拉缓存。
     */
    @PostMapping("/reload-cache")
    public ApiResponse<String> reloadCache() {
        dictService.refreshCache();
        return ApiResponse.ok("ok");
    }
}
