package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 签到配置表实体：bp_checkin_config
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinConfig {
    private Long id;
    private Integer continuousDaysRequired;
    private String rewardType;
    private Integer rewardValue;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
