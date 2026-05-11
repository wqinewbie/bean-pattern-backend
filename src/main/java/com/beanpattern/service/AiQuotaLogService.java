package com.beanpattern.service;

import com.beanpattern.entity.AiQuotaLog;
import com.beanpattern.mapper.AiQuotaLogMapper;
import com.beanpattern.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI次数变动日志服务
 */
@Service
public class AiQuotaLogService {

    private final AiQuotaLogMapper aiQuotaLogMapper;
    private final UserMapper userMapper;

    public AiQuotaLogService(AiQuotaLogMapper aiQuotaLogMapper, UserMapper userMapper) {
        this.aiQuotaLogMapper = aiQuotaLogMapper;
        this.userMapper = userMapper;
    }

    /**
     * 记录AI次数变动
     *
     * @param userId 用户ID
     * @param changeType 变更类型：PURCHASE/GIFT/USE/REFUND
     * @param changeAmount 变更数量（正数增加，负数减少）
     * @param bizType 业务类型：ORDER/ACTIVITY/ADMIN
     * @param bizId 业务ID
     * @param description 描述
     */
    @Transactional
    public void logChange(Long userId, String changeType, Integer changeAmount,
                         String bizType, String bizId, String description) {
        // 获取当前余额
        var user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        Integer balanceAfter = user.getAiQuota() != null ? user.getAiQuota() : 0;
        Integer balanceBefore = balanceAfter - changeAmount;

        AiQuotaLog log = AiQuotaLog.builder()
                .userId(userId)
                .changeType(changeType)
                .changeAmount(changeAmount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .bizType(bizType)
                .bizId(bizId)
                .description(description)
                .build();

        aiQuotaLogMapper.insert(log);
    }

    /**
     * 尝试记录AI次数变动，重复业务日志返回 false
     */
    @Transactional
    public boolean tryLogChange(Long userId, String changeType, Integer changeAmount,
                                String bizType, String bizId, String description) {
        try {
            logChange(userId, changeType, changeAmount, bizType, bizId, description);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    /**
     * 获取用户的AI次数变动记录
     */
    public List<AiQuotaLog> getUserLogs(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return aiQuotaLogMapper.listByUserId(userId, offset, pageSize);
    }

    /**
     * 获取用户的AI次数变动记录总数
     */
    public int getUserLogsCount(Long userId) {
        return aiQuotaLogMapper.countByUserId(userId);
    }

    /**
     * 检查业务是否已记录过次数扣减
     */
    public boolean hasLoggedBiz(Long userId, String changeType, String bizType, String bizId) {
        if (userId == null || bizType == null || bizId == null || bizId.isEmpty()) {
            return false;
        }
        return aiQuotaLogMapper.countByBiz(userId, changeType, bizType, bizId) > 0;
    }
}
