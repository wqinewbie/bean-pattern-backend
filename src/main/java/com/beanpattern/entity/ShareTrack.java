package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分享追踪表实体：bp_share_track
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareTrack {
    private Long id;
    private Long sharerId;             // 分享者用户ID
    private Long visitorId;            // 访问者用户ID（可能为空）
    private String visitorOpenid;      // 访问者OpenID
    private String taskCode;           // 任务代码
    private LocalDate shareDate;       // 分享日期
    private Boolean isValid;           // 是否有效
    private LocalDateTime createdAt;   // 创建时间
}
