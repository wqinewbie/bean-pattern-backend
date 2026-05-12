package com.beanpattern.service.task;

import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.UserGiftMapper;
import com.beanpattern.service.InviteCodeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InviteRechargeTaskHandler implements TaskHandler {

    private static final String CLAIM_SOURCE_PREFIX = "INVITE_RECHARGE_GIFT:";

    private final InviteCodeService inviteCodeService;
    private final UserGiftMapper userGiftMapper;
    private final ObjectMapper objectMapper;

    public InviteRechargeTaskHandler(InviteCodeService inviteCodeService, UserGiftMapper userGiftMapper) {
        this.inviteCodeService = inviteCodeService;
        this.userGiftMapper = userGiftMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(TaskConfig config) {
        String handlerType = readExtraText(config, "handlerType", "");
        return "INVITE_RECHARGE".equals(handlerType)
                || "invite_recharge".equals(config.getTaskCode());
    }

    @Override
    public TaskCenterItem buildTaskItem(Long userId, TaskConfig config) {
        int targetCount = readExtraInt(config, "targetCount", 1);
        int currentCount = inviteCodeService.countInvitedPaid(userId);
        String packageCode = readExtraText(config, "giftPackageCode", "");
        boolean claimed = hasClaimed(userId, packageCode);

        int status;
        if (claimed) status = 2;
        else if (currentCount >= targetCount) status = 1;
        else status = 0;

        String progressText = claimed
                ? "已领取邀请充值礼包"
                : currentCount >= targetCount
                    ? "已达标，可领取礼包"
                    : "已邀请首充 " + currentCount + "/" + targetCount + " 人";

        return TaskCenterItem.builder()
                .taskId(config.getId())
                .taskCode(config.getTaskCode())
                .taskName(config.getTaskName())
                .taskType(config.getTaskType())
                .description(config.getDescription())
                .rewardType(config.getRewardType())
                .rewardValue(config.getRewardValue())
                .icon(config.getIcon())
                .sortOrder(config.getSortOrder())
                .handlerType("INVITE_RECHARGE")
                .bizCategory("BENEFIT")
                .progressText(progressText)
                .status(status)
                .currentCount(currentCount)
                .targetCount(targetCount)
                .progressId(null)
                .done(claimed)
                .canClaim(currentCount >= targetCount && !claimed)
                .build();
    }

    private boolean hasClaimed(Long userId, String packageCode) {
        if (!StringUtils.hasText(packageCode)) return false;
        return userGiftMapper.findByUserId(userId).stream()
                .map(UserGift::getSource)
                .filter(StringUtils::hasText)
                .anyMatch(source -> source.equals(CLAIM_SOURCE_PREFIX + packageCode));
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
}
