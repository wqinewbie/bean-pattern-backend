package com.beanpattern.service;

import com.beanpattern.entity.PopupConfig;
import com.beanpattern.mapper.PopupConfigMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PopupConfigService {

    private final PopupConfigMapper mapper;

    public PopupConfigService(PopupConfigMapper mapper) {
        this.mapper = mapper;
    }

    public List<PopupConfig> getActivePopups() {
        return mapper.findActive();
    }

    public List<PopupConfig> getAll() {
        return mapper.findAll();
    }

    public PopupConfig getByKey(String key) {
        return mapper.findByKey(key);
    }

    public void save(PopupConfig config) {
        if (config.getId() != null) {
            mapper.update(config);
        } else {
            mapper.insert(config);
        }
    }

    public void delete(Long id) {
        mapper.delete(id);
    }
}
