package com.beanpattern.model.vo;

import com.beanpattern.entity.BannerEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

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
    private String  tagText;
    private Integer sortOrder;
    private Integer status;

    public static BannerVO from(BannerEntity b) {
        BannerVO vo = new BannerVO();
        vo.id        = b.getId();
        vo.title     = b.getTitle()     != null ? b.getTitle()     : "";
        vo.subTitle  = b.getSubTitle()  != null ? b.getSubTitle()  : "";
        vo.imageUrl  = b.getImageUrl()  != null ? b.getImageUrl()  : "";
        vo.linkType  = b.getLinkType()  != null ? b.getLinkType()  : "NONE";
        vo.linkValue = b.getLinkValue() != null ? b.getLinkValue() : "";
        vo.tagText   = b.getTagText()   != null ? b.getTagText()   : "";
        vo.sortOrder = b.getSortOrder() != null ? b.getSortOrder() : 0;
        vo.status    = b.getStatus()    != null ? b.getStatus()    : 1;
        return vo;
    }

    public Long    getId()        { return id; }
    public String  getTitle()     { return title; }
    public String  getSubTitle()  { return subTitle; }
    public String  getImageUrl()  { return imageUrl; }
    public String  getLinkType()  { return linkType; }
    public String  getLinkValue() { return linkValue; }
    public String  getTagText()   { return tagText; }
    public Integer getSortOrder() { return sortOrder; }
    public Integer getStatus()    { return status; }
}
