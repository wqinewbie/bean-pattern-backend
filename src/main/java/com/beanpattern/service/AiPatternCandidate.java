package com.beanpattern.service;

import java.util.List;
import java.util.Map;

public record AiPatternCandidate(
        String variant,
        String imageUrl,
        String imageKey,
        int gridSize,
        AiImageProcessor.ProcessedResult processedResult,
        AiPatternQualityScorer.QualityMetrics metrics,
        double score
) {
    public List<List<Map<String, Object>>> mappedPixelData() {
        return processedResult.mappedPixelData();
    }

    public int colorCount() {
        return processedResult.colorCount();
    }
}
