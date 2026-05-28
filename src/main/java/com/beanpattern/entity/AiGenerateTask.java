package com.beanpattern.entity;

import java.util.Date;

/**
 * AI生成任务实体
 */
public class AiGenerateTask {

    private Long id;
    private String taskId;
    private Long userId;

    // 请求参数
    private String imageUrl;
    private String prompt;
    private String style;
    private String sizeMode;
    private Integer gridMin;
    private Integer gridMax;
    private String brand;
    private Integer colorCount;
    private Boolean mirror;

    // 任务状态
    private String status;  // PENDING, PROCESSING, SUCCESS, FAILED

    // 结果数据
    private String aiImageUrl;
    private String rawAiImageUrl;
    private Integer detectedGridWidth;
    private Integer detectedGridHeight;
    private Integer finalGridWidth;
    private Integer finalGridHeight;
    private String perfectPixelStatus;
    private String perfectPixelError;
    private String errorMessage;

    // 时间戳
    private Date createdAt;
    private Date updatedAt;
    private Date completedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getSizeMode() {
        return sizeMode;
    }

    public void setSizeMode(String sizeMode) {
        this.sizeMode = sizeMode;
    }

    public Integer getGridMin() {
        return gridMin;
    }

    public void setGridMin(Integer gridMin) {
        this.gridMin = gridMin;
    }

    public Integer getGridMax() {
        return gridMax;
    }

    public void setGridMax(Integer gridMax) {
        this.gridMax = gridMax;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getColorCount() {
        return colorCount;
    }

    public void setColorCount(Integer colorCount) {
        this.colorCount = colorCount;
    }

    public Boolean getMirror() {
        return mirror;
    }

    public void setMirror(Boolean mirror) {
        this.mirror = mirror;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAiImageUrl() {
        return aiImageUrl;
    }

    public void setAiImageUrl(String aiImageUrl) {
        this.aiImageUrl = aiImageUrl;
    }

    public String getRawAiImageUrl() {
        return rawAiImageUrl;
    }

    public void setRawAiImageUrl(String rawAiImageUrl) {
        this.rawAiImageUrl = rawAiImageUrl;
    }

    public Integer getDetectedGridWidth() {
        return detectedGridWidth;
    }

    public void setDetectedGridWidth(Integer detectedGridWidth) {
        this.detectedGridWidth = detectedGridWidth;
    }

    public Integer getDetectedGridHeight() {
        return detectedGridHeight;
    }

    public void setDetectedGridHeight(Integer detectedGridHeight) {
        this.detectedGridHeight = detectedGridHeight;
    }

    public Integer getFinalGridWidth() {
        return finalGridWidth;
    }

    public void setFinalGridWidth(Integer finalGridWidth) {
        this.finalGridWidth = finalGridWidth;
    }

    public Integer getFinalGridHeight() {
        return finalGridHeight;
    }

    public void setFinalGridHeight(Integer finalGridHeight) {
        this.finalGridHeight = finalGridHeight;
    }

    public String getPerfectPixelStatus() {
        return perfectPixelStatus;
    }

    public void setPerfectPixelStatus(String perfectPixelStatus) {
        this.perfectPixelStatus = perfectPixelStatus;
    }

    public String getPerfectPixelError() {
        return perfectPixelError;
    }

    public void setPerfectPixelError(String perfectPixelError) {
        this.perfectPixelError = perfectPixelError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
}
