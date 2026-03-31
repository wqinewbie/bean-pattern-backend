package com.beanpattern.model.vo;

import com.beanpattern.entity.RechargePlanEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * 充值套餐视图对象
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RechargePlanVO {

    private Long       id;
    private String     name;
    private String     description;
    private Integer    coins;
    private Integer    aiQuota;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer    isVip;
    private Integer    vipDays;
    private String     tag;
    private Integer    sortOrder;

    public static RechargePlanVO from(RechargePlanEntity e) {
        RechargePlanVO vo = new RechargePlanVO();
        vo.id            = e.getId();
        vo.name          = e.getName()          != null ? e.getName()          : "";
        vo.description   = e.getDescription()   != null ? e.getDescription()   : "";
        vo.coins         = e.getCoins()         != null ? e.getCoins()         : 0;
        vo.aiQuota       = e.getAiQuota()       != null ? e.getAiQuota()       : 0;
        vo.price         = e.getPrice();
        vo.originalPrice = e.getOriginalPrice();
        vo.isVip         = e.getIsVip()         != null ? e.getIsVip()         : 0;
        vo.vipDays       = e.getVipDays()       != null ? e.getVipDays()       : 0;
        vo.tag           = e.getTag()           != null ? e.getTag()           : "";
        vo.sortOrder     = e.getSortOrder()     != null ? e.getSortOrder()     : 0;
        return vo;
    }

    public Long       getId()            { return id; }
    public String     getName()          { return name; }
    public String     getDescription()   { return description; }
    public Integer    getCoins()         { return coins; }
    public Integer    getAiQuota()       { return aiQuota; }
    public BigDecimal getPrice()         { return price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public Integer    getIsVip()         { return isVip; }
    public Integer    getVipDays()       { return vipDays; }
    public String     getTag()           { return tag; }
    public Integer    getSortOrder()     { return sortOrder; }
}
