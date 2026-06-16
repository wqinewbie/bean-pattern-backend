package com.beanpattern.model.vo;

import com.beanpattern.entity.BpHistory;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BpHistoryDetailVO {
    private Long id;
    private Long userId;
    private String taskId;
    private String sourceType;
    private String brand;
    private Integer colorCount;
    private String name;
    private Integer gridSize;
    private Long boxId;
    private String sourceUrl;
    private String originalImageUrl;
    private String inputImageUrl;
    private String mappedPixelData;
    private String aiStyle;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public static BpHistoryDetailVO from(BpHistory history) {
        BpHistoryDetailVO vo = new BpHistoryDetailVO();
        if (history == null) {
            return vo;
        }
        vo.setId(history.getId());
        vo.setUserId(history.getUserId());
        vo.setTaskId(history.getTaskId());
        vo.setSourceType(history.getSourceType());
        vo.setBrand(history.getBrand());
        vo.setColorCount(history.getColorCount());
        vo.setName(history.getName());
        vo.setGridSize(history.getGridSize());
        vo.setBoxId(history.getBoxId());
        vo.setSourceUrl(history.getSourceUrl());
        vo.setMappedPixelData(history.getMappedPixelData());
        vo.setAiStyle(history.getAiStyle());
        vo.setExpiresAt(history.getExpiresAt());
        vo.setCreatedAt(history.getCreatedAt());
        return vo;
    }
}
