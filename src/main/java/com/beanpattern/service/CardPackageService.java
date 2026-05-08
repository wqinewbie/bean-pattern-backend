package com.beanpattern.service;

import com.beanpattern.entity.CardPackage;
import com.beanpattern.mapper.CardPackageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 次卡套餐配置服务
 */
@Service
public class CardPackageService {

    private final CardPackageMapper cardPackageMapper;

    public CardPackageService(CardPackageMapper cardPackageMapper) {
        this.cardPackageMapper = cardPackageMapper;
    }

    /**
     * 获取所有启用的次卡套餐（用户端）
     */
    public List<CardPackage> listActivePackages() {
        return cardPackageMapper.listActive();
    }

    /**
     * 获取所有次卡套餐（管理后台）
     */
    public List<CardPackage> listAllPackages() {
        return cardPackageMapper.listAll();
    }

    /**
     * 根据ID获取套餐
     */
    public CardPackage getById(Long id) {
        return cardPackageMapper.findById(id);
    }

    /**
     * 根据套餐代码获取套餐
     */
    public CardPackage getByCode(String packageCode) {
        return cardPackageMapper.findByCode(packageCode);
    }

    /**
     * 创建次卡套餐
     */
    @Transactional
    public CardPackage create(CardPackage cardPackage) {
        // 检查套餐代码是否已存在
        CardPackage existing = cardPackageMapper.findByCode(cardPackage.getPackageCode());
        if (existing != null) {
            throw new IllegalArgumentException("套餐代码已存在");
        }

        cardPackageMapper.insert(cardPackage);
        return cardPackage;
    }

    /**
     * 更新次卡套餐
     */
    @Transactional
    public CardPackage update(CardPackage cardPackage) {
        CardPackage existing = cardPackageMapper.findById(cardPackage.getId());
        if (existing == null) {
            throw new IllegalArgumentException("套餐不存在");
        }

        // 如果修改了套餐代码，检查新代码是否已被使用
        if (!existing.getPackageCode().equals(cardPackage.getPackageCode())) {
            CardPackage codeCheck = cardPackageMapper.findByCode(cardPackage.getPackageCode());
            if (codeCheck != null) {
                throw new IllegalArgumentException("套餐代码已存在");
            }
        }

        cardPackageMapper.update(cardPackage);
        return cardPackageMapper.findById(cardPackage.getId());
    }

    /**
     * 启用/禁用套餐
     */
    @Transactional
    public void updateStatus(Long id, Boolean isActive) {
        CardPackage existing = cardPackageMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("套餐不存在");
        }
        cardPackageMapper.updateStatus(id, isActive);
    }

    /**
     * 删除套餐
     */
    @Transactional
    public void delete(Long id) {
        CardPackage existing = cardPackageMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("套餐不存在");
        }
        cardPackageMapper.deleteById(id);
    }
}
