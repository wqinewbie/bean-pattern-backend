package com.beanpattern.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BpBox {
    // 状态常量
    public static final int STATUS_PROCESSING = 0; // 处理中
    public static final int STATUS_COMPLETED = 1;  // 已完成
    public static final int STATUS_INVALID = 2;    // 已失效
    public static final int STATUS_DELETED = 3;    // 已删除（逻辑删除）

    private Long id;
    private Long userId;
    @NotBlank(message = "来源类型不能为空")
    private String sourceType;  // LOCAL, AI, DRAW
    @NotBlank(message = "品牌不能为空")
    private String brand;       // 品牌，如mard
    @Positive(message = "色数必须大于0")
    private Integer colorCount; // 色数，如48
    @NotBlank(message = "图纸名称不能为空")
    @Size(max = 128, message = "图纸名称最长128字符")
    private String name;        // 图纸名称
    @NotNull(message = "尺寸不能为空")
    @Positive(message = "尺寸必须大于0")
    private Integer gridSize;   // 尺寸，如64表示64x64
    private Long draftId;       // 关联草稿箱ID
    private Long historyId;     // 关联时光机ID
    private String sourceUrl;   // 原图URL
    private String coverUrl;    // 封面图URL（压缩图）
    private Integer status;     // 0=处理中 1=已完成 2=已失效 3=已删除

    private String mappedPixelData; // 主图案JSON(二维像素对象)
    private String focusProgress;   // 沉浸模式进度JSON
    private Integer focusCompletedCells; // 沉浸模式已完成格子数
    private Integer focusTotalCells;     // 沉浸模式总格子数

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
