package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理后台操作日志表实体：bp_admin_operation_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOperationLog {
    private Long id;
    private Long adminId;              // 管理员ID
    private String adminName;          // 管理员名称
    private String operationType;      // 操作类型：create/update/delete
    private String module;             // 模块：vip_package/card_package/privilege
    private Long targetId;             // 操作对象ID
    private String beforeData;         // 修改前数据（JSON）
    private String afterData;          // 修改后数据（JSON）
    private String ip;                 // IP地址
    private LocalDateTime createdAt;   // 创建时间
}
