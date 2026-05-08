package com.beanpattern.service;

import com.beanpattern.entity.VipPackage;
import com.beanpattern.mapper.VipPackageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会员套餐配置服务
 */
@Service
public class VipPackageService {

    private final VipPackageMapper vipPackageMapper;

    public VipPackageService(VipPackageMapper vipPackageMapper) {
        this.vipPackageMapper = vipPackageMapper;
    }

    /**
     * 获取所有启用的会员套餐（用户端）
     */
    public List<VipPackage> listActivePackages() {
        return vipPackageMapper.listActive();
    }

    /**
     * 获取所有会员套餐（管理后台）
     */
    public List<VipPackage> listAllPackages() {
        return vipPackageMapper.listAll();
    }

    /**
     * 根据ID获取套餐
     */
    public VipPackage getById(Long id) {
        return vipPackageMapper.findById(id);
    }

    /**
     * 根据套餐代码获取套餐
     */
    public VipPackage getByCode(String packageCode) {
        return vipPackageMapper.findByCode(packageCode);
    }

    /**
     * 创建会员套餐
     */
    @Transactional
    public VipPackage create(VipPackage vipPackage) {
        // 检查套餐代码是否已存在
        VipPackage existing = vipPackageMapper.findByCode(vipPackage.getPackageCode());
        if (existing != null) {
            throw new IllegalArgumentException("套餐代码已存在");
        }

        vipPackageMapper.insert(vipPackage);
        return vipPackage;
    }

    /**
     * 更新会员套餐
     */
    @Transactional
    public VipPackage update(VipPackage vipPackage) {
        VipPackage existing = vipPackageMapper.findById(vipPackage.getId());
        if (existing == null) {
            throw new IllegalArgumentException("套餐不存在");
        }

        // 如果修改了套餐代码，检查新代码是否已被使用
        if (!existing.getPackageCode().equals(vipPackage.getPackageCode())) {
            VipPackage codeCheck = vipPackageMapper.findByCode(vipPackage.getPackageCode());
            if (codeCheck != null) {
                throw new IllegalArgumentException("套餐代码已存在");
            }
        }

        vipPackageMapper.update(vipPackage);
        return vipPackageMapper.findById(vipPackage.getId());
    }

    /**
     * 启用/禁用套餐
     */
    @Transactional
    public void updateStatus(Long id, Boolean isActive) {
        VipPackage existing = vipPackageMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("套餐不存在");
        }
        vipPackageMapper.updateStatus(id, isActive);
    }

    /**
     * 删除套餐
     */
    @Transactional
    public void delete(Long id) {
        VipPackage existing = vipPackageMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("套餐不存在");
        }
        vipPackageMapper.deleteById(id);
    }
}
