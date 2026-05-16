package com.beanpattern.service.task;

import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.UserGiftMapper;
import com.beanpattern.service.GiftPackageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 首冲礼包/首充福利的聚合实现。
 *
 * 本质上属于资格福利，不是过程型任务：
 * - 看用户是否发生过首次支付
 * - 看是否已经领取过对应礼包
 */
@Component
public class FirstRechargeTaskHandler implements TaskHandler {

    private static final String CLAIM_SOURCE_PREFIX = "FIRST_RECHARGE_GIFT:";

    private final OrderMapper orderMapper;
    private final UserGiftMapper userGiftMapper;
    private final GiftPackageService giftPackageService;
    private final ObjectMapper objectMapper;

    public FirstRechargeTaskHandler(OrderMapper orderMapper, UserGiftMapper userGiftMapper, GiftPackageService giftPackageService) {
        this.orderMapper = orderMapper;
        this.userGiftMapper = userGiftMapper;
        this.giftPackageService = giftPackageService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(TaskConfig config) {
        String handlerType = readExtraText(config, "handlerType", "");
        return "FIRST_RECHARGE_GIFT".equals(handlerType)
                || "first_recharge_gift".equals(config.getTaskCode())
                || "first_recharge".equals(config.getTaskCode());
    }

    @Override
    public TaskCenterItem buildTaskItem(Long userId, TaskConfig config) {
        boolean hasPaidOrder = hasPaidOrder(userId);
        String packageCode = resolveGiftPackageCode(config);
        boolean claimed = hasClaimed(userId, packageCode);

        int status;
        if (claimed) status = 2;
        else if (hasPaidOrder) status = 1;
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
                .handlerType("FIRST_RECHARGE_GIFT")
                .bizCategory("BENEFIT")
                .progressText(claimed ? "已领取首冲礼包" : hasPaidOrder ? "已满足首冲资格，待领取" : "完成首次充值后可领取")
                .status(status)
                .currentCount(hasPaidOrder ? 1 : 0)
                .targetCount(1)
                .progressId(null)
                .done(claimed)
                .canClaim(hasPaidOrder && !claimed)
                .build();
    }

    private boolean hasPaidOrder(Long userId) {
        List<OrderEntity> orders = orderMapper.listByUserId(userId);
        return orders.stream().anyMatch(order -> "PAID".equalsIgnoreCase(order.getStatus()));
    }

    private boolean hasClaimed(Long userId, String packageCode) {
        if (!StringUtils.hasText(packageCode)) return false;
        return userGiftMapper.findByUserId(userId).stream()
                .map(UserGift::getSource)
                .filter(StringUtils::hasText)
                .anyMatch(source -> source.equals(CLAIM_SOURCE_PREFIX + packageCode));
    }

    private String resolveGiftPackageCode(TaskConfig config) {
        return readExtraText(config, "giftPackageCode", "");
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
