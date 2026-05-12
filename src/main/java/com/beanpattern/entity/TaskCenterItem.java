package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务中心返回给前端的聚合模型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCenterItem {
    private Long taskId;
    private String taskCode;
    private String taskName;
    private String taskType;
    private String description;
    private String rewardType;
    private Integer rewardValue;
    private String icon;
    private Integer sortOrder;
    private String handlerType;
    private String bizCategory;
    private String progressText;
    private Integer status;
    private Integer currentCount;
    private Integer targetCount;
    private Long progressId;
    private Boolean done;
    private Boolean canClaim;
}
