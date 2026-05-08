package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到记录表实体：bp_user_checkin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCheckin {
    private Long id;
    private Long userId;               // 用户ID
    private LocalDate checkinDate;     // 签到日期
    private Integer continuousDays;    // 连续签到天数
    private LocalDateTime createdAt;   // 创建时间
}
