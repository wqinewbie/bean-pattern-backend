package com.beanpattern.service;

import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserGift;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 任务奖励统一发放服务。
 * 所有非付费任务奖励只发放礼品包，具体权益由用户兑换礼品包后到账。
 */
@Service
public class TaskRewardService {

    private final GiftPackageService giftPackageService;
    private final ObjectMapper objectMapper;

    public TaskRewardService(GiftPackageService giftPackageService) {
        this.giftPackageService = giftPackageService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public UserGift grantTaskPackage(Long userId, TaskConfig config, String sourcePrefix) {
        String packageCode = resolveGiftPackageCode(config);
        if (!StringUtils.hasText(packageCode)) {
            throw new IllegalStateException("任务未配置奖励礼品包");
        }
        String source = (StringUtils.hasText(sourcePrefix) ? sourcePrefix : "TASK") + ":" + config.getTaskCode() + ":" + packageCode;
        return giftPackageService.grantPackageToUser(userId, packageCode, source);
    }

    public String resolveGiftPackageCode(TaskConfig config) {
        if (config == null || !StringUtils.hasText(config.getExtraConfig())) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(config.getExtraConfig());
            JsonNode value = root.get("giftPackageCode");
            return value == null || value.isNull() ? "" : value.asText("").trim();
        } catch (Exception e) {
            return "";
        }
    }
}
