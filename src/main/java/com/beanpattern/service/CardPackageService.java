package com.beanpattern.service;

import com.beanpattern.entity.CardPackage;
import com.beanpattern.mapper.CardPackageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 次卡套餐配置服务
 */
@Service
public class CardPackageService {

    private final CardPackageMapper cardPackageMapper;
    private final VipService vipService;

    public CardPackageService(CardPackageMapper cardPackageMapper, VipService vipService) {
        this.cardPackageMapper = cardPackageMapper;
        this.vipService = vipService;
    }

    /**
     * 获取所有启用的次卡套餐（用户端）
     */
    public List<CardPackage> listActivePackages() {
        return cardPackageMapper.listActive();
    }

    /**
     * 获取所有启用的次卡套餐（用户端，根据用户会员状态过滤）
     */
    public List<CardPackage> listActivePackagesForUser(Long userId) {
        List<CardPackage> packages = cardPackageMapper.listActive();

        // 如果用户未登录，过滤掉所有仅会员可购买的套餐
        if (userId == null) {
            return packages.stream()
                    .filter(pkg -> pkg.getVipOnly() == null || !pkg.getVipOnly())
                    .collect(Collectors.toList());
        }

        // 检查用户是否是会员
        boolean isVip = vipService.isVip(userId);

        // 如果用户不是会员，过滤掉仅会员可购买的套餐
        if (!isVip) {
            return packages.stream()
                    .filter(pkg -> pkg.getVipOnly() == null || !pkg.getVipOnly())
                    .collect(Collectors.toList());
        }

        return packages;
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
