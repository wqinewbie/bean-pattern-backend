package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权益配置表实体：bp_privilege_config
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeConfig {
    private Long id;
    private String configKey;          // 配置键
    private String configName;         // 配置名称
    private String freeValue;          // 免费用户值
    private String vipValue;           // 会员用户值
    private String valueType;          // 值类型：number/boolean/string
    private String description;        // 说明
    private Integer sortOrder;         // 排序
    private Boolean isActive;          // 是否启用
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
}
