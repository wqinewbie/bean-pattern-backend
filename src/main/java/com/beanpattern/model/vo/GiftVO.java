package com.beanpattern.model.vo;

import com.beanpattern.entity.BpUserGift;
import com.beanpattern.entity.UserGift;

/**
 * 礼品视图对象——统一 UserGift(旧) 和 BpUserGift(新) 的序列化格式
 */
public class GiftVO {

    private Long id;
    private Long userId;
    private Long giftItemId;        // UserGift 字段名（兼容前端）
    private String giftCode;        // UserGift 字段名
    private String giftName;
    private String giftCategory;    // UserGift 字段名
    private Integer value;          // UserGift 字段名
    private String source;
    private Long taskId;            // UserGift 字段名
    private Long shareRecordId;     // UserGift 字段名
    private Long orderId;           // UserGift 字段名
    private Integer status;         // 0=未使用, 1=已使用, 2=已过期（兼容旧格式）
    private String usedAt;
    private String expireAt;
    private String createdAt;

    public static GiftVO from(UserGift g) {
        GiftVO vo = new GiftVO();
        vo.id = g.getId();
        vo.userId = g.getUserId();
        vo.giftItemId = g.getGiftItemId();
        vo.giftCode = g.getGiftCode();
        vo.giftName = g.getGiftName();
        vo.giftCategory = g.getGiftCategory();
        vo.value = g.getValue();
        vo.source = g.getSource();
        vo.taskId = g.getTaskId();
        vo.shareRecordId = g.getShareRecordId();
        vo.orderId = g.getOrderId();
        vo.status = g.getStatus();
        vo.usedAt = g.getUsedAt() != null ? g.getUsedAt().toString() : null;
        vo.expireAt = g.getExpireAt() != null ? g.getExpireAt().toString() : null;
        vo.createdAt = g.getCreatedAt() != null ? g.getCreatedAt().toString() : null;
        return vo;
    }

    public static GiftVO from(BpUserGift g) {
        GiftVO vo = new GiftVO();
        vo.id = g.getId();
        vo.userId = g.getUserId();
        vo.giftItemId = g.getGiftId();
        vo.giftCode = g.getGiftType();
        vo.giftName = g.getGiftName();
        vo.giftCategory = g.getGiftType();
        vo.value = g.getGiftValue();
        vo.source = g.getSource();
        vo.taskId = g.getSourceId() != null && g.getSource() != null && g.getSource().startsWith("TASK") ? g.getSourceId() : null;
        vo.shareRecordId = g.getSourceId() != null && g.getSource() != null && g.getSource().startsWith("SHARE") ? g.getSourceId() : null;
        vo.orderId = g.getOrderNo() != null ? Long.parseLong(g.getOrderNo()) : null;
        vo.status = "UNUSED".equals(g.getStatus()) ? 0 : "USED".equals(g.getStatus()) ? 1 : 2;
        vo.usedAt = g.getUsedAt() != null ? g.getUsedAt().toString() : null;
        vo.expireAt = g.getExpireAt() != null ? g.getExpireAt().toString() : null;
        vo.createdAt = g.getCreatedAt() != null ? g.getCreatedAt().toString() : null;
        return vo;
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
    public String getUsedAt() { return usedAt; }
    public String getExpireAt() { return expireAt; }
    public String getCreatedAt() { return createdAt; }
}
