package com.beanpattern.model.vo;

import com.beanpattern.entity.BpUserGift;

public class GiftVO {

    private Long id;
    private Long userId;
    private Long giftItemId;
    private String giftCode;
    private String giftName;
    private String giftCategory;
    private Integer value;
    private String source;
    private Long taskId;
    private Long shareRecordId;
    private Long orderId;
    private Integer status;
    private String usageMode;
    private String targetTab;
    private String usedAt;
    private String expireAt;
    private String createdAt;

    public static GiftVO from(BpUserGift g) {
        GiftVO vo = new GiftVO();
        vo.id = g.getId();
        vo.userId = g.getUserId();
        vo.giftItemId = g.getGiftId();
        vo.giftCode = g.getGiftType();
        vo.giftName = g.getGiftName();
        vo.giftCategory = resolveCategory(g.getGiftType());
        vo.value = g.getGiftValue();
        vo.source = g.getSource();
        vo.taskId = g.getSourceId() != null && g.getSource() != null && g.getSource().startsWith("TASK") ? g.getSourceId() : null;
        vo.shareRecordId = g.getSourceId() != null && g.getSource() != null && g.getSource().startsWith("SHARE") ? g.getSourceId() : null;
        vo.orderId = g.getSourceId() != null && g.getSource() != null && g.getSource().startsWith("ORDER") ? g.getSourceId() : null;
        vo.status = "UNUSED".equals(g.getStatus()) ? 0 : "USED".equals(g.getStatus()) ? 1 : 2;
        vo.usedAt = g.getUsedAt() != null ? g.getUsedAt().toString() : null;
        vo.expireAt = g.getExpireAt() != null ? g.getExpireAt().toString() : null;
        vo.createdAt = g.getCreatedAt() != null ? g.getCreatedAt().toString() : null;
        resolveUsage(vo);
        return vo;
    }

    private static String resolveCategory(String giftType) {
        if (giftType == null) return null;
        if (giftType.endsWith("_COUPON") || giftType.equals("COUPON")) return "COUPON";
        if (giftType.equals("GIFT_PACKAGE")) return "GIFT_PACKAGE";
        return giftType;
    }

    private static void resolveUsage(GiftVO vo) {
        String code = vo.giftCode != null ? vo.giftCode : "";
        String category = vo.giftCategory != null ? vo.giftCategory : "";
        if ("COUPON".equals(category)
                || code.endsWith("_COUPON")
                || "VIP_COUPON".equals(code)
                || "CARD_COUPON".equals(code)
                || "VIP_CARD_COUPON".equals(code)) {
            vo.usageMode = "JUMP_VIP";
            vo.targetTab = code.contains("CARD") ? "cards" : "vip";
        }
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getGiftItemId() { return giftItemId; }
    public String getGiftCode() { return giftCode; }
    public String getGiftName() { return giftName; }
    public String getGiftCategory() { return giftCategory; }
    public Integer getValue() { return value; }
    public String getSource() { return source; }
    public Long getTaskId() { return taskId; }
    public Long getShareRecordId() { return shareRecordId; }
    public Long getOrderId() { return orderId; }
    public Integer getStatus() { return status; }
    public String getUsageMode() { return usageMode; }
    public String getTargetTab() { return targetTab; }
    public String getUsedAt() { return usedAt; }
    public String getExpireAt() { return expireAt; }
    public String getCreatedAt() { return createdAt; }
}
