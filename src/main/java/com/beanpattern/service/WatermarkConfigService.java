package com.beanpattern.service;

import com.beanpattern.entity.WatermarkConfig;
import com.beanpattern.mapper.WatermarkConfigMapper;
import org.springframework.stereotype.Service;

@Service
public class WatermarkConfigService {

    private final WatermarkConfigMapper mapper;

    public WatermarkConfigService(WatermarkConfigMapper mapper) {
        this.mapper = mapper;
    }

    public WatermarkConfig getConfig() {
        WatermarkConfig config = mapper.getConfig();
        if (config == null) {
            config = getDefaultConfig();
        }
        return config;
    }

    public WatermarkConfig save(WatermarkConfig config) {
        WatermarkConfig existing = mapper.getConfig();
        if (existing != null) {
            config.setId(existing.getId());
            mapper.update(config);
        } else {
            mapper.insert(config);
        }
        return config;
    }

    public WatermarkConfig getDefaultConfig() {
        WatermarkConfig config = new WatermarkConfig();
        config.setEnabled(1);
        config.setText("拼豆小程序");
        config.setFontSize(24);
        config.setColor("rgba(128,128,128,0.5)");
        config.setPosition("右下");
        config.setOpacity(0.5);
        config.setMargin(20);
        return config;
    }
}
