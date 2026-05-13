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
 * 浠诲姟鏈嶅姟銆?
 *
 * 閲嶆柊瀹氫綅涓轰换鍔′腑蹇冭仛鍚堝眰锛?
 * - 浠诲姟灞曠ず鐘舵€佺敱 TaskHandler 鎸変笟鍔¤兘鍔涜绠?
 * - 閫氱敤杩涘害鍨嬩换鍔′粛鍏煎鏃х殑 bp_task_config + user_task_progress 妯″瀷
 * - 绛惧埌绛夌嫭绔嬬帺娉曢€氳繃 handler 鑱氬悎杩涗换鍔′腑蹇冨睍绀?
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
     * 鑾峰彇鎵€鏈夊惎鐢ㄧ殑浠诲姟閰嶇疆銆?
     */
    public List<TaskConfig> getAllActiveTasks() {
        return taskConfigMapper.findAllActive();
    }

    /**
     * 鑾峰彇浠诲姟涓績鑱氬悎鍒楄〃銆?
     */
    public List<TaskCenterItem> getTaskCenterItems(Long userId) {
        return getAllActiveTasks().stream()
                .map(config -> resolveHandler(config).buildTaskItem(userId, config))
                .sorted((a, b) -> Integer.compare(defaultInt(a.getSortOrder()), defaultInt(b.getSortOrder())))
                .toList();
    }

    /**
     * 鑾峰彇鐢ㄦ埛浠诲姟杩涘害銆?
     * 浠呰繑鍥炰粛鐒朵娇鐢ㄦ棫 progress 妯″瀷鐨勪换鍔¤繘搴︼紝渚涘吋瀹规帴鍙ｄ娇鐢ㄣ€?
     */
    public List<UserTaskProgress> getUserTaskProgress(Long userId) {
        return userTaskProgressMapper.findByUserId(userId);
    }

    /**
     * 鑾峰彇鐢ㄦ埛浠诲姟杩涘害Map锛坱askCode -> progress锛夈€?
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
     * 澧炲姞浠诲姟杩涘害銆?
     * 褰撳墠鍙厑璁搁€氱敤杩涘害鍨嬩换鍔¤蛋璇ュ叆鍙ｏ紝閬垮厤鎶婄鍒扮瓑鐙珛鐜╂硶鍐嶆濉炲洖缁熶竴閫昏緫銆?
     */
    @Transactional
    public UserTaskProgress incrementTaskProgress(Long userId, String taskCode) {
        TaskConfig config = taskConfigMapper.findByCode(taskCode);
        if (config == null) {
            throw new IllegalArgumentException("浠诲姟涓嶅瓨鍦? " + taskCode);
        }
        if (!isGenericProgressTask(config)) {
            throw new IllegalArgumentException("Task does not support generic progress completion");
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
     * 棰嗗彇浠诲姟濂栧姳銆?
     * 鐩墠浠嶅吋瀹归€氱敤杩涘害鍨嬩换鍔＄殑棰嗗彇閫昏緫銆?
     */
    @Transactional
    public UserGift claimTaskReward(Long userId, Long progressId) {
        UserTaskProgress progress = userTaskProgressMapper.findById(progressId);
        if (progress == null) {
            throw new IllegalArgumentException("Task progress not found");
        }
        if (!progress.getUserId().equals(userId)) {
            throw new IllegalArgumentException("鏃犳潈鎿嶄綔");
        }
        if (progress.getStatus() != 1) {
            throw new IllegalArgumentException("Task is not completed or already claimed");
        }

        TaskConfig config = taskConfigMapper.findByCode(progress.getTaskCode());
        if (config == null) {
            throw new IllegalArgumentException("Task config not found");
        }
        if (!isGenericProgressTask(config)) {
            throw new IllegalArgumentException("璇ヤ换鍔″鍔遍渶閫氳繃瀵瑰簲涓氬姟鎺ュ彛棰嗗彇");
        }

        userTaskProgressMapper.claim(progressId);
        return taskRewardService.grantReward(userId, config.getRewardType(), config.getRewardValue(), "TASK", progress.getTaskId(), null, null);
    }

    /**
     * 棰嗗彇璧勬牸鍨嬬ぜ鍖呫€?
     */
    @Transactional
    public UserGift claimBenefitGift(Long userId, String taskCode) {
        TaskConfig config = taskConfigMapper.findByCode(taskCode);
        if (config == null) {
            throw new IllegalArgumentException("Task config not found");
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
        throw new IllegalArgumentException("Task benefit is not claimable");
    }

    private UserGift claimFirstRechargeGift(Long userId, TaskConfig config) {
        boolean hasPaidOrder = orderMapper.listByUserId(userId).stream()
                .anyMatch(order -> "PAID".equalsIgnoreCase(order.getStatus()));
        if (!hasPaidOrder) {
            throw new IllegalStateException("Complete first recharge before claiming");
        }
        return claimPackageGift(userId, config, "FIRST_RECHARGE_GIFT", "First recharge gift already claimed");
    }

    private UserGift claimRegisterGift(Long userId, TaskConfig config) {
        if (userMapper.findById(userId) == null) {
            throw new IllegalStateException("Register before claiming");
        }
        return claimPackageGift(userId, config, "REGISTER_GIFT", "Register gift already claimed");
    }

    private UserGift claimInviteRegisterGift(Long userId, TaskConfig config) {
        int targetCount = readExtraInt(config, "targetCount", 1);
        int currentCount = countInviteRegister(userId);
        int availableRounds = currentCount / targetCount;
        int claimedRounds = countClaimedRounds(userId, "INVITE_REGISTER_GIFT", config);
        if (availableRounds <= claimedRounds) {
            throw new IllegalStateException("邀请注册人数未达标");
        }
        return claimPackageGift(userId, config, "INVITE_REGISTER_GIFT", null);
    }

    private UserGift claimInviteRechargeGift(Long userId, TaskConfig config) {
        int targetCount = readExtraInt(config, "targetCount", 1);
        int currentCount = countInviteRecharge(userId);
        int availableRounds = currentCount / targetCount;
        int claimedRounds = countClaimedRounds(userId, "INVITE_RECHARGE_GIFT", config);
        if (availableRounds <= claimedRounds) {
            throw new IllegalStateException("邀请首充人数未达标");
        }
        return claimPackageGift(userId, config, "INVITE_RECHARGE_GIFT", null);
    }

    private UserGift claimPackageGift(Long userId, TaskConfig config, String sourcePrefix, String duplicateMessage) {
        String packageCode = readExtraText(config, "giftPackageCode", "");
        if (!StringUtils.hasText(packageCode)) {
            throw new IllegalArgumentException("未配置礼包 giftPackageCode");
        }
        String sourcePrefixWithCode = sourcePrefix + ":" + packageCode + ":";
        int claimedRounds = userGiftMapper.countByUserIdAndSourcePrefix(userId, sourcePrefixWithCode);
        if (StringUtils.hasText(duplicateMessage) && claimedRounds > 0) {
            throw new IllegalStateException(duplicateMessage);
        }
        String source = sourcePrefixWithCode + (claimedRounds + 1);
        return giftPackageService.grantPackageToUser(userId, packageCode, source);
    }

    private int countClaimedRounds(Long userId, String sourcePrefix, TaskConfig config) {
        String packageCode = readExtraText(config, "giftPackageCode", "");
        if (!StringUtils.hasText(packageCode)) {
            return 0;
        }
        return userGiftMapper.countByUserIdAndSourcePrefix(userId, sourcePrefix + ":" + packageCode + ":");
    }

    private int countInviteRegister(Long userId) {
        return inviteCodeService.countInvitedRegistered(userId);
    }

    private int countInviteRecharge(Long userId) {
        return inviteCodeService.countInvitedPaid(userId);
    }

    private TaskHandler resolveHandler(TaskConfig config) {
        return taskHandlers.stream()
                .filter(handler -> handler.supports(config))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("鏈壘鍒颁换鍔″鐞嗗櫒: " + config.getTaskCode()));
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
     * 鑾峰彇鍛ㄦ湡寮€濮嬫棩鏈熴€?
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
