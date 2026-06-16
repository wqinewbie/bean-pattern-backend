package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.BpUserGiftMapper;
import com.beanpattern.mapper.TaskConfigMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.GiftPackageService;
import com.beanpattern.service.InviteCodeService;
import com.beanpattern.service.task.GiftPackageRewardHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invite")
public class InviteController {

    private static final String INVITE_REGISTER_GIFT_PREFIX = "INVITE_REGISTER_GIFT:";

    private final SessionHelper sessionHelper;
    private final InviteCodeService inviteCodeService;
    private final TaskConfigMapper taskConfigMapper;
    private final BpUserGiftMapper bpUserGiftMapper;
    private final GiftPackageService giftPackageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InviteController(SessionHelper sessionHelper,
                            InviteCodeService inviteCodeService,
                            TaskConfigMapper taskConfigMapper,
                            BpUserGiftMapper bpUserGiftMapper,
                            GiftPackageService giftPackageService) {
        this.sessionHelper = sessionHelper;
        this.inviteCodeService = inviteCodeService;
        this.taskConfigMapper = taskConfigMapper;
        this.bpUserGiftMapper = bpUserGiftMapper;
        this.giftPackageService = giftPackageService;
    }

    @GetMapping("/my-code")
    public ApiResponse<Map<String, Object>> myCode(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        String inviteCode = inviteCodeService.ensureInviteCode(user);
        int registeredCount = inviteCodeService.countInvitedRegistered(user.getId());
        int paidCount = inviteCodeService.countInvitedPaid(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("inviteCode", inviteCode);
        result.put("registeredCount", registeredCount);
        result.put("paidCount", paidCount);

        Map<String, Object> nextReward = buildNextReward(user.getId());
        if (nextReward != null) {
            result.put("nextReward", nextReward);
        }

        return ApiResponse.ok(result);
    }

    @PostMapping("/bind")
    public ApiResponse<Map<String, Object>> bind(HttpServletRequest request, @RequestBody Map<String, String> body) {
        UserEntity user = sessionHelper.requireUser(request);
        String inviteCode = body == null ? "" : body.getOrDefault("inviteCode", "");
        inviteCodeService.bindInviteRelation(user.getId(), inviteCode);
        return ApiResponse.ok(Map.of("success", true));
    }

    @GetMapping("/records")
    public ApiResponse<Map<String, Object>> records(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        String inviteCode = inviteCodeService.ensureInviteCode(user);
        List<Map<String, Object>> records = inviteCodeService.listInvites(user.getId()).stream().map(item -> Map.<String, Object>of(
                "id", item.getId(),
                "inviteeUserId", item.getInviteeUserId(),
                "nickName", item.getInviteeNickName() == null ? "新朋友" : item.getInviteeNickName(),
                "avatarUrl", item.getInviteeAvatarUrl() == null ? "" : item.getInviteeAvatarUrl(),
                "status", item.getStatus() == null ? 1 : item.getStatus(),
                "firstPaidAt", item.getFirstPaidAt() == null ? "" : item.getFirstPaidAt().toString(),
                "createdAt", item.getCreatedAt() == null ? "" : item.getCreatedAt().toString()
        )).toList();
        return ApiResponse.ok(Map.of(
                "inviteCode", inviteCode,
                "registeredCount", inviteCodeService.countInvitedRegistered(user.getId()),
                "paidCount", inviteCodeService.countInvitedPaid(user.getId()),
                "records", records
        ));
    }

    private Map<String, Object> buildNextReward(Long userId) {
        TaskConfig config = taskConfigMapper.findByCode("invite_friend");
        if (config == null) {
            config = taskConfigMapper.findByCode("invite_register");
        }
        if (config == null || !StringUtils.hasText(config.getExtraConfig())) {
            return null;
        }

        JsonNode extra;
        try {
            extra = objectMapper.readTree(config.getExtraConfig());
        } catch (Exception e) {
            return null;
        }

        int targetCount = extra.has("targetCount") ? extra.get("targetCount").asInt(1) : 1;
        String giftPackageCode = extra.has("giftPackageCode") ? extra.get("giftPackageCode").asText() : "";

        int currentCount = inviteCodeService.countInvitedRegistered(userId);
        int claimedRounds = 0;
        if (StringUtils.hasText(giftPackageCode)) {
            claimedRounds = bpUserGiftMapper.countByUserIdAndSourcePrefix(userId,
                    INVITE_REGISTER_GIFT_PREFIX + giftPackageCode + ":");
        }

        int nextTarget = (claimedRounds + 1) * targetCount;
        int needCount = Math.max(nextTarget - currentCount, 0);

        Map<String, Object> reward = new HashMap<>();
        reward.put("needCount", needCount);
        reward.put("targetCount", targetCount);
        reward.put("currentCount", currentCount);

        if (needCount == 0) {
            reward.put("description", "已达标，可领取奖励");
        } else {
            reward.put("description", "再邀请" + needCount + "人注册可获得");
        }

        if (StringUtils.hasText(giftPackageCode)) {
            GiftPackage giftPackage = giftPackageService.getByCode(giftPackageCode);
            if (giftPackage != null) {
                List<com.beanpattern.entity.RewardItem> rewardItems = GiftPackageRewardHelper.parseAllRewardItems(giftPackage);
                reward.put("rewardItems", rewardItems.stream().map(item -> Map.of(
                        "type", item.getType() != null ? item.getType() : "",
                        "value", item.getValue() != null ? item.getValue() : 0,
                        "displayText", item.getDisplayText() != null ? item.getDisplayText() : ""
                )).toList());
                if (!rewardItems.isEmpty()) {
                    reward.put("rewardText", rewardItems.get(0).getDisplayText());
                }
            }
        }

        return reward;
    }
}
