package com.beanpattern.service;

import com.beanpattern.entity.BannerClaimLog;
import com.beanpattern.entity.BannerEntity;
import com.beanpattern.mapper.BannerClaimLogMapper;
import com.beanpattern.mapper.BannerMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class BannerService {

    private final BannerMapper bannerMapper;
    private final BannerClaimLogMapper claimLogMapper;
    private final GiftPackageService giftPackageService;
    private final ObjectMapper objectMapper;

    public BannerService(BannerMapper bannerMapper,
                         BannerClaimLogMapper claimLogMapper,
                         GiftPackageService giftPackageService) {
        this.bannerMapper = bannerMapper;
        this.claimLogMapper = claimLogMapper;
        this.giftPackageService = giftPackageService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 领取 Banner 礼品包。
     */
    @Transactional
    public Map<String, Object> claimBannerGift(Long userId, Long bannerId) {
        BannerEntity banner = bannerMapper.findById(bannerId);
        if (banner == null) {
            throw new IllegalArgumentException("Banner不存在");
        }

        if (!"CLAIM_GIFT".equals(banner.getActionType())) {
            throw new IllegalArgumentException("该Banner不支持领取礼品");
        }
        LocalDateTime nowDateTime = LocalDateTime.now();
        if (banner.getStatus() == null || banner.getStatus() != 1
                || (banner.getStartAt() != null && nowDateTime.isBefore(banner.getStartAt()))
                || (banner.getEndAt() != null && nowDateTime.isAfter(banner.getEndAt()))) {
            throw new IllegalStateException("活动已过期");
        }

        try {
            JsonNode config = objectMapper.readTree(banner.getActionConfig());
            String limit = readText(config, "limit", "ONCE");
            String bannerCode = readText(config, "banner_code", "banner_" + bannerId);
            String packageCode = readText(config, "giftPackageCode", readText(config, "packageCode", ""));
            if (!StringUtils.hasText(packageCode)) {
                throw new IllegalArgumentException("Banner必须绑定礼品包");
            }

            LocalDate today = LocalDate.now();
            String lockName = buildClaimLockName(userId, bannerCode, limit, today);
            boolean locked = false;
            boolean releaseAfterCompletion = false;
            try {
                Integer lockResult = claimLogMapper.acquireLock(lockName, 5);
                locked = lockResult != null && lockResult == 1;
                if (!locked) {
                    throw new IllegalStateException("领取中，请稍后再试");
                }
                releaseAfterCompletion = registerLockReleaseAfterCompletion(lockName);

                enforceClaimLimit(userId, bannerCode, limit, today);

                var packageGift = giftPackageService.grantPackageToUser(userId, packageCode, "BANNER:" + bannerCode);
                BannerClaimLog log = new BannerClaimLog();
                log.setUserId(userId);
                log.setBannerId(bannerId);
                log.setBannerCode(bannerCode);
                log.setGiftType("GIFT_PACKAGE");
                log.setGiftValue(1);
                log.setClaimDate(today);
                claimLogMapper.insert(log);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "领取成功，已放入我的礼品包");
                result.put("giftId", packageGift.getId());
                result.put("giftName", packageGift.getGiftName());
                result.put("claimMode", "PACKAGE_STORED");
                return result;
            } finally {
                if (locked && !releaseAfterCompletion) {
                    claimLogMapper.releaseLock(lockName);
                }
            }

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("领取失败：" + e.getMessage());
        }
    }

    private void enforceClaimLimit(Long userId, String bannerCode, String limit, LocalDate today) {
        if ("ONCE".equals(limit)) {
            int count = claimLogMapper.countByUserAndBanner(userId, bannerCode);
            if (count > 0) {
                throw new IllegalStateException("已经参加过了哦");
            }
        } else if ("DAILY".equals(limit)) {
            int count = claimLogMapper.countByUserAndBannerAndDate(userId, bannerCode, today);
            if (count > 0) {
                throw new IllegalStateException("今日已领取，明天再来吧");
            }
        }
    }

    private String buildClaimLockName(Long userId, String bannerCode, String limit, LocalDate today) {
        String scope = "DAILY".equals(limit) ? today.toString() : "ONCE";
        String raw = userId + ":" + bannerCode + ":" + scope;
        return "bp_banner_claim:" + DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private boolean registerLockReleaseAfterCompletion(String lockName) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return false;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                claimLogMapper.releaseLock(lockName);
            }
        });
        return true;
    }

    private String readText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asText();
    }
}
