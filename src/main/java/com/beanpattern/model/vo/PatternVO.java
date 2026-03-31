package com.beanpattern.model.vo;

import com.beanpattern.entity.CreatorPatternEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 创作者图纸视图对象
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatternVO {

    private Long    id;
    private Long    userId;
    private String  title;
    private String  description;
    private String  coverUrl;
    private String  patternUrl;
    private String  gridSize;
    private Integer difficulty;
    private Integer priceCoins;
    private Integer downloadCount;
    private Integer likeCount;
    private Integer status;
    private String  rejectReason;
    private String  category;
    private String  tags;
    private String  createdAt;

    public static PatternVO from(CreatorPatternEntity p) {
        PatternVO vo = new PatternVO();
        vo.id            = p.getId();
        vo.userId        = p.getUserId();
        vo.title         = p.getTitle()         != null ? p.getTitle()         : "";
        vo.description   = p.getDescription()   != null ? p.getDescription()   : "";
        vo.coverUrl      = p.getCoverUrl()       != null ? p.getCoverUrl()      : "";
        vo.patternUrl    = p.getPatternUrl()     != null ? p.getPatternUrl()    : "";
        vo.gridSize      = p.getGridSize()       != null ? p.getGridSize()      : "";
        vo.difficulty    = p.getDifficulty()     != null ? p.getDifficulty()    : 1;
        vo.priceCoins    = p.getPriceCoins()     != null ? p.getPriceCoins()    : 0;
        vo.downloadCount = p.getDownloadCount()  != null ? p.getDownloadCount() : 0;
        vo.likeCount     = p.getLikeCount()      != null ? p.getLikeCount()     : 0;
        vo.status        = p.getStatus()         != null ? p.getStatus()        : 0;
        vo.rejectReason  = p.getRejectReason();
        vo.category      = p.getCategory()       != null ? p.getCategory()      : "";
        vo.tags          = p.getTags()           != null ? p.getTags()          : "";
        vo.createdAt     = p.getCreatedAt()      != null ? p.getCreatedAt().toString() : "";
        return vo;
    }

    public Long    getId()            { return id; }
    public Long    getUserId()        { return userId; }
    public String  getTitle()         { return title; }
    public String  getDescription()   { return description; }
    public String  getCoverUrl()      { return coverUrl; }
    public String  getPatternUrl()    { return patternUrl; }
    public String  getGridSize()      { return gridSize; }
    public Integer getDifficulty()    { return difficulty; }
    public Integer getPriceCoins()    { return priceCoins; }
    public Integer getDownloadCount() { return downloadCount; }
    public Integer getLikeCount()     { return likeCount; }
    public Integer getStatus()        { return status; }
    public String  getRejectReason()  { return rejectReason; }
    public String  getCategory()      { return category; }
    public String  getTags()          { return tags; }
    public String  getCreatedAt()     { return createdAt; }
}
