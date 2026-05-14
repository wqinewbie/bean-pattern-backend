package com.beanpattern.service.task;

import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.mapper.UserGiftMapper;
import com.beanpattern.service.InviteCodeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InviteRegisterTaskHandler implements TaskHandler {

    private static final String CLAIM_SOURCE_PREFIX = "INVITE_REGISTER_GIFT:";

    private final InviteCodeService inviteCodeService;
    private final UserGiftMapper userGiftMapper;
    private final ObjectMapper objectMapper;

    public InviteRegisterTaskHandler(InviteCodeService inviteCodeService, UserGiftMapper userGiftMapper) {
        this.inviteCodeService = inviteCodeService;
        this.userGiftMapper = userGiftMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(TaskConfig config) {
        String handlerType = readExtraText(config, "handlerType", "");
        return "INVITE_REGISTER".equals(handlerType)
                || "invite_friend".equals(config.getTaskCode())
                || "invite_register".equals(config.getTaskCode());
    }

    @Override
    public TaskCenterItem buildTaskItem(Long userId, TaskConfig config) {
        int targetCount = readExtraInt(config, "targetCount", 1);
        int currentCount = inviteCodeService.countInvitedRegistered(userId);
        String packageCode = readExtraText(config, "giftPackageCode", "");
        int claimedRounds = countClaimedRounds(userId, packageCode);
        int availableRounds = targetCount > 0 ? currentCount / targetCount : 0;
        int pendingRounds = Math.max(availableRounds - claimedRounds, 0);
        int nextTarget = (claimedRounds + 1) * targetCount;
        int nextNeed = Math.max(nextTarget - currentCount, 0);

        int status = pendingRounds > 0 ? 1 : 0;
        String progressText = pendingRounds > 0
                ? "已达标，可领取第" + (claimedRounds + 1) + "份礼包"
                : "已邀请注册 " + currentCount + " 人，还差 " + nextNeed + " 人可再领";

        return TaskCenterItem.builder()
                .taskId(config.getId())
                .taskCode(config.getTaskCode())
                .taskName(config.getTaskName())
                .taskType(config.getTaskType())
                .description(config.getDescription())
                .rewardType("GIFT_PACKAGE")
                .rewardValue(1)
                .icon(config.getIcon())
                .sortOrder(config.getSortOrder())
                .handlerType("INVITE_REGISTER")
                .bizCategory("BENEFIT")
                .progressText(progressText)
                .status(status)
                .currentCount(currentCount)
                .targetCount(targetCount)
                .progressId(null)
                .done(false)
                .canClaim(pendingRounds > 0)
                .build();
    }

    private int countClaimedRounds(Long userId, String packageCode) {
        if (!StringUtils.hasText(packageCode)) return 0;
        return userGiftMapper.countByUserIdAndSourcePrefix(userId, CLAIM_SOURCE_PREFIX + packageCode + ":");
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
