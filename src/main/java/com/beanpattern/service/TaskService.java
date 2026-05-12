package com.beanpattern.service;

import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserGift;
import com.beanpattern.entity.UserTaskProgress;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.TaskConfigMapper;
import com.beanpattern.mapper.UserGiftMapper;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.mapper.UserTaskProgressMapper;
import com.beanpattern.service.task.TaskHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务服务。
 *
 * 重新定位为任务中心聚合层：
 * - 任务展示状态由 TaskHandler 按业务能力计算
 * - 通用进度型任务仍兼容旧的 bp_task_config + user_task_progress 模型
 * - 签到等独立玩法通过 handler 聚合进任务中心展示
 */
@Service
public class TaskService {

    private final TaskConfigMapper taskConfigMapper;
    private final UserTaskProgressMapper userTaskProgressMapper;
    private final UserGiftMapper userGiftMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final GiftPackageService giftPackageService;
    private final InviteCodeService inviteCodeService;
    private final TaskRewardService taskRewardService;
    private final List<TaskHandler> taskHandlers;
    private final ObjectMapper objectMapper;

    public TaskService(TaskConfigMapper taskConfigMapper,
                       UserTaskProgressMapper userTaskProgressMapper,
                       UserGiftMapper userGiftMapper,
                       UserMapper userMapper,
                       OrderMapper orderMapper,
                       GiftPackageService giftPackageService,
                       InviteCodeService inviteCodeService,
                       TaskRewardService taskRewardService,
                       List<TaskHandler> taskHandlers) {
        this.taskConfigMapper = taskConfigMapper;
        this.userTaskProgressMapper = userTaskProgressMapper;
        this.userGiftMapper = userGiftMapper;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.giftPackageService = giftPackageService;
        this.inviteCodeService = inviteCodeService;
        this.taskRewardService = taskRewardService;
        this.taskHandlers = taskHandlers;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取所有启用的任务配置。
     */
    public List<TaskConfig> getAllActiveTasks() {
        return taskConfigMapper.findAllActive();
    }

    /**
     * 获取任务中心聚合列表。
     */
    public List<TaskCenterItem> getTaskCenterItems(Long userId) {
        return getAllActiveTasks().stream()
                .map(config -> resolveHandler(config).buildTaskItem(userId, config))
                .sorted((a, b) -> Integer.compare(defaultInt(a.getSortOrder()), defaultInt(b.getSortOrder())))
                .toList();
    }

    /**
     * 获取用户任务进度。
     * 仅返回仍然使用旧 progress 模型的任务进度，供兼容接口使用。
     */
    public List<UserTaskProgress> getUserTaskProgress(Long userId) {
        return userTaskProgressMapper.findByUserId(userId);
    }

    /**
     * 获取用户任务进度Map（taskCode -> progress）。
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
     * 增加任务进度。
     * 当前只允许通用进度型任务走该入口，避免把签到等独立玩法再次塞回统一逻辑。
     */
    @Transactional
    public UserTaskProgress incrementTaskProgress(Long userId, String taskCode) {
        TaskConfig config = taskConfigMapper.findByCode(taskCode);
        if (config == null) {
            throw new IllegalArgumentException("任务不存在: " + taskCode);
        }
        if (!isGenericProgressTask(config)) {
            throw new IllegalArgumentException("该任务不支持通过统一进度接口完成，请走对应业务接口");
        }

        int targetCount = readExtraInt(config, "targetCount", 1);
        LocalDate periodStart = getPeriodStart(config.getTaskType());
        UserTaskProgress progress = userTaskProgressMapper.findByUserAndTaskAndPeriod(userId, config.getId(), periodStart);

        if (progress == null) {
            progress = new UserTaskProgress();
            progress.setUserId(userId);
            progress.setTaskId(config.getId());
            progress.setTaskCode(taskCode);
            progress.setCurrentCount(1);
            progress.setTargetCount(targetCount);
            progress.setPeriodStart(periodStart);

            if (1 >= targetCount) {
                progress.setStatus(1);
                progress.setCompletedAt(LocalDateTime.now());
            } else {
                progress.setStatus(0);
            }
            userTaskProgressMapper.insert(progress);
        } else {
            if (progress.getStatus() == 2) {
                return progress;
            }

            int newCount = defaultInt(progress.getCurrentCount()) + 1;
            int newStatus = progress.getStatus() != null ? progress.getStatus() : 0;
            LocalDateTime completedAt = progress.getCompletedAt();

            if (newCount >= targetCount) {
                newStatus = 1;
                completedAt = LocalDateTime.now();
            }

            userTaskProgressMapper.updateProgress(progress.getId(), newCount, newStatus, completedAt);
            progress.setCurrentCount(newCount);
            progress.setStatus(newStatus);
            progress.setCompletedAt(completedAt);
            progress.setTargetCount(targetCount);
        }

        return progress;
    }

    /**
     * 领取任务奖励。
     * 目前仍兼容通用进度型任务的领取逻辑。
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
        if (!isGenericProgressTask(config)) {
            throw new IllegalArgumentException("该任务奖励需通过对应业务接口领取");
        }

        userTaskProgressMapper.claim(progressId);
        return taskRewardService.grantReward(userId, config.getRewardType(), config.getRewardValue(), "TASK", progress.getTaskId(), null, null);
    }

    /**
     * 领取资格型礼包。
     */
    @Transactional
    public UserGift claimBenefitGift(Long userId, String taskCode) {
        TaskConfig config = taskConfigMapper.findByCode(taskCode);
        if (config == null) {
            throw new IllegalArgumentException("任务配置不存在");
        }
        String handlerType = readExtraText(config, "handlerType", "");
        if ("FIRST_RECHARGE_GIFT".equals(handlerType)
                || "first_recharge_gift".equals(config.getTaskCode())
                || "first_recharge".equals(config.getTaskCode())) {
            return claimFirstRechargeGift(userId, config);
        }
        if ("REGISTER_GIFT".equals(handlerType)
                || "register_gift".equals(config.getTaskCode())
                || "new_user_gift".equals(config.getTaskCode())) {
            return claimRegisterGift(userId, config);
        }
        if ("INVITE_REGISTER".equals(handlerType)
                || "invite_friend".equals(config.getTaskCode())
                || "invite_register".equals(config.getTaskCode())) {
            return claimInviteRegisterGift(userId, config);
        }
        if ("INVITE_RECHARGE".equals(handlerType)
                || "invite_recharge".equals(config.getTaskCode())) {
            return claimInviteRechargeGift(userId, config);
        }
        throw new IllegalArgumentException("该任务不是可领取的资格礼包");
    }

    private UserGift claimFirstRechargeGift(Long userId, TaskConfig config) {
        boolean hasPaidOrder = orderMapper.listByUserId(userId).stream()
                .anyMatch(order -> "PAID".equalsIgnoreCase(order.getStatus()));
        if (!hasPaidOrder) {
            throw new IllegalStateException("完成首次充值后才可领取");
        }
        return claimPackageGift(userId, config, "FIRST_RECHARGE_GIFT", "首冲礼包已领取");
    }

    private UserGift claimRegisterGift(Long userId, TaskConfig config) {
        if (userMapper.findById(userId) == null) {
            throw new IllegalStateException("注册后才可领取");
        }
        return claimPackageGift(userId, config, "REGISTER_GIFT", "注册礼包已领取");
    }

    private UserGift claimInviteRegisterGift(Long userId, TaskConfig config) {
        int targetCount = readExtraInt(config, "targetCount", 1);
        int currentCount = countInviteRegister(userId);
        if (currentCount < targetCount) {
            throw new IllegalStateException("邀请注册人数未达标");
        }
        return claimPackageGift(userId, config, "INVITE_REGISTER_GIFT", "邀请注册礼包已领取");
    }

    private UserGift claimInviteRechargeGift(Long userId, TaskConfig config) {
        int targetCount = readExtraInt(config, "targetCount", 1);
        int currentCount = countInviteRecharge(userId);
        if (currentCount < targetCount) {
            throw new IllegalStateException("邀请首充人数未达标");
        }
        return claimPackageGift(userId, config, "INVITE_RECHARGE_GIFT", "邀请充值礼包已领取");
    }

    private UserGift claimPackageGift(Long userId, TaskConfig config, String sourcePrefix, String duplicateMessage) {
        String packageCode = readExtraText(config, "giftPackageCode", "");
        if (!StringUtils.hasText(packageCode)) {
            throw new IllegalArgumentException("未配置礼包 giftPackageCode");
        }
        String source = sourcePrefix + ":" + packageCode;
        boolean claimed = userGiftMapper.findByUserId(userId).stream()
                .anyMatch(gift -> source.equals(gift.getSource()));
        if (claimed) {
            throw new IllegalStateException(duplicateMessage);
        }
        return giftPackageService.grantPackageToUser(userId, packageCode, source);
    }

    private int countInviteRegister(Long userId) {
        return inviteCodeService.countInvitedRegistered(userId);
    }

    private int countInviteRecharge(Long userId) {
        return inviteCodeService.countInvitedPaid(userId);
    }

    /**
     * 发放奖励。
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

        switch (rewardType) {
            case "VIP_DAYS" -> {
                gift.setGiftCode("VIP_DAYS_" + rewardValue);
                gift.setGiftName(rewardValue + "天VIP会员");
                gift.setGiftCategory("VIP_DAYS");
                userMapper.addVipDays(userId, rewardValue);
            }
            case "AI_COUNT", "AI_QUOTA" -> {
                gift.setGiftCode("AI_COUNT_" + rewardValue);
                gift.setGiftName(rewardValue + "次AI生成");
                gift.setGiftCategory("AI_COUNT");
                userMapper.addAiQuota(userId, rewardValue);
            }
            case "COUPON" -> {
                gift.setGiftCode("COUPON_" + rewardValue);
                gift.setGiftName(rewardValue + "元优惠券");
                gift.setGiftCategory("COUPON");
                gift.setExpireAt(LocalDateTime.now().plusDays(30));
            }
            default -> throw new IllegalArgumentException("未知奖励类型: " + rewardType);
        }

        userGiftMapper.insert(gift);
        return gift;
    }

    private TaskHandler resolveHandler(TaskConfig config) {
        return taskHandlers.stream()
                .filter(handler -> handler.supports(config))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到任务处理器: " + config.getTaskCode()));
    }

    private boolean isGenericProgressTask(TaskConfig config) {
        String handlerType = readExtraText(config, "handlerType", "GENERIC_PROGRESS");
        return "GENERIC_PROGRESS".equals(handlerType) || "EVENT_TASK".equals(handlerType);
    }

    private int readExtraInt(TaskConfig config, String field, int defaultValue) {
        JsonNode value = readExtraNode(config, field);
        return value == null || value.isNull() ? defaultValue : value.asInt(defaultValue);
    }

    private String readExtraText(TaskConfig config, String field, String defaultValue) {
        JsonNode value = readExtraNode(config, field);
        return value == null || value.isNull() ? defaultValue : value.asText(defaultValue);
    }

    private JsonNode readExtraNode(TaskConfig config, String field) {
        if (config == null || !StringUtils.hasText(config.getExtraConfig())) return null;
        try {
            JsonNode root = objectMapper.readTree(config.getExtraConfig());
            return root.get(field);
        } catch (Exception e) {
            return null;
        }
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 获取周期开始日期。
     */
    private LocalDate getPeriodStart(String taskType) {
        LocalDate now = LocalDate.now();
        if (taskType == null) return now;
        return switch (taskType) {
            case "DAILY" -> now;
            case "WEEKLY" -> now.minusDays(now.getDayOfWeek().getValue() - 1L);
            case "ONCE", "SHARE" -> null;
            default -> now;
        };
    }
}
