package com.beanpattern.service;

import com.beanpattern.entity.BannerClaimLog;
import com.beanpattern.entity.BannerEntity;
import com.beanpattern.mapper.BannerClaimLogMapper;
import com.beanpattern.mapper.BannerMapper;
import com.beanpattern.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class BannerService {

    private final BannerMapper bannerMapper;
    private final BannerClaimLogMapper claimLogMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public BannerService(BannerMapper bannerMapper,
                        BannerClaimLogMapper claimLogMapper,
                        UserMapper userMapper) {
        this.bannerMapper = bannerMapper;
        this.claimLogMapper = claimLogMapper;
        this.userMapper = userMapper;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 领取Banner礼品包
     */
    @Transactional
    public Map<String, Object> claimBannerGift(Long userId, Long bannerId) {
        // 1. 查询Banner配置
        BannerEntity banner = bannerMapper.findById(bannerId);
        if (banner == null) {
            throw new IllegalArgumentException("Banner不存在");
        }

        if (!"CLAIM_GIFT".equals(banner.getActionType())) {
            throw new IllegalArgumentException("该Banner不支持领取礼品");
        }

        // 2. 解析礼品配置
        try {
            JsonNode config = objectMapper.readTree(banner.getActionConfig());
            String limit = config.get("limit").asText();
            String bannerCode = config.get("banner_code").asText();

            // 3. 检查领取限制
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

            // 4. 发放礼品
            JsonNode gifts = config.get("gifts");
            for (JsonNode gift : gifts) {
                String type = gift.get("type").asText();
                int value = gift.get("value").asInt();

                if ("AI_QUOTA".equals(type)) {
                    userMapper.addAiQuota(userId, value);
                } else if ("VIP_DAYS".equals(type)) {
                    userMapper.addVipDays(userId, value);
                } else if ("MAGIC_COINS".equals(type)) {
                    userMapper.addCoins(userId, value);
                }

                // 5. 记录领取日志
                BannerClaimLog log = new BannerClaimLog();
                log.setUserId(userId);
                log.setBannerId(bannerId);
                log.setBannerCode(bannerCode);
                log.setGiftType(type);
                log.setGiftValue(value);
                log.setClaimDate(today);

                try {
                    claimLogMapper.insert(log);
                } catch (DuplicateKeyException e) {
                    throw new IllegalStateException("领取失败，请勿重复领取");
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "领取成功！");
            return result;

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("领取失败：" + e.getMessage());
        }
    }
}
