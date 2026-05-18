package com.beanpattern.service.task;

import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.mapper.BpUserGiftMapper;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.service.GiftPackageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 注册礼包属于资格福利：用户注册后即可领取一次。
 */
@Component
public class RegisterGiftTaskHandler implements TaskHandler {

    private static final String CLAIM_SOURCE_PREFIX = "REGISTER_GIFT:";

    private final UserMapper userMapper;
    private final BpUserGiftMapper bpUserGiftMapper;
    private final GiftPackageService giftPackageService;
    private final ObjectMapper objectMapper;

    public RegisterGiftTaskHandler(UserMapper userMapper, BpUserGiftMapper bpUserGiftMapper, GiftPackageService giftPackageService) {
        this.userMapper = userMapper;
        this.bpUserGiftMapper = bpUserGiftMapper;
        this.giftPackageService = giftPackageService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(TaskConfig config) {
        String handlerType = readExtraText(config, "handlerType", "");
        return "REGISTER_GIFT".equals(handlerType)
                || "register_gift".equals(config.getTaskCode())
                || "new_user_gift".equals(config.getTaskCode());
    }

    @Override
    public TaskCenterItem buildTaskItem(Long userId, TaskConfig config) {
        boolean registered = userMapper.findById(userId) != null;
        String packageCode = readExtraText(config, "giftPackageCode", "");
        String sourcePrefix = CLAIM_SOURCE_PREFIX + packageCode + ":";
        boolean claimed = StringUtils.hasText(packageCode)
                && bpUserGiftMapper.countByUserIdAndSourcePrefix(userId, sourcePrefix) > 0;

        int status;
        if (claimed) status = 2;
        else if (registered) status = 1;
        else status = 0;

        GiftPackage giftPackage = StringUtils.hasText(packageCode)
                ? giftPackageService.getByCode(packageCode)
                : null;
        GiftPackageRewardHelper.RewardInfo rewardInfo = GiftPackageRewardHelper.parseGiftPackageReward(giftPackage);

        return TaskCenterItem.builder()
                .taskId(config.getId())
                .taskCode(config.getTaskCode())
                .taskName(config.getTaskName())
                .taskType(config.getTaskType())
                .description(config.getDescription())
                .rewardType(rewardInfo.getDisplayType())
                .rewardValue(rewardInfo.getDisplayValue())
                .rewardItems(rewardInfo.getItems())
                .icon(config.getIcon())
                .sortOrder(config.getSortOrder())
                .handlerType("REGISTER_GIFT")
                .bizCategory("BENEFIT")
                .progressText(claimed ? "已领取注册礼包" : registered ? "注册成功，可领取礼包" : "注册后可领取")
                .status(status)
                .currentCount(registered ? 1 : 0)
                .targetCount(1)
                .progressId(null)
                .done(claimed)
                .canClaim(registered && !claimed)
                .build();
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
}
