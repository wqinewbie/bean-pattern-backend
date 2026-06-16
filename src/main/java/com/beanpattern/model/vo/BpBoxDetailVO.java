package com.beanpattern.model.vo;

import com.beanpattern.entity.BpBox;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BpBoxDetailVO {
    private Long id;
    private Long userId;
    private String sourceType;
    private String brand;
    private Integer colorCount;
    private String name;
    private Integer gridSize;
    private Long draftId;
    private Long historyId;
    private String sourceUrl;
    private String originalImageUrl;
    private String inputImageUrl;
    private String coverUrl;
    private Integer status;
    private String mappedPixelData;
    private String aiStyle;
    private String focusProgress;
    private Integer focusCompletedCells;
    private Integer focusTotalCells;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BpBoxDetailVO from(BpBox box) {
        BpBoxDetailVO vo = new BpBoxDetailVO();
        if (box == null) {
            return vo;
        }
        vo.setId(box.getId());
        vo.setUserId(box.getUserId());
        vo.setSourceType(box.getSourceType());
        vo.setBrand(box.getBrand());
        vo.setColorCount(box.getColorCount());
        vo.setName(box.getName());
        vo.setGridSize(box.getGridSize());
        vo.setDraftId(box.getDraftId());
        vo.setHistoryId(box.getHistoryId());
        vo.setSourceUrl(box.getSourceUrl());
        vo.setCoverUrl(box.getCoverUrl());
        vo.setStatus(box.getStatus());
        vo.setMappedPixelData(box.getMappedPixelData());
        vo.setAiStyle(box.getAiStyle());
        vo.setFocusProgress(box.getFocusProgress());
        vo.setFocusCompletedCells(box.getFocusCompletedCells());
        vo.setFocusTotalCells(box.getFocusTotalCells());
        vo.setCreatedAt(box.getCreatedAt());
        vo.setUpdatedAt(box.getUpdatedAt());
        return vo;
    }
}
