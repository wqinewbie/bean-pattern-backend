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
    private String progressData; // 沉浸模式进度JSON（旧字段，兼容保留）
    
    // V6.0 新增字段
    private String pixelData;    // 像素数据JSON（可选，用于优化大数据传输）
    private String mappedPixelData; // 前端主格式JSON（API字段，映射到pixelData）
    private String colorMapping; // 颜色映射JSON
    private String focusProgress; // 沉浸模式进度JSON（新）
    private Integer focusCompletedCells; // 沉浸模式已完成格子数
    private Integer focusTotalCells;     // 沉浸模式总格子数
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
