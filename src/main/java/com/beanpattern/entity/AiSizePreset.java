package com.beanpattern.entity;

import java.util.Date;

public class AiSizePreset {

    private Long id;
    private String presetKey;
    private String name;
    private String description;
    private Integer gridMin;
    private Integer gridMax;
    private String candidateGrids;
    private Integer defaultGrid;
    private Integer sortOrder;
    private Integer recommended;
    private Integer enabled;
    private String remark;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPresetKey() {
        return presetKey;
    }

    public void setPresetKey(String presetKey) {
        this.presetKey = presetKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCandidateGrids() {
        return candidateGrids;
    }

    public void setCandidateGrids(String candidateGrids) {
        this.candidateGrids = candidateGrids;
    }

    public Integer getDefaultGrid() {
        return defaultGrid;
    }

    public void setDefaultGrid(Integer defaultGrid) {
        this.defaultGrid = defaultGrid;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getRecommended() {
        return recommended;
    }

    public void setRecommended(Integer recommended) {
        this.recommended = recommended;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
}
