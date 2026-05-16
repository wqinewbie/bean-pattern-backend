package com.beanpattern.service.task;

import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.service.CheckinService;
import com.beanpattern.service.GiftPackageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 签到是独立玩法系统，任务中心只聚合展示它的入口和状态。
 */
@Component
public class CheckinTaskHandler implements TaskHandler {

    private final CheckinService checkinService;
    private final GiftPackageService giftPackageService;
    private final ObjectMapper objectMapper;

    public CheckinTaskHandler(CheckinService checkinService, GiftPackageService giftPackageService) {
        this.checkinService = checkinService;
        this.giftPackageService = giftPackageService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(TaskConfig config) {
        String handlerType = readExtraText(config, "handlerType", "");
        return "CHECKIN".equals(handlerType) || "daily_checkin".equals(config.getTaskCode());
    }

    @Override
    public TaskCenterItem buildTaskItem(Long userId, TaskConfig config) {
        Map<String, Object> status = checkinService.getCheckinStatus(userId);
        boolean checkedInToday = Boolean.TRUE.equals(status.get("checkedInToday"));
        boolean canClaim = Boolean.TRUE.equals(status.get("canClaim"));
        int requiredDays = numberValue(status.get("requiredDays"), 3);
        int continuousDays = numberValue(status.get("continuousDays"), 0);

        int itemStatus;
        if (canClaim) itemStatus = 1;
        else if (checkedInToday) itemStatus = 2;
        else itemStatus = 0;

        String packageCode = readExtraText(config, "giftPackageCode", "");
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
                .handlerType("CHECKIN")
                .bizCategory("PLAY_SYSTEM")
                .progressText("连续签到" + continuousDays + "/" + requiredDays + "天")
                .status(itemStatus)
                .currentCount(continuousDays)
                .targetCount(requiredDays)
                .progressId(null)
                .done(checkedInToday && !canClaim)
                .canClaim(canClaim)
                .build();
    }

    private int numberValue(Object value, int defaultValue) {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    private String readExtraText(TaskConfig config, String field, String defaultValue) {
        if (config == null || !StringUtils.hasText(config.getExtraConfig())) return defaultValue;
        try {
            JsonNode root = objectMapper.readTree(config.getExtraConfig());
            JsonNode value = root.get(field);
            return value == null || value.isNull() ? defaultValue : value.asText(defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
