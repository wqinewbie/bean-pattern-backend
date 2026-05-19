package com.beanpattern.service;

import com.beanpattern.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户数据清除服务，删除用户及其所有关联数据。
 */
@Service
public class UserDataService {

    private final UserMapper userMapper;
    private final BpBoxMapper bpBoxMapper;
    private final BpDraftMapper bpDraftMapper;
    private final BpHistoryMapper bpHistoryMapper;
    private final OrderMapper orderMapper;
    private final BpUserGiftMapper bpUserGiftMapper;
    private final UserCheckinMapper userCheckinMapper;
    private final UserInviteRelationMapper userInviteRelationMapper;
    private final UserVipRecordMapper userVipRecordMapper;
    private final BannerClaimLogMapper bannerClaimLogMapper;
    private final AiGenerateTaskMapper aiGenerateTaskMapper;
    private final AiQuotaLogMapper aiQuotaLogMapper;
    private final UserActivityLogMapper userActivityLogMapper;
    private final UserTaskProgressMapper userTaskProgressMapper;
    private final CreatorPatternMapper creatorPatternMapper;
    private final FeedbackMapper feedbackMapper;
    private final ReviewTaskSubmissionMapper reviewTaskSubmissionMapper;
    private final ShareRecordMapper shareRecordMapper;
    private final ShareVisitorMapper shareVisitorMapper;
    private final ShareTrackMapper shareTrackMapper;
    private final UserNotificationMapper userNotificationMapper;
    private final UserWatermarkConfigMapper userWatermarkConfigMapper;
    private final UserGiftMapper userGiftMapper;

    public UserDataService(UserMapper userMapper, BpBoxMapper bpBoxMapper, BpDraftMapper bpDraftMapper,
                           BpHistoryMapper bpHistoryMapper, OrderMapper orderMapper,
                           BpUserGiftMapper bpUserGiftMapper,
                           UserCheckinMapper userCheckinMapper,
                           UserInviteRelationMapper userInviteRelationMapper,
                           UserVipRecordMapper userVipRecordMapper,
                           BannerClaimLogMapper bannerClaimLogMapper,
                           AiGenerateTaskMapper aiGenerateTaskMapper, AiQuotaLogMapper aiQuotaLogMapper,
                           UserActivityLogMapper userActivityLogMapper,
                           UserTaskProgressMapper userTaskProgressMapper,
                           CreatorPatternMapper creatorPatternMapper, FeedbackMapper feedbackMapper,
                           ReviewTaskSubmissionMapper reviewTaskSubmissionMapper,
                           ShareRecordMapper shareRecordMapper, ShareVisitorMapper shareVisitorMapper,
                           ShareTrackMapper shareTrackMapper,
                           UserNotificationMapper userNotificationMapper,
                           UserWatermarkConfigMapper userWatermarkConfigMapper,
                           UserGiftMapper userGiftMapper) {
        this.userMapper = userMapper;
        this.bpBoxMapper = bpBoxMapper;
        this.bpDraftMapper = bpDraftMapper;
        this.bpHistoryMapper = bpHistoryMapper;
        this.orderMapper = orderMapper;
        this.bpUserGiftMapper = bpUserGiftMapper;
        this.userCheckinMapper = userCheckinMapper;
        this.userInviteRelationMapper = userInviteRelationMapper;
        this.userVipRecordMapper = userVipRecordMapper;
        this.bannerClaimLogMapper = bannerClaimLogMapper;
        this.aiGenerateTaskMapper = aiGenerateTaskMapper;
        this.aiQuotaLogMapper = aiQuotaLogMapper;
        this.userActivityLogMapper = userActivityLogMapper;
        this.userTaskProgressMapper = userTaskProgressMapper;
        this.creatorPatternMapper = creatorPatternMapper;
        this.feedbackMapper = feedbackMapper;
        this.reviewTaskSubmissionMapper = reviewTaskSubmissionMapper;
        this.shareRecordMapper = shareRecordMapper;
        this.shareVisitorMapper = shareVisitorMapper;
        this.shareTrackMapper = shareTrackMapper;
        this.userNotificationMapper = userNotificationMapper;
        this.userWatermarkConfigMapper = userWatermarkConfigMapper;
        this.userGiftMapper = userGiftMapper;
    }

    @Transactional
    public void clearUserData(Long userId) {
        bpBoxMapper.deleteByUserId(userId);
        bpDraftMapper.deleteByUserId(userId);
        bpHistoryMapper.deleteByUserId(userId);
        creatorPatternMapper.deleteByUserId(userId);
        userCheckinMapper.deleteByUserId(userId);
        userActivityLogMapper.deleteByUserId(userId);
        userTaskProgressMapper.deleteByUserId(userId);
        bpUserGiftMapper.deleteByUserId(userId);
        userGiftMapper.deleteByUserId(userId);
        aiGenerateTaskMapper.deleteByUserId(userId);
        aiQuotaLogMapper.deleteByUserId(userId);
        orderMapper.deleteByUserId(userId);
        bannerClaimLogMapper.deleteByUserId(userId);
        feedbackMapper.deleteByUserId(userId);
        reviewTaskSubmissionMapper.deleteByUserId(userId);
        shareRecordMapper.deleteByUserId(userId);
        shareVisitorMapper.deleteByShareUserId(userId);
        shareTrackMapper.deleteByUserId(userId);
        userNotificationMapper.deleteByUserId(userId);
        userWatermarkConfigMapper.deleteByUserId(userId);
        userInviteRelationMapper.deleteByUserId(userId);
        userVipRecordMapper.deleteByUserId(userId);
        userMapper.deleteById(userId);
    }
}
