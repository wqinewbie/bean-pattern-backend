package com.beanpattern.service;

import com.beanpattern.entity.ShareRecord;
import com.beanpattern.entity.ShareVisitor;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.ShareRecordMapper;
import com.beanpattern.mapper.ShareVisitorMapper;
import com.beanpattern.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分享服务
 */
@Service
public class ShareService {

    private static final int REWARD_THRESHOLD = 3; // 有效访问达到3次才给奖励

    private final ShareRecordMapper shareRecordMapper;
    private final ShareVisitorMapper shareVisitorMapper;
    private final UserMapper userMapper;
    private final TaskService taskService;

    public ShareService(ShareRecordMapper shareRecordMapper,
                       ShareVisitorMapper shareVisitorMapper,
                       UserMapper userMapper,
                       TaskService taskService) {
        this.shareRecordMapper = shareRecordMapper;
        this.shareVisitorMapper = shareVisitorMapper;
        this.userMapper = userMapper;
        this.taskService = taskService;
    }

    /**
     * 创建分享记录
     */
    public ShareRecord createShareRecord(Long userId, String shareScene, String targetId, String shareTicket) {
        ShareRecord record = new ShareRecord();
        record.setUserId(userId);
        record.setShareScene(shareScene);
        record.setTargetId(targetId);
        record.setShareTicket(shareTicket);
        shareRecordMapper.insert(record);
        return record;
    }

    /**
     * 记录访客访问
     */
    @Transactional
    public void recordVisitor(Long shareRecordId, Long shareUserId, String visitorOpenid, boolean isNewUser) {
        // 检查是否已记录过该访客
        ShareVisitor existing = shareVisitorMapper.findByRecordAndVisitor(shareRecordId, visitorOpenid);
        if (existing != null) {
            // 已记录，只更新时间
            return;
        }

        // 判断是否有效访问（新用户）
        ShareVisitor visitor = new ShareVisitor();
        visitor.setShareRecordId(shareRecordId);
        visitor.setShareUserId(shareUserId);
        visitor.setVisitorOpenid(visitorOpenid);
        visitor.setIsNewUser(isNewUser ? 1 : 0);
        visitor.setVisitAt(LocalDateTime.now());
        shareVisitorMapper.insert(visitor);

        // 更新分享记录的访问次数
        shareRecordMapper.incrementVisitCount(shareRecordId);

        // 如果是新用户，增加有效访问次数
        if (isNewUser) {
            shareRecordMapper.incrementValidVisitCount(shareRecordId);
            
            // 检查是否达到奖励条件
            ShareRecord record = shareRecordMapper.findById(shareRecordId);
            if (record != null && record.getValidVisitCount() >= REWARD_THRESHOLD && record.getRewardStatus() == 0) {
                // 标记为可领取
                shareRecordMapper.updateRewardStatus(shareRecordId, 1);
            }
        }
    }

    /**
     * 领取分享奖励
     */
    @Transactional
    public UserGift claimShareReward(Long userId, Long shareRecordId) {
        ShareRecord record = shareRecordMapper.findById(shareRecordId);
        if (record == null) {
            throw new IllegalArgumentException("分享记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        if (record.getRewardStatus() != 1) {
            throw new IllegalArgumentException("未达到领取条件");
        }

        // 标记已领取
        shareRecordMapper.updateRewardStatus(shareRecordId, 2);

        // 发放奖励（通过任务服务）
        return taskService.grantReward(userId, "AI_COUNT", 3, "SHARE", null, shareRecordId, null);
    }

    /**
     * 获取用户的分享记录
     */
    public List<ShareRecord> getUserShareRecords(Long userId) {
        return shareRecordMapper.findByUserId(userId);
    }

    /**
     * 获取分享记录的访客列表
     */
    public List<ShareVisitor> getShareVisitors(Long shareRecordId) {
        return shareVisitorMapper.findByShareRecordId(shareRecordId);
    }

    /**
     * 获取分享记录的奖励状态
     */
    public int getRewardStatus(Long shareRecordId) {
        ShareRecord record = shareRecordMapper.findById(shareRecordId);
        return record != null ? record.getRewardStatus() : 0;
    }
    
    /**
     * 根据ID获取分享记录
     */
    public ShareRecord getShareRecordById(Long shareRecordId) {
        return shareRecordMapper.findById(shareRecordId);
    }
}
