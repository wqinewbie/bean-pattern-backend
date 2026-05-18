package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 活动配置表实体：bp_activity_config
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityConfig {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private Long id;
    private String activityCode;       // 活动编码
    private String title;              // 活动标题
    private String description;        // 活动描述
    private String coverImage;         // 封面图片
    private String activityType;       // 活动类型：CONTENT/GIFT
    private String giftPackageCode;    // 绑定礼品包编码
    private String limitType;          // 限制类型：ONCE/DAILY/UNLIMITED
    private Integer totalQuota;        // 总名额
    private Integer remainQuota;       // 剩余名额
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime startAt;     // 活动开始时间
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime endAt;       // 活动结束时间
    private String contentHtml;        // 富文本HTML内容
    private String contentJson;        // 富文本JSON结构
    private String pageType;           // 页面类型：RICH_TEXT/CUSTOM
    private String buttonText;         // 按钮文案
    private String buttonAction;       // 按钮动作：CLAIM/RECHARGE/JUMP
    private String buttonUrl;
    private Long bannerId;
    private String giftItems;
    private String discountConfig;
    private String taskConfig;
    private Boolean status;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime createdAt;   // 创建时间
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime updatedAt;   // 更新时间
}
