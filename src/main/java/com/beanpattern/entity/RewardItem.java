package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 奖励项明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardItem {
    /**
     * 奖励类型：AI_COUNT, VIP_DAYS, COINS 等
     */
    private String type;

    /**
     * 奖励数量
     */
    private Integer value;

    /**
     * 显示文本，如 "10次AI对话"、"7天VIP"
     */
    private String displayText;
}
