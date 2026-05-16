package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签到奖励领取记录表实体：bp_user_checkin_claim
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCheckinClaim {
    private Long id;
    private Long userId;               // 用户ID
    private LocalDate claimDate;       // 领取日期
    private Integer continuousDays;    // 连续签到天数（达到多少天领取的）
    private LocalDateTime createdAt;   // 创建时间
}
