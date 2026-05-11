package com.beanpattern.service;

import com.beanpattern.entity.UserCheckin;
import com.beanpattern.entity.UserCheckinClaim;
import com.beanpattern.entity.UserCheckinStatus;
import com.beanpattern.mapper.UserCheckinClaimMapper;
import com.beanpattern.mapper.UserCheckinMapper;
import com.beanpattern.mapper.UserCheckinStatusMapper;
import com.beanpattern.mapper.UserMapper;
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
    private final UserMapper userMapper;

    // 签到配置（后续可以从数据库读取）
    private static final int CONTINUOUS_DAYS_REQUIRED = 3;  // 连续签到天数要求
    private static final String REWARD_TYPE = "AI_COUNT";   // 奖励类型
    private static final int REWARD_VALUE = 1;              // 奖励值

    public CheckinService(UserCheckinMapper checkinMapper,
                         UserCheckinStatusMapper statusMapper,
                         UserCheckinClaimMapper claimMapper,
                         UserMapper userMapper) {
        this.checkinMapper = checkinMapper;
        this.statusMapper = statusMapper;
        this.claimMapper = claimMapper;
        this.userMapper = userMapper;
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

        // 获取最近7天的签到记录
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
        result.put("requiredDays", CONTINUOUS_DAYS_REQUIRED);
        result.put("rewardType", REWARD_TYPE);
        result.put("rewardValue", REWARD_VALUE);

        return result;
    }

    /**
     * 执行签到
     */
    @Transactional
    public Map<String, Object> doCheckin(Long userId) {
        LocalDate today = LocalDate.now();

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
        boolean canClaim = (newContinuousDays >= CONTINUOUS_DAYS_REQUIRED);

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

        // 插入领取记录
        try {
            UserCheckinClaim claim = new UserCheckinClaim();
            claim.setUserId(userId);
            claim.setClaimDate(today);
            claim.setContinuousDays(status.getContinuousDays());
            claim.setRewardType(REWARD_TYPE);
            claim.setRewardValue(REWARD_VALUE);
            claimMapper.insert(claim);
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("今天已经领取过奖励了");
        }

        // 发放奖励
        if ("AI_COUNT".equals(REWARD_TYPE)) {
            userMapper.addAiQuota(userId, REWARD_VALUE);
        } else if ("VIP_DAYS".equals(REWARD_TYPE)) {
            userMapper.addVipDays(userId, REWARD_VALUE);
        }

        // 重置连续天数和领取状态
        statusMapper.resetContinuous(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("rewardType", REWARD_TYPE);
        result.put("rewardValue", REWARD_VALUE);
        result.put("message", "领取成功！获得" + REWARD_VALUE + "次AI生成");

        return result;
    }

    /**
     * 获取签到配置
     */
    public Map<String, Object> getCheckinConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("continuousDaysRequired", CONTINUOUS_DAYS_REQUIRED);
        config.put("rewardType", REWARD_TYPE);
        config.put("rewardValue", REWARD_VALUE);
        return config;
    }
}
