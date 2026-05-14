package com.beanpattern.service;

import com.beanpattern.entity.BannerClaimLog;
import com.beanpattern.entity.BannerEntity;
import com.beanpattern.mapper.BannerClaimLogMapper;
import com.beanpattern.mapper.BannerMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
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
     * 领取Banner礼品包
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

        try {
            JsonNode config = objectMapper.readTree(banner.getActionConfig());
            String limit = readText(config, "limit", "ONCE");
            String bannerCode = readText(config, "banner_code", "banner_" + bannerId);

            LocalDate today = LocalDate.now();
            if ("ONCE".equals(limit)) {
                int count = claimLogMapper.countByUserAndBanner(userId, bannerCode);
                if (count > 0) {
                    throw new IllegalStateException("您已领取过该礼品");
                }
            } else if ("DAILY".equals(limit)) {
                int count = claimLogMapper.countByUserAndBannerAndDate(userId, bannerCode, today);
                if (count > 0) {
                    throw new IllegalStateException("今日已领取，明天再来吧");
                }
            }

            String packageCode = readText(config, "giftPackageCode", readText(config, "packageCode", ""));
            if (!StringUtils.hasText(packageCode)) {
                throw new IllegalArgumentException("Banner必须绑定礼品包");
            }

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

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("领取失败：" + e.getMessage());
        }
    }

    private String readText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asText();
    }
}
