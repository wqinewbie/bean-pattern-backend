package com.beanpattern.service;

import java.util.List;
import java.util.Map;

public record AiPatternPipelineResult(
        AiPatternCandidate selectedCandidate,
        List<AiPatternCandidate> candidates,
        String processMetaJson
) {
    public List<List<Map<String, Object>>> mappedPixelData() {
        return selectedCandidate.mappedPixelData();
    }

    public int colorCount() {
        return selectedCandidate.colorCount();
    }

    public String selectedImageUrl() {
        return selectedCandidate.imageUrl();
    }

    public String selectedImageVariant() {
        return selectedCandidate.variant();
    }
}
