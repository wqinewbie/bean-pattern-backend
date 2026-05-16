package com.beanpattern.service.task;

import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.RewardItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GiftPackageRewardHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static class RewardInfo {
        private String displayType;
        private int displayValue;
        private List<RewardItem> items;

        public RewardInfo(String displayType, int displayValue, List<RewardItem> items) {
            this.displayType = displayType;
            this.displayValue = displayValue;
            this.items = items;
        }

        public String getDisplayType() { return displayType; }
        public int getDisplayValue() { return displayValue; }
        public List<RewardItem> getItems() { return items; }
    }

    public static RewardInfo parseGiftPackageReward(GiftPackage giftPackage) {
        if (giftPackage == null || !StringUtils.hasText(giftPackage.getItemsJson())) {
            return new RewardInfo("GIFT_PACKAGE", 1, new ArrayList<>());
        }

        try {
            JsonNode items = objectMapper.readTree(giftPackage.getItemsJson());
            if (!items.isArray() || items.isEmpty()) {
                return new RewardInfo("GIFT_PACKAGE", 1, new ArrayList<>());
            }

            List<RewardItem> rewardItems = new ArrayList<>();
            JsonNode firstItem = items.get(0);
            String firstType = readText(firstItem, "type", "AI_COUNT");
            double firstValue = readDouble(firstItem, "value", 1);

            for (JsonNode item : items) {
                String type = readText(item, "type", "");
                int value = (int) Math.round(readDouble(item, "value", 0));
                if (StringUtils.hasText(type) && value > 0) {
                    String displayText = formatRewardText(type, value);
                    rewardItems.add(RewardItem.builder()
                            .type(type)
                            .value(value)
                            .displayText(displayText)
                            .build());
                }
            }

            String displayType = mapToDisplayType(firstType);
            int displayValue = (int) Math.round(firstValue);

            return new RewardInfo(displayType, displayValue, rewardItems);
        } catch (Exception e) {
            return new RewardInfo("GIFT_PACKAGE", 1, new ArrayList<>());
        }
    }

    public static RewardItem parseRewardInfo(GiftPackage giftPackage) {
        RewardInfo info = parseGiftPackageReward(giftPackage);
        List<RewardItem> items = info.getItems();
        if (!items.isEmpty()) {
            return items.get(0);
        }
        return new RewardItem(info.getDisplayType(), info.getDisplayValue(), null);
    }

    public static List<RewardItem> parseAllRewardItems(GiftPackage giftPackage) {
        return parseGiftPackageReward(giftPackage).getItems();
    }

    private static String formatRewardText(String type, int value) {
        return switch (type) {
            case "AI_COUNT", "AI_QUOTA" -> value + "次AI对话";
            case "VIP_DAYS" -> value + "天VIP";
            case "COINS" -> value + "金币";
            default -> "奖励礼包";
        };
    }

    private static String mapToDisplayType(String giftType) {
        if ("AI_QUOTA".equals(giftType) || "AI_COUNT".equals(giftType)) {
            return "AI_COUNT";
        }
        if ("VIP_DAYS".equals(giftType)) {
            return "VIP_DAYS";
        }
        if ("COINS".equals(giftType)) {
            return "COINS";
        }
        return "GIFT_PACKAGE";
    }

    private static String readText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asText();
    }

    private static double readDouble(JsonNode node, String field, double defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asDouble();
    }
}
