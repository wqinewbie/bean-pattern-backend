package com.beanpattern.model.dict;

public class DictOption {
    private String label;
    private String value;
    private String color;
    private String tagType;
    private Boolean disabled;
    private Integer sort;
    private String remark;

    public DictOption() {
    }

    public DictOption(String label, String value, String tagType, int sort) {
        this.label = label;
        this.value = value;
        this.color = tagType;
        this.tagType = tagType;
        this.disabled = false;
        this.sort = sort;
        this.remark = "";
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
