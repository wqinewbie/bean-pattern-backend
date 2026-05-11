package com.beanpattern.model.vo;

import com.beanpattern.entity.BannerEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Banner视图对象
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BannerVO {

    private Long    id;
    private String  title;
    private String  subTitle;
    private String  imageUrl;
    private String  linkType;
    private String  linkValue;
    private String  actionType;
    private String  actionConfig;
    private String  tagText;
    @JsonProperty("bgColor")
    private String  bgColor;
    private Integer sortOrder;
    private Integer status;

    public static BannerVO from(BannerEntity b) {
        BannerVO vo = new BannerVO();
        vo.id = b.getId();
        vo.title = sanitizeText(b.getTitle(), "初夏限定拼豆");
        vo.subTitle = sanitizeText(b.getSubTitle(), "一键生成专属图纸");
        vo.imageUrl = b.getImageUrl() != null ? b.getImageUrl() : "";
        vo.linkType = b.getLinkType() != null ? b.getLinkType() : "NONE";
        vo.linkValue = b.getLinkValue() != null ? b.getLinkValue() : "";
        vo.actionType = b.getActionType() != null ? b.getActionType() : vo.linkType;
        vo.actionConfig = b.getActionConfig() != null ? b.getActionConfig() : "";
        vo.tagText = sanitizeText(b.getTagText(), "魔法上新");

        String rawBg = b.getBgColor();
        vo.bgColor = (rawBg != null && !rawBg.trim().isEmpty()) ? rawBg.trim() : "#FF9800";

        vo.sortOrder = b.getSortOrder() != null ? b.getSortOrder() : 0;
        vo.status = b.getStatus() != null ? b.getStatus() : 1;
        return vo;
    }

    private static String sanitizeText(String value, String fallback) {
        if (value == null) return fallback;
        String t = value.trim();
        if (t.isEmpty()) return fallback;
        if (t.contains("�")) return fallback;
        if (t.matches(".*[鑴婢閸娴鈥].*")) return fallback;
        if (looksLikeMojibake(t)) return fallback;
        return t;
    }

    private static boolean looksLikeMojibake(String s) {
        int high = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x80 && c <= 0xFF) high++;
        }
        return high >= 3;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubTitle() { return subTitle; }
    public String getImageUrl() { return imageUrl; }
    public String getLinkType() { return linkType; }
    public String getLinkValue() { return linkValue; }
    public String getActionType() { return actionType; }
    public String getActionConfig() { return actionConfig; }
    public String getTagText() { return tagText; }
    public String getBgColor() { return bgColor; }
    public Integer getSortOrder() { return sortOrder; }
    public Integer getStatus() { return status; }
}
