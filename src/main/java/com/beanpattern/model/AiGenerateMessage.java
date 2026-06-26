package com.beanpattern.model;

import java.util.List;

public class AiGenerateMessage {

    private String taskId;
    private String imageUrl;
    private String userPrompt;
    private String style;
    private String promptTemplate;
    private String negativePromptTemplate;
    private String modelKey;
    private String sizeMode;
    private String sizePreset;
    private String sizePresetName;
    private Integer gridMin;
    private Integer gridMax;
    private List<Integer> candidateGrids;
    private Integer defaultGrid;
    private String brand;
    private Integer colorCount;
    private Boolean mirror;
    private Boolean skipPerfectPixel;
    private String createdAt;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getPromptTemplate() {
        return promptTemplate;
    }

    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }

    public String getNegativePromptTemplate() {
        return negativePromptTemplate;
    }

    public void setNegativePromptTemplate(String negativePromptTemplate) {
        this.negativePromptTemplate = negativePromptTemplate;
    }

    public String getModelKey() {
        return modelKey;
    }

    public void setModelKey(String modelKey) {
        this.modelKey = modelKey;
    }

    public String getSizeMode() {
        return sizeMode;
    }

    public void setSizeMode(String sizeMode) {
        this.sizeMode = sizeMode;
    }

    public String getSizePreset() {
        return sizePreset;
    }

    public void setSizePreset(String sizePreset) {
        this.sizePreset = sizePreset;
    }

    public String getSizePresetName() {
        return sizePresetName;
    }

    public void setSizePresetName(String sizePresetName) {
        this.sizePresetName = sizePresetName;
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

    public List<Integer> getCandidateGrids() {
        return candidateGrids;
    }

    public void setCandidateGrids(List<Integer> candidateGrids) {
        this.candidateGrids = candidateGrids;
    }

    public Integer getDefaultGrid() {
        return defaultGrid;
    }

    public void setDefaultGrid(Integer defaultGrid) {
        this.defaultGrid = defaultGrid;
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

    public Boolean getSkipPerfectPixel() {
        return skipPerfectPixel;
    }

    public void setSkipPerfectPixel(Boolean skipPerfectPixel) {
        this.skipPerfectPixel = skipPerfectPixel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
