package com.beanpattern.service;

import com.beanpattern.entity.CheckinConfig;
import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.RewardItem;
import com.beanpattern.entity.UserCheckin;
import com.beanpattern.entity.UserCheckinClaim;
import com.beanpattern.entity.UserCheckinStatus;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.CheckinConfigMapper;
import com.beanpattern.mapper.UserCheckinClaimMapper;
import com.beanpattern.mapper.UserCheckinMapper;
import com.beanpattern.mapper.UserCheckinStatusMapper;
import com.beanpattern.service.task.GiftPackageRewardHelper;
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
        CheckinConfig config = getActiveConfig();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        List<UserCheckin> recentCheckins = checkinMapper.findRecentByUser(userId, startDate);

        // 动态计算 totalDays 和 continuousDays（不依赖 bp_user_checkin_status）
        int totalDays = checkinMapper.countByUserId(userId);
        UserCheckinClaim lastClaim = claimMapper.findLastByUser(userId);
        LocalDate lastClaimDate = lastClaim != null ? lastClaim.getClaimDate() : null;
        int continuousDays = calcContinuousDays(recentCheckins, today, lastClaimDate);
        boolean checkedInToday = recentCheckins.stream().anyMatch(c -> today.equals(c.getCheckinDate()));
        boolean claimedToday = lastClaim != null && today.equals(lastClaim.getClaimDate());
        boolean canClaim = !claimedToday && continuousDays >= config.getContinuousDaysRequired() && config.getIsActive();

        // 构建签到日历（最近7天）
        Map<String, Boolean> calendar = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            calendar.put(today.minusDays(6 - i).toString(), false);
        }
        for (UserCheckin checkin : recentCheckins) {
            calendar.put(checkin.getCheckinDate().toString(), true);
        }

        GiftPackage giftPackage = giftPackageService.getByCode(config.getGiftPackageCode());
        RewardItem rewardInfo;
        if (giftPackage != null) {
            rewardInfo = GiftPackageRewardHelper.parseRewardInfo(giftPackage);
        } else {
            rewardInfo = new RewardItem("NONE", 0, "未配置");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("continuousDays", continuousDays);
        result.put("totalDays", totalDays);
        result.put("lastCheckinDate", !recentCheckins.isEmpty() ? recentCheckins.get(0).getCheckinDate().toString() : null);
        result.put("canClaim", canClaim);
        result.put("checkedInToday", checkedInToday);
        result.put("calendar", calendar);
        result.put("requiredDays", config.getContinuousDaysRequired());
        result.put("giftPackageCode", config.getGiftPackageCode() != null ? config.getGiftPackageCode() : "");
        result.put("rewardType", rewardInfo.getType());
        result.put("rewardValue", rewardInfo.getValue());
        result.put("rewardItems", giftPackage != null ? GiftPackageRewardHelper.parseAllRewardItems(giftPackage) : java.util.Collections.emptyList());
        result.put("isActive", giftPackage != null && config.getIsActive());

        return result;
    }

    /**
     * 计算从指定日期往前推的连续签到天数，截止到 lastClaimDate（不含）或第一个断签日
     */
    private int calcContinuousDays(List<UserCheckin> recentCheckins, LocalDate today, LocalDate lastClaimDate) {
        if (recentCheckins.isEmpty()) return 0;
        int count = 0;
        LocalDate date = today;
        LocalDate stopAt = lastClaimDate != null ? lastClaimDate : LocalDate.MIN;
        while (date.isAfter(stopAt)) {
            final LocalDate d = date;
            if (recentCheckins.stream().noneMatch(c -> d.equals(c.getCheckinDate()))) break;
            count++;
            date = date.minusDays(1);
        }
        return count;
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

        // 从 bp_user_checkin 记录动态计算连续天数
        UserCheckin lastCheckin = checkinMapper.findLastByUser(userId);
        int newContinuousDays;
        if (lastCheckin == null) {
            newContinuousDays = 1;
        } else if (lastCheckin.getCheckinDate().equals(today.minusDays(1))) {
            newContinuousDays = lastCheckin.getContinuousDays() + 1;
        } else {
            newContinuousDays = 1;
        }

        boolean canClaim = (newContinuousDays >= config.getContinuousDaysRequired());

        try {
            UserCheckin checkin = new UserCheckin();
            checkin.setUserId(userId);
            checkin.setCheckinDate(today);
            checkin.setContinuousDays(newContinuousDays);
            checkinMapper.insert(checkin);
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("今天已经签到过了");
        }

        // 同步更新旧状态表（兼容过渡期，后续版本删除）
        try {
            UserCheckinStatus status = statusMapper.findByUserId(userId);
            if (status == null) {
                status = new UserCheckinStatus();
                status.setUserId(userId);
                try { statusMapper.insert(status); } catch (Exception ignored) {}
            }
            status.setContinuousDays(newContinuousDays);
            status.setTotalDays(checkinMapper.countByUserId(userId));
            status.setLastCheckinDate(today);
            status.setCanClaim(canClaim);
            statusMapper.update(status);
        } catch (Exception ignored) {}

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("continuousDays", newContinuousDays);
        result.put("totalDays", checkinMapper.countByUserId(userId));
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

        // 检查今天是否已领取
        UserCheckinClaim alreadyClaimed = claimMapper.findByUserAndDate(userId, today);
        if (alreadyClaimed != null) {
            throw new IllegalStateException("今天已领取过签到奖励");
        }

        // 查找上次领取日期，用于计算新一轮连续签到天数
        UserCheckinClaim lastClaim = claimMapper.findLastByUser(userId);
        LocalDate lastClaimDate = lastClaim != null ? lastClaim.getClaimDate() : null;

        // 动态计算是否可以领取（只统计上次领取之后的连续签到）
        List<UserCheckin> recentCheckins = checkinMapper.findRecentByUser(userId, today.minusDays(6));
        int continuousDays = calcContinuousDays(recentCheckins, today, lastClaimDate);
        if (continuousDays < config.getContinuousDaysRequired()) {
            throw new IllegalStateException("暂无可领取的奖励");
        }

        if (config.getGiftPackageCode() == null || config.getGiftPackageCode().isBlank()) {
            throw new IllegalStateException("签到奖励未配置礼品包");
        }

        GiftPackage giftPackage = giftPackageService.getByCode(config.getGiftPackageCode());
        if (giftPackage == null) {
            throw new IllegalStateException("签到未配置礼品包");
        }
        RewardItem rewardInfo = GiftPackageRewardHelper.parseRewardInfo(giftPackage);

        UserGift packageGift = giftPackageService.grantPackageToUser(userId, config.getGiftPackageCode(), "CHECKIN:" + today);

        // 记录领取记录（用于防止重复领取 & 重置连续签到计数起点）
        UserCheckinClaim claim = new UserCheckinClaim();
        claim.setUserId(userId);
        claim.setClaimDate(today);
        claim.setContinuousDays(continuousDays);
        claimMapper.insert(claim);

        // 同步更新旧表（兼容过渡期）
        try { statusMapper.resetContinuous(userId); } catch (Exception ignored) {}

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("rewardType", rewardInfo.getType());
        result.put("rewardValue", rewardInfo.getValue());
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

        // 从礼品包获取奖励信息
        GiftPackage giftPackage = giftPackageService.getByCode(config.getGiftPackageCode());
        if (giftPackage == null) {
            throw new IllegalStateException("签到未配置礼品包");
        }
        RewardItem rewardInfo = GiftPackageRewardHelper.parseRewardInfo(giftPackage);

        Map<String, Object> result = new HashMap<>();
        result.put("id", config.getId());
        result.put("continuousDaysRequired", config.getContinuousDaysRequired());
        result.put("giftPackageCode", config.getGiftPackageCode());
        result.put("rewardType", rewardInfo.getType());
        result.put("rewardValue", rewardInfo.getValue());
        result.put("rewardItems", GiftPackageRewardHelper.parseAllRewardItems(giftPackage));
        result.put("isActive", config.getIsActive());
        return result;
    }
}
