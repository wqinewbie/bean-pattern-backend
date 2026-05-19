package com.beanpattern.service;

import com.beanpattern.entity.VipPackage;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.VipPackageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员套餐配置服务
 */
@Service
public class VipPackageService {

    private final VipPackageMapper vipPackageMapper;
    private final VipService vipService;
    private final OrderMapper orderMapper;

    public VipPackageService(VipPackageMapper vipPackageMapper, VipService vipService, OrderMapper orderMapper) {
        this.vipPackageMapper = vipPackageMapper;
        this.vipService = vipService;
        this.orderMapper = orderMapper;
    }

    /**
     * 获取所有启用的会员套餐（用户端）
     */
    public List<VipPackage> listActivePackages() {
        return vipPackageMapper.listActive();
    }

    /**
     * 获取所有启用的会员套餐（用户端，根据用户会员状态过滤）
     */
    public List<VipPackage> listActivePackagesForUser(Long userId) {
        List<VipPackage> packages = vipPackageMapper.listActive();

        // 如果用户未登录，过滤掉所有仅会员可购买的套餐（且不计算剩余可购次数）
        if (userId == null) {
            return packages.stream()
                    .filter(pkg -> pkg.getVipOnly() == null || !pkg.getVipOnly())
                    .collect(Collectors.toList());
        }

        // 检查用户是否是会员
        boolean isVip = vipService.isVip(userId);

        // 如果用户不是会员，过滤掉仅会员可购买的套餐
        if (!isVip) {
            packages = packages.stream()
                    .filter(pkg -> pkg.getVipOnly() == null || !pkg.getVipOnly())
                    .collect(Collectors.toList());
        }

        // 计算每个套餐的剩余可购次数（NULL=不限购）
        for (VipPackage pkg : packages) {
            Integer purchaseLimit = pkg.getPurchaseLimit();
            if (purchaseLimit == null || purchaseLimit <= 0) {
                pkg.setRemainingPurchaseCount(null);
                continue;
            }
            int purchaseCount = orderMapper.countUserPurchase(userId, "vip", pkg.getPackageCode());
            pkg.setRemainingPurchaseCount(Math.max(purchaseLimit - purchaseCount, 0));
        }

        return packages;
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
        return vipPackageMapper.findById(vipPackage.getId());
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
