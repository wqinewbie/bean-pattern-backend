package com.beanpattern.service;

import com.beanpattern.entity.PrivilegeConfig;
import com.beanpattern.mapper.PrivilegeConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 权益配置服务
 */
@Service
public class PrivilegeConfigService {

    private final PrivilegeConfigMapper privilegeConfigMapper;

    public PrivilegeConfigService(PrivilegeConfigMapper privilegeConfigMapper) {
        this.privilegeConfigMapper = privilegeConfigMapper;
    }

    /**
     * 获取所有启用的权益配置（用户端）
     */
    public List<PrivilegeConfig> listActiveConfigs() {
        return privilegeConfigMapper.listActive();
    }

    /**
     * 获取所有权益配置（管理后台）
     */
    public List<PrivilegeConfig> listAllConfigs() {
        return privilegeConfigMapper.listAll();
    }

    /**
     * 根据ID获取权益配置
     */
    public PrivilegeConfig getById(Long id) {
        return privilegeConfigMapper.findById(id);
    }

    /**
     * 根据配置键获取权益配置
     */
    public PrivilegeConfig getByKey(String configKey) {
        return privilegeConfigMapper.findByKey(configKey);
    }

    /**
     * 更新权益配置
     */
    @Transactional
    public PrivilegeConfig update(PrivilegeConfig privilegeConfig) {
        PrivilegeConfig existing = privilegeConfigMapper.findById(privilegeConfig.getId());
        if (existing == null) {
            throw new IllegalArgumentException("权益配置不存在");
        }

        privilegeConfigMapper.update(privilegeConfig);
        return privilegeConfigMapper.findById(privilegeConfig.getId());
    }
}
