package com.beanpattern.service;

import com.beanpattern.entity.CheckinConfig;
import com.beanpattern.entity.UserCheckin;
import com.beanpattern.entity.UserCheckinClaim;
import com.beanpattern.entity.UserCheckinStatus;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.CheckinConfigMapper;
import com.beanpattern.mapper.UserCheckinClaimMapper;
import com.beanpattern.mapper.UserCheckinMapper;
import com.beanpattern.mapper.UserCheckinStatusMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签到服务
 */
@Service
public class CheckinService {

    private final UserCheckinMapper checkinMapper;
    private final UserCheckinStatusMapper statusMapper;
    private final UserCheckinClaimMapper claimMapper;
    private final CheckinConfigMapper checkinConfigMapper;
    private final GiftPackageService giftPackageService;

    private static final int DEFAULT_CONTINUOUS_DAYS_REQUIRED = 3;
    private static final boolean DEFAULT_IS_ACTIVE = true;

    public CheckinService(UserCheckinMapper checkinMapper,
                         UserCheckinStatusMapper statusMapper,
                         UserCheckinClaimMapper claimMapper,
                         CheckinConfigMapper checkinConfigMapper,
                         GiftPackageService giftPackageService) {
        this.checkinMapper = checkinMapper;
        this.statusMapper = statusMapper;
        this.claimMapper = claimMapper;
        this.checkinConfigMapper = checkinConfigMapper;
        this.giftPackageService = giftPackageService;
    }

    /**
     * 获取有效签到配置
     */
    public CheckinConfig getActiveConfig() {
        CheckinConfig config = checkinConfigMapper.findLatest();
        if (config == null) {
            return CheckinConfig.builder()
                    .continuousDaysRequired(DEFAULT_CONTINUOUS_DAYS_REQUIRED)
                    .giftPackageCode("")
                    .isActive(DEFAULT_IS_ACTIVE)
                    .build();
        }
        if (config.getContinuousDaysRequired() == null || config.getContinuousDaysRequired() < 1) {
            config.setContinuousDaysRequired(DEFAULT_CONTINUOUS_DAYS_REQUIRED);
        }
        if (config.getGiftPackageCode() == null) {
            config.setGiftPackageCode("");
        }
        if (config.getIsActive() == null) {
            config.setIsActive(DEFAULT_IS_ACTIVE);
        }
        return config;
    }

    /**
     * 保存签到配置
     */
    @Transactional
    public CheckinConfig saveConfig(CheckinConfig config) {
        CheckinConfig target = CheckinConfig.builder()
                .id(config.getId())
                .continuousDaysRequired(config.getContinuousDaysRequired() == null || config.getContinuousDaysRequired() < 1
                        ? DEFAULT_CONTINUOUS_DAYS_REQUIRED : config.getContinuousDaysRequired())
                .giftPackageCode(config.getGiftPackageCode() == null ? "" : config.getGiftPackageCode().trim())
                .isActive(config.getIsActive() == null ? DEFAULT_IS_ACTIVE : config.getIsActive())
                .build();
        if (target.getGiftPackageCode().isBlank()) {
            throw new IllegalArgumentException("签到奖励必须绑定礼品包");
        }

        CheckinConfig existing = checkinConfigMapper.findLatest();
        if (existing == null) {
            checkinConfigMapper.insert(target);
            return target;
        }

        target.setId(existing.getId());
        checkinConfigMapper.update(target);
        return getActiveConfig();
    }

    /**
     * 获取用户签到状态
     */
    public Map<String, Object> getCheckinStatus(Long userId) {
        UserCheckinStatus status = statusMapper.findByUserId(userId);

        // 如果没有状态记录，初始化一个
        if (status == null) {
            status = new UserCheckinStatus();
            status.setUserId(userId);
            status.setContinuousDays(0);
            status.setTotalDays(0);
            status.setLastCheckinDate(null);
            status.setCanClaim(false);
        }

        CheckinConfig config = getActiveConfig();
        LocalDate startDate = LocalDate.now().minusDays(6);
        List<UserCheckin> recentCheckins = checkinMapper.findRecentByUser(userId, startDate);

        // 构建签到日历（最近7天）
        Map<String, Boolean> calendar = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(6 - i);
            calendar.put(date.toString(), false);
        }
        for (UserCheckin checkin : recentCheckins) {
            calendar.put(checkin.getCheckinDate().toString(), true);
        }

        // 检查今天是否已签到
        LocalDate today = LocalDate.now();
        boolean checkedInToday = checkinMapper.findByUserAndDate(userId, today) != null;

        Map<String, Object> result = new HashMap<>();
        result.put("continuousDays", status.getContinuousDays());
        result.put("totalDays", status.getTotalDays());
        result.put("lastCheckinDate", status.getLastCheckinDate());
        result.put("canClaim", status.getCanClaim());
        result.put("checkedInToday", checkedInToday);
        result.put("calendar", calendar);
        result.put("requiredDays", config.getContinuousDaysRequired());
        result.put("giftPackageCode", config.getGiftPackageCode());
        result.put("rewardType", "GIFT_PACKAGE");
        result.put("rewardValue", 1);
        result.put("isActive", config.getIsActive());

        return result;
    }

    /**
     * 执行签到
     */
    @Transactional
    public Map<String, Object> doCheckin(Long userId) {
        LocalDate today = LocalDate.now();
        CheckinConfig config = getActiveConfig();
        if (!Boolean.TRUE.equals(config.getIsActive())) {
            throw new IllegalStateException("签到暂未开启");
        }

        // 检查今天是否已签到（利用唯一索引防止重复）
        UserCheckin existingCheckin = checkinMapper.findByUserAndDate(userId, today);
        if (existingCheckin != null) {
            throw new IllegalStateException("今天已经签到过了");
        }

        // 获取或创建签到状态
        UserCheckinStatus status = statusMapper.findByUserId(userId);
        if (status == null) {
            status = new UserCheckinStatus();
            status.setUserId(userId);
            status.setContinuousDays(0);
            status.setTotalDays(0);
            status.setLastCheckinDate(null);
            status.setCanClaim(false);
            statusMapper.insert(status);
        }

        // 计算连续天数
        int newContinuousDays;
        LocalDate lastCheckinDate = status.getLastCheckinDate();

        if (lastCheckinDate == null) {
            // 第一次签到
            newContinuousDays = 1;
        } else if (lastCheckinDate.equals(today.minusDays(1))) {
            // 连续签到
            newContinuousDays = status.getContinuousDays() + 1;
        } else if (lastCheckinDate.equals(today)) {
            // 今天已签到（理论上不会到这里，因为前面已经检查过）
            throw new IllegalStateException("今天已经签到过了");
        } else {
            // 中断了，重新开始
            newContinuousDays = 1;
        }

        // 判断是否可以领取奖励
        boolean canClaim = (newContinuousDays >= config.getContinuousDaysRequired());

        // 插入签到记录
        try {
            UserCheckin checkin = new UserCheckin();
            checkin.setUserId(userId);
            checkin.setCheckinDate(today);
            checkin.setContinuousDays(newContinuousDays);
            checkinMapper.insert(checkin);
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("今天已经签到过了");
        }

        // 更新签到状态
        status.setContinuousDays(newContinuousDays);
        status.setTotalDays(status.getTotalDays() + 1);
        status.setLastCheckinDate(today);
        status.setCanClaim(canClaim);
        statusMapper.update(status);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("continuousDays", newContinuousDays);
        result.put("totalDays", status.getTotalDays());
        result.put("canClaim", canClaim);
        result.put("message", "签到成功！连续签到" + newContinuousDays + "天");

        return result;
    }

    /**
     * 领取签到奖励
     */
    @Transactional
    public Map<String, Object> claimReward(Long userId) {
        LocalDate today = LocalDate.now();
        CheckinConfig config = getActiveConfig();

        // 检查签到状态
        UserCheckinStatus status = statusMapper.findByUserId(userId);
        if (status == null || !status.getCanClaim()) {
            throw new IllegalStateException("暂无可领取的奖励");
        }

        // 检查今天是否已领取（利用唯一索引防止重复）
        UserCheckinClaim existingClaim = claimMapper.findByUserAndDate(userId, today);
        if (existingClaim != null) {
            throw new IllegalStateException("今天已经领取过奖励了");
        }

        if (config.getGiftPackageCode() == null || config.getGiftPackageCode().isBlank()) {
            throw new IllegalStateException("签到奖励未配置礼品包");
        }

        UserGift packageGift;
        try {
            UserCheckinClaim claim = new UserCheckinClaim();
            claim.setUserId(userId);
            claim.setClaimDate(today);
            claim.setContinuousDays(status.getContinuousDays());
            claim.setRewardType("GIFT_PACKAGE");
            claim.setRewardValue(1);
            claimMapper.insert(claim);
            packageGift = giftPackageService.grantPackageToUser(userId, config.getGiftPackageCode(), "CHECKIN:" + today);
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("今天已经领取过奖励了");
        }

        // 重置连续天数和领取状态
        statusMapper.resetContinuous(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("rewardType", "GIFT_PACKAGE");
        result.put("rewardValue", 1);
        result.put("giftId", packageGift.getId());
        result.put("giftName", packageGift.getGiftName());
        result.put("message", "领取成功，礼品包已放入我的礼品包");

        return result;
    }

    /**
     * 获取后台签到统计
     */
    public Map<String, Object> getAdminStatistics() {
        LocalDate today = LocalDate.now();
        Map<String, Object> result = new HashMap<>();
        result.put("todayCheckinCount", checkinMapper.countByDate(today));
        result.put("totalCheckinUsers", checkinMapper.countDistinctUsers());
        result.put("todayClaimCount", claimMapper.countByDate(today));
        result.put("totalRewardValue", claimMapper.sumRewardValue());
        return result;
    }

    /**
     * 获取后台签到记录列表
     */
    public Map<String, Object> getAdminRecentCheckins(int page, int pageSize, LocalDate startDate, LocalDate endDate, String keyword) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePage - 1) * safePageSize;
        String normalizedKeyword = keyword == null ? null : keyword.trim();

        List<Map<String, Object>> list = checkinMapper.findAdminRecent(startDate, endDate, normalizedKeyword, safePageSize, offset);
        int total = checkinMapper.countAdminRecent(startDate, endDate, normalizedKeyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", safePage);
        result.put("pageSize", safePageSize);
        return result;
    }

    /**
     * 获取签到配置
     */
    public Map<String, Object> getCheckinConfig() {
        CheckinConfig config = getActiveConfig();
        Map<String, Object> result = new HashMap<>();
        result.put("id", config.getId());
        result.put("continuousDaysRequired", config.getContinuousDaysRequired());
        result.put("giftPackageCode", config.getGiftPackageCode());
        result.put("rewardType", "GIFT_PACKAGE");
        result.put("rewardValue", 1);
        result.put("isActive", config.getIsActive());
        return result;
    }
}
