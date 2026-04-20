package com.beanpattern.service;

import com.beanpattern.entity.UserEntity;
import com.beanpattern.entity.UserVipRecord;
import com.beanpattern.entity.VipProduct;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.mapper.UserVipRecordMapper;
import com.beanpattern.mapper.VipProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VIP会员服务
 */
@Service
public class VipService {

    private final UserMapper userMapper;
    private final UserVipRecordMapper userVipRecordMapper;
    private final VipProductMapper vipProductMapper;

    public VipService(UserMapper userMapper, 
                      UserVipRecordMapper userVipRecordMapper,
                      VipProductMapper vipProductMapper) {
        this.userMapper = userMapper;
        this.userVipRecordMapper = userVipRecordMapper;
        this.vipProductMapper = vipProductMapper;
    }

    /**
     * 获取当前生效的VIP记录
     */
    public UserVipRecord getActiveVipRecord(Long userId) {
        return userVipRecordMapper.findActiveByUserId(userId, LocalDateTime.now());
    }

    /**
     * 获取用户VIP等级
     */
    public int getUserVipLevel(Long userId) {
        UserVipRecord record = getActiveVipRecord(userId);
        return record != null ? record.getVipLevel() : 0;
    }

    /**
     * 获取VIP产品列表
     */
    public List<VipProduct> getActiveProducts() {
        return vipProductMapper.findAllActive();
    }

    /**
     * 根据编码获取VIP产品
     */
    public VipProduct getProductByCode(String productCode) {
        return vipProductMapper.findByCode(productCode);
    }

    /**
     * 激活VIP
     */
    @Transactional
    public void activateVip(Long userId, Long productId, String orderNo, Long orderId) {
        VipProduct product = vipProductMapper.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("VIP产品不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusDays(product.getValidDays());

        // 创建VIP记录
        UserVipRecord record = new UserVipRecord();
        record.setUserId(userId);
        record.setProductId(productId);
        record.setProductCode(product.getProductCode());
        record.setVipLevel(product.getVipLevel());
        record.setOrderNo(orderNo);
        record.setOrderId(orderId);
        record.setStartAt(now);
        record.setExpireAt(expireAt);
        userVipRecordMapper.insert(record);

        // 更新用户VIP信息
        userMapper.updateVip(userId, product.getVipLevel(), expireAt);

        // 更新用户配额
        updateUserQuota(userId, product);
    }

    /**
     * 更新用户配额
     */
    private void updateUserQuota(Long userId, VipProduct product) {
        // 更新存储配额
        if (product.getStorageQuota() != null) {
            userMapper.updateStorageQuota(userId, product.getStorageQuota());
        }
        // 更新草稿配额
        if (product.getDraftQuota() != null) {
            userMapper.updateDraftQuota(userId, product.getDraftQuota());
        }
        // 更新可用品牌
        if (product.getAvailableBrands() != null) {
            userMapper.updateAvailableBrands(userId, product.getAvailableBrands());
        }
    }

    /**
     * 检查并重置AI配额
     */
    @Transactional
    public void checkAndResetAiQuota(Long userId) {
        UserVipRecord record = getActiveVipRecord(userId);
        if (record == null || record.getAiResetAt() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReset = record.getAiResetAt().plusMonths(1);

        if (now.isAfter(nextReset) || now.isEqual(nextReset)) {
            // 重置AI使用次数
            userVipRecordMapper.resetAiUsedCount(record.getId());
            // 更新重置时间
            userVipRecordMapper.updateAiResetAt(record.getId(), now);
        }
    }

    /**
     * 使用AI次数
     */
    @Transactional
    public boolean useAiQuota(Long userId) {
        UserVipRecord record = getActiveVipRecord(userId);
        if (record == null) {
            return false;
        }

        // 检查是否还有配额（ai_used_count < ai_quota）
        VipProduct product = vipProductMapper.findById(record.getProductId());
        if (product == null) {
            return false;
        }
        
        Integer aiQuota = product.getAiQuotaPerMonth();
        if (aiQuota == null || aiQuota <= 0) {
            return false;
        }
        
        if (record.getAiUsedCount() >= aiQuota) {
            return false;
        }

        userVipRecordMapper.incrementAiUsedCount(record.getId());
        return true;
    }

    /**
     * 获取用户VIP历史记录
     */
    public List<UserVipRecord> getUserVipHistory(Long userId) {
        return userVipRecordMapper.findByUserId(userId);
    }
    
    /**
     * 获取用户配额信息
     */
    public UserEntity getUserQuotaInfo(Long userId) {
        return userMapper.findById(userId);
    }
}
