package com.beanpattern.entity;

import java.time.LocalDateTime;

/**
 * 图片/图纸任务记录表实体：bp_image_task
 */
public class ImageTaskEntity {
    private Long id;
    private Long userId;
    private String taskType;
    private String sourceUrl;   // 原图 URL（持久化存储地址）
    private String resultUrl;   // 结果图 URL
    private String patternUrl;  // 图纸 URL（带编号）
    private String colorStats;  // 颜色统计 JSON
    private String status;
    private String errorMessage;
    private Integer isSaved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getResultUrl() { return resultUrl; }
    public void setResultUrl(String resultUrl) { this.resultUrl = resultUrl; }

    public String getPatternUrl() { return patternUrl; }
    public void setPatternUrl(String patternUrl) { this.patternUrl = patternUrl; }

    public String getColorStats() { return colorStats; }
    public void setColorStats(String colorStats) { this.colorStats = colorStats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getIsSaved() { return isSaved; }
    public void setIsSaved(Integer isSaved) { this.isSaved = isSaved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
