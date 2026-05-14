package com.beanpattern.service;

import com.beanpattern.entity.ShareRecord;
import com.beanpattern.entity.ShareVisitor;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.ShareRecordMapper;
import com.beanpattern.mapper.ShareVisitorMapper;
import com.beanpattern.mapper.TaskConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分享服务。
 * 分享奖励属于运营奖励，只发放后台任务配置绑定的礼品包。
 */
@Service
public class ShareService {

    private static final int REWARD_THRESHOLD = 3;

    private final ShareRecordMapper shareRecordMapper;
    private final ShareVisitorMapper shareVisitorMapper;
    private final TaskConfigMapper taskConfigMapper;
    private final TaskRewardService taskRewardService;

    public ShareService(ShareRecordMapper shareRecordMapper,
                        ShareVisitorMapper shareVisitorMapper,
                        TaskConfigMapper taskConfigMapper,
                        TaskRewardService taskRewardService) {
        this.shareRecordMapper = shareRecordMapper;
        this.shareVisitorMapper = shareVisitorMapper;
        this.taskConfigMapper = taskConfigMapper;
        this.taskRewardService = taskRewardService;
    }

    public ShareRecord createShareRecord(Long userId, String shareScene, String targetId, String shareTicket) {
        ShareRecord record = new ShareRecord();
        record.setUserId(userId);
        record.setShareScene(shareScene);
        record.setTargetId(targetId);
        record.setShareTicket(shareTicket);
        shareRecordMapper.insert(record);
        return record;
    }

    @Transactional
    public void recordVisitor(Long shareRecordId, Long shareUserId, String visitorOpenid, boolean isNewUser) {
        ShareVisitor existing = shareVisitorMapper.findByRecordAndVisitor(shareRecordId, visitorOpenid);
        if (existing != null) {
            return;
        }

        ShareVisitor visitor = new ShareVisitor();
        visitor.setShareRecordId(shareRecordId);
        visitor.setShareUserId(shareUserId);
        visitor.setVisitorOpenid(visitorOpenid);
        visitor.setIsNewUser(isNewUser ? 1 : 0);
        visitor.setVisitAt(LocalDateTime.now());
        shareVisitorMapper.insert(visitor);

        shareRecordMapper.incrementVisitCount(shareRecordId);

        if (isNewUser) {
            shareRecordMapper.incrementValidVisitCount(shareRecordId);
            ShareRecord record = shareRecordMapper.findById(shareRecordId);
            if (record != null && record.getValidVisitCount() >= REWARD_THRESHOLD && record.getRewardStatus() == 0) {
                shareRecordMapper.updateRewardStatus(shareRecordId, 1);
            }
        }
    }

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

        TaskConfig config = resolveShareRewardTask(record);
        UserGift gift = taskRewardService.grantTaskPackage(userId, config, "SHARE");
        shareRecordMapper.updateRewardStatus(shareRecordId, 2);
        return gift;
    }

    public List<ShareRecord> getUserShareRecords(Long userId) {
        return shareRecordMapper.findByUserId(userId);
    }

    public List<ShareVisitor> getShareVisitors(Long shareRecordId) {
        return shareVisitorMapper.findByShareRecordId(shareRecordId);
    }

    public int getRewardStatus(Long shareRecordId) {
        ShareRecord record = shareRecordMapper.findById(shareRecordId);
        return record != null ? record.getRewardStatus() : 0;
    }

    public ShareRecord getShareRecordById(Long shareRecordId) {
        return shareRecordMapper.findById(shareRecordId);
    }

    private TaskConfig resolveShareRewardTask(ShareRecord record) {
        TaskConfig config = null;
        if (record.getShareScene() != null && !record.getShareScene().isBlank()) {
            config = taskConfigMapper.findByCode(record.getShareScene());
        }
        if (config == null) {
            config = taskConfigMapper.findByCode("daily_share");
        }
        if (config == null) {
            config = taskConfigMapper.findByCode("share_friend");
        }
        if (config == null) {
            throw new IllegalStateException("分享奖励任务未配置");
        }
        return config;
    }
}
