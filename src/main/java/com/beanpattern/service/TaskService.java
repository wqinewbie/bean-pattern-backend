package com.beanpattern.service;

import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserGift;
import com.beanpattern.entity.UserTaskProgress;
import com.beanpattern.mapper.GiftItemMapper;
import com.beanpattern.mapper.TaskConfigMapper;
import com.beanpattern.mapper.UserGiftMapper;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.mapper.UserTaskProgressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务服务
 */
@Service
public class TaskService {

    private final TaskConfigMapper taskConfigMapper;
    private final UserTaskProgressMapper userTaskProgressMapper;
    private final UserGiftMapper userGiftMapper;
    private final GiftItemMapper giftItemMapper;
    private final UserMapper userMapper;

    public TaskService(TaskConfigMapper taskConfigMapper,
                       UserTaskProgressMapper userTaskProgressMapper,
                       UserGiftMapper userGiftMapper,
                       GiftItemMapper giftItemMapper,
                       UserMapper userMapper) {
        this.taskConfigMapper = taskConfigMapper;
        this.userTaskProgressMapper = userTaskProgressMapper;
        this.userGiftMapper = userGiftMapper;
        this.giftItemMapper = giftItemMapper;
        this.userMapper = userMapper;
    }

    /**
     * 获取所有启用的任务配置
     */
    public List<TaskConfig> getAllActiveTasks() {
        return taskConfigMapper.findAllActive();
    }

    /**
     * 获取用户任务进度
     */
    public List<UserTaskProgress> getUserTaskProgress(Long userId) {
        return userTaskProgressMapper.findByUserId(userId);
    }

    /**
     * 获取用户任务进度Map（taskCode -> progress）
     */
    public Map<String, UserTaskProgress> getUserTaskProgressMap(Long userId) {
        List<UserTaskProgress> list = userTaskProgressMapper.findByUserId(userId);
        Map<String, UserTaskProgress> map = new HashMap<>();
        for (UserTaskProgress p : list) {
            map.put(p.getTaskCode(), p);
        }
        return map;
    }

    /**
     * 增加任务进度
     */
    @Transactional
    public UserTaskProgress incrementTaskProgress(Long userId, String taskCode) {
        TaskConfig config = taskConfigMapper.findByCode(taskCode);
        if (config == null) {
            throw new IllegalArgumentException("任务不存在: " + taskCode);
        }

        LocalDate periodStart = getPeriodStart(config.getTaskType());
        UserTaskProgress progress = userTaskProgressMapper.findByUserAndTaskAndPeriod(userId, config.getId(), periodStart);

        if (progress == null) {
            // 创建新进度
            progress = new UserTaskProgress();
            progress.setUserId(userId);
            progress.setTaskId(config.getId());
            progress.setTaskCode(taskCode);
            progress.setCurrentCount(1);
            progress.setTargetCount(config.getTargetCount());
            progress.setPeriodStart(periodStart);
            
            if (1 >= config.getTargetCount()) {
                progress.setStatus(1); // 已完成可领取
                progress.setCompletedAt(LocalDateTime.now());
            } else {
                progress.setStatus(0); // 进行中
            }
            userTaskProgressMapper.insert(progress);
        } else {
            if (progress.getStatus() == 2) {
                // 已领取，不处理
                return progress;
            }
            
            int newCount = progress.getCurrentCount() + 1;
            int newStatus = 0;
            LocalDateTime completedAt = null;
            
            if (newCount >= config.getTargetCount()) {
                newStatus = 1; // 已完成可领取
                completedAt = LocalDateTime.now();
            }
            
            userTaskProgressMapper.updateProgress(progress.getId(), newCount, newStatus, completedAt);
            progress.setCurrentCount(newCount);
            progress.setStatus(newStatus);
            progress.setCompletedAt(completedAt);
        }

        return progress;
    }

    /**
     * 领取任务奖励
     */
    @Transactional
    public UserGift claimTaskReward(Long userId, Long progressId) {
        UserTaskProgress progress = userTaskProgressMapper.findById(progressId);
        if (progress == null) {
            throw new IllegalArgumentException("任务进度不存在");
        }
        if (!progress.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        if (progress.getStatus() != 1) {
            throw new IllegalArgumentException("任务未完成或已领取");
        }

        TaskConfig config = taskConfigMapper.findByCode(progress.getTaskCode());
        if (config == null) {
            throw new IllegalArgumentException("任务配置不存在");
        }

        // 标记已领取
        userTaskProgressMapper.claim(progressId);

        // 发放奖励
        return grantReward(userId, config.getRewardType(), config.getRewardValue(), "TASK", progress.getTaskId(), null, null);
    }

    /**
     * 发放奖励
     */
    @Transactional
    public UserGift grantReward(Long userId, String rewardType, Integer rewardValue, 
                                String source, Long taskId, Long shareRecordId, Long orderId) {
        UserGift gift = new UserGift();
        gift.setUserId(userId);
        gift.setTaskId(taskId);
        gift.setShareRecordId(shareRecordId);
        gift.setOrderId(orderId);
        gift.setSource(source);
        gift.setValue(rewardValue);
        
        // 根据奖励类型设置
        switch (rewardType) {
            case "VIP_DAYS":
                gift.setGiftCode("VIP_DAYS_" + rewardValue);
                gift.setGiftName(rewardValue + "天VIP会员");
                gift.setGiftCategory("VIP_DAYS");
                // 直接发放VIP
                userMapper.addVipDays(userId, rewardValue);
                break;
            case "AI_COUNT":
                gift.setGiftCode("AI_COUNT_" + rewardValue);
                gift.setGiftName(rewardValue + "次AI生成");
                gift.setGiftCategory("AI_COUNT");
                userMapper.addAiQuota(userId, rewardValue);
                break;
            case "COUPON":
                gift.setGiftCode("COUPON_" + rewardValue);
                gift.setGiftName(rewardValue + "元优惠券");
                gift.setGiftCategory("COUPON");
                gift.setExpireAt(LocalDateTime.now().plusDays(30));
                break;
            default:
                throw new IllegalArgumentException("未知奖励类型: " + rewardType);
        }

        userGiftMapper.insert(gift);
        return gift;
    }

    /**
     * 获取周期开始日期
     */
    private LocalDate getPeriodStart(String taskType) {
        LocalDate now = LocalDate.now();
        switch (taskType) {
            case "DAILY":
                return now;
            case "WEEKLY":
                return now.minusDays(now.getDayOfWeek().getValue() - 1);
            case "ONCE":
            case "SHARE":
                return null;
            default:
                return now;
        }
    }
}
