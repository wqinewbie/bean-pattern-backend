package com.beanpattern.service;

import com.beanpattern.entity.SysDictItem;
import com.beanpattern.mapper.SysDictItemMapper;
import com.beanpattern.model.dict.DictOption;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 系统字典：数据来自表 {@code bp_sys_dict_item}，内存缓存；库变更后可调用 {@link #refreshCache()} 或重启。
 */
@Service
@Order(100)
public class DictService implements ApplicationRunner {

    private final SysDictItemMapper dictItemMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile Map<String, List<DictOption>> cache = Map.of();

    public DictService(SysDictItemMapper dictItemMapper) {
        this.dictItemMapper = dictItemMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        refreshCache();
    }

    public void refreshCache() {
        List<SysDictItem> rows = dictItemMapper.findAllEnabled();
        Map<String, List<DictOption>> next = new LinkedHashMap<>();
        for (SysDictItem row : rows) {
            if (!StringUtils.hasText(row.getDictType())) {
                continue;
            }
            String typeKey = row.getDictType().trim();
            next.computeIfAbsent(typeKey, k -> new ArrayList<>()).add(toDictOption(row));
        }
        Map<String, List<DictOption>> frozen = new LinkedHashMap<>();
        next.forEach((k, v) -> frozen.put(k, List.copyOf(v)));
        lock.writeLock().lock();
        try {
            cache = frozen;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<DictOption> getOptions(String dictType) {
        if (!StringUtils.hasText(dictType)) {
            return List.of();
        }
        String key = dictType.trim();
        lock.readLock().lock();
        try {
            List<DictOption> list = cache.get(key);
            return list == null ? List.of() : new ArrayList<>(list);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, List<DictOption>> getOptionsBatch(List<String> dictTypes) {
        Map<String, List<DictOption>> result = new LinkedHashMap<>();
        for (String dictType : dictTypes) {
            if (StringUtils.hasText(dictType)) {
                String key = dictType.trim();
                result.put(key, getOptions(key));
            }
        }
        return result;
    }

    private static DictOption toDictOption(SysDictItem row) {
        String tag = StringUtils.hasText(row.getTagType()) ? row.getTagType() : "info";
        int sort = row.getSortOrder() != null ? row.getSortOrder() : 0;
        DictOption o = new DictOption(row.getDictLabel(), row.getDictValue(), tag, sort);
        if (Integer.valueOf(1).equals(row.getDisabled())) {
            o.setDisabled(true);
        }
        if (StringUtils.hasText(row.getRemark())) {
            o.setRemark(row.getRemark());
        }
        return o;
    }
}
