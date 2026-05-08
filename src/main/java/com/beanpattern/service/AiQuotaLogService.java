package com.beanpattern.service;

import com.beanpattern.entity.AiQuotaLog;
import com.beanpattern.mapper.AiQuotaLogMapper;
import com.beanpattern.mapper.UserMapper;
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

        Integer balanceBefore = user.getAiQuota();
        Integer balanceAfter = balanceBefore + changeAmount;

        // 记录日志
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
}
