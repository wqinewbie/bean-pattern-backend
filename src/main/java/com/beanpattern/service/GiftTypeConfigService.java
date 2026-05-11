package com.beanpattern.service;

import com.beanpattern.entity.GiftTypeConfig;
import com.beanpattern.mapper.GiftTypeConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GiftTypeConfigService {

    private final GiftTypeConfigMapper giftTypeConfigMapper;

    public GiftTypeConfigService(GiftTypeConfigMapper giftTypeConfigMapper) {
        this.giftTypeConfigMapper = giftTypeConfigMapper;
    }

    public List<GiftTypeConfig> listAll() {
        return giftTypeConfigMapper.findAll();
    }

    public List<GiftTypeConfig> listActive() {
        return giftTypeConfigMapper.findAllActive();
    }

    public GiftTypeConfig save(GiftTypeConfig giftTypeConfig) {
        if (!StringUtils.hasText(giftTypeConfig.getCode())) {
            throw new IllegalArgumentException("礼品类型编码不能为空");
        }
        if (!StringUtils.hasText(giftTypeConfig.getName())) {
            throw new IllegalArgumentException("礼品类型名称不能为空");
        }
        if (!StringUtils.hasText(giftTypeConfig.getValueType())) {
            giftTypeConfig.setValueType("number");
        }
        if (giftTypeConfig.getStatus() == null) {
            giftTypeConfig.setStatus(1);
        }
        if (giftTypeConfig.getSortOrder() == null) {
            giftTypeConfig.setSortOrder(0);
        }
        if (giftTypeConfig.getId() == null) {
            giftTypeConfigMapper.insert(giftTypeConfig);
        } else {
            giftTypeConfigMapper.update(giftTypeConfig);
        }
        return giftTypeConfig;
    }

    public void toggleStatus(Long id) {
        giftTypeConfigMapper.toggleStatus(id);
    }
}
