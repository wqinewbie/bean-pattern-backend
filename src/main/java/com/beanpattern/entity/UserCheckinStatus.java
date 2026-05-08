package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到状态表实体：bp_user_checkin_status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCheckinStatus {
    private Long id;
    private Long userId;               // 用户ID
    private Integer continuousDays;    // 当前连续签到天数
    private Integer totalDays;         // 累计签到天数
    private LocalDate lastCheckinDate; // 最后签到日期
    private Boolean canClaim;          // 是否可以领取奖励
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
}
