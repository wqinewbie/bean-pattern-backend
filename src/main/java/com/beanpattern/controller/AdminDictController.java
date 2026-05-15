package com.beanpattern.controller;

import com.beanpattern.entity.SysDictItem;
import com.beanpattern.mapper.SysDictItemMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.dict.DictOption;
import com.beanpattern.service.DictService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dict")
public class AdminDictController {
    private final DictService dictService;
    private final SysDictItemMapper dictItemMapper;

    public AdminDictController(DictService dictService, SysDictItemMapper dictItemMapper) {
        this.dictService = dictService;
        this.dictItemMapper = dictItemMapper;
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

    @GetMapping("/items")
    public ApiResponse<List<SysDictItem>> list() {
        return ApiResponse.ok(dictItemMapper.findAll());
    }

    @GetMapping("/items/{id}")
    public ApiResponse<SysDictItem> getById(@PathVariable Long id) {
        SysDictItem item = dictItemMapper.findById(id);
        if (item == null) {
            return ApiResponse.error("字典项不存在");
        }
        return ApiResponse.ok(item);
    }

    @PostMapping("/items")
    public ApiResponse<SysDictItem> create(@RequestBody SysDictItem item) {
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        if (item.getDisabled() == null) {
            item.setDisabled(0);
        }
        if (item.getSortOrder() == null) {
            item.setSortOrder(0);
        }
        dictItemMapper.insert(item);
        dictService.refreshCache();
        return ApiResponse.ok(item);
    }

    @PutMapping("/items/{id}")
    public ApiResponse<SysDictItem> update(@PathVariable Long id, @RequestBody SysDictItem item) {
        SysDictItem existing = dictItemMapper.findById(id);
        if (existing == null) {
            return ApiResponse.error("字典项不存在");
        }
        item.setId(id);
        dictItemMapper.update(item);
        dictService.refreshCache();
        return ApiResponse.ok(item);
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        SysDictItem existing = dictItemMapper.findById(id);
        if (existing == null) {
            return ApiResponse.error("字典项不存在");
        }
        dictItemMapper.deleteById(id);
        dictService.refreshCache();
        return ApiResponse.ok("删除成功");
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
