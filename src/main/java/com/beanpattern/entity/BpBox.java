package com.beanpattern.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BpBox {
    private Long id;
    private Long userId;
    private String sourceType;  // LOCAL, AI, DRAW
    private String brand;      // 品牌，如mard
    private Integer colorCount; // 色数，如48
    private String name;        // 图纸名称
    private Integer gridSize;   // 尺寸，如64表示64x64
    private String rgbData;     // 原始RGB数组
    private String gridData;    // 色号索引数组
    private String colorPalette; // 颜色表JSON
    private Long draftId;      // 关联草稿箱ID
    private Long historyId;   // 关联时光机ID
    private String sourceUrl;  // 原图URL
    private Integer status;    // 0=处理中 1=已完成 2=已失效
    private String progressData; // 沉浸模式进度JSON
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
