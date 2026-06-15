package com.beanpattern.service;

import com.beanpattern.entity.AiGenerateTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiPatternPipelineService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AiImageProcessor aiImageProcessor;

    @Autowired
    private AiPatternQualityScorer qualityScorer;

    @Autowired
    private ImageStorageService imageStorageService;

    public AiPatternPipelineResult process(AiGenerateTask task) {
        String brand = StringUtils.hasText(task.getBrand()) ? task.getBrand() : "MARD";
        int targetColorCount = task.getColorCount() != null ? task.getColorCount() : 0;
        boolean mirror = Boolean.TRUE.equals(task.getMirror());
        int gridSize = resolveGridSize(task);
        int similarityThreshold = resolveSimilarityThreshold(task);

        List<ImageVariant> variants = resolveVariants(task);
        List<AiPatternCandidate> candidates = new ArrayList<>();
        List<Map<String, Object>> candidateErrors = new ArrayList<>();

        for (ImageVariant variant : variants) {
            try {
                ImageStorageService.StoredImage image = readVariant(variant);
                AiImageProcessor.ProcessedResult processed = aiImageProcessor.process(
                        image.bytes(),
                        brand,
                        targetColorCount,
                        mirror,
                        gridSize,
                        similarityThreshold
                );
                AiPatternQualityScorer.ScoredMetrics scored = qualityScorer.score(
                        processed.mappedPixelData(),
                        processed.colorCount(),
                        targetColorCount,
                        "REFINED".equals(variant.name())
                );
                candidates.add(new AiPatternCandidate(
                        variant.name(),
                        variant.url(),
                        variant.key(),
                        gridSize,
                        processed,
                        scored.metrics(),
                        scored.score()
                ));
            } catch (Exception exc) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("variant", variant.name());
                error.put("message", exc.getMessage());
                candidateErrors.add(error);
            }
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException("AI pattern pipeline failed: no valid image candidate");
        }

        AiPatternCandidate selected = candidates.stream()
                .max((a, b) -> Double.compare(a.score(), b.score()))
                .orElseThrow();
        return new AiPatternPipelineResult(selected, candidates, buildProcessMeta(task, selected, candidates, candidateErrors));
    }

    private ImageStorageService.StoredImage readVariant(ImageVariant variant) {
        if (StringUtils.hasText(variant.key())) {
            return imageStorageService.readKey(variant.key());
        }
        return imageStorageService.readPublicUrl(variant.url());
    }

    private List<ImageVariant> resolveVariants(AiGenerateTask task) {
        List<ImageVariant> variants = new ArrayList<>();
        addVariant(variants, "REFINED", task.getAiImageUrl(), task.getAiImageKey());
        addVariant(variants, "RAW", task.getRawAiImageUrl(), task.getRawAiImageKey());
        return variants;
    }

    private void addVariant(List<ImageVariant> variants, String name, String url, String key) {
        if (!StringUtils.hasText(url) && !StringUtils.hasText(key)) {
            return;
        }
        boolean duplicate = variants.stream().anyMatch(existing ->
                StringUtils.hasText(key)
                        ? key.equals(existing.key())
                        : StringUtils.hasText(url) && url.equals(existing.url()));
        if (!duplicate) {
            variants.add(new ImageVariant(name, url, key));
        }
    }

    private int resolveGridSize(AiGenerateTask task) {
        if (task.getFinalGridWidth() != null) {
            return task.getFinalGridWidth();
        }
        if (task.getFinalGridHeight() != null) {
            return task.getFinalGridHeight();
        }
        return "small".equalsIgnoreCase(task.getSizeMode()) ? 32 : 48;
    }

    private int resolveSimilarityThreshold(AiGenerateTask task) {
        return switch (String.valueOf(task.getSizeMode()).toLowerCase()) {
            case "simple" -> 18;
            case "clean" -> 10;
            default -> 0;
        };
    }

    private String buildProcessMeta(AiGenerateTask task,
                                    AiPatternCandidate selected,
                                    List<AiPatternCandidate> candidates,
                                    List<Map<String, Object>> candidateErrors) {
        try {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("version", 1);
            meta.put("selectedImageVariant", selected.variant());
            meta.put("selectedScore", selected.score());
            meta.put("gridSize", selected.gridSize());
            meta.put("perfectPixelStatus", task.getPerfectPixelStatus());
            meta.put("perfectPixelError", task.getPerfectPixelError());
            meta.put("candidates", candidates.stream().map(this::candidateMeta).toList());
            if (!candidateErrors.isEmpty()) {
                meta.put("candidateErrors", candidateErrors);
            }
            return objectMapper.writeValueAsString(meta);
        } catch (Exception exc) {
            return "{\"version\":1,\"error\":\"failed to serialize process meta\"}";
        }
    }

    private Map<String, Object> candidateMeta(AiPatternCandidate candidate) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("variant", candidate.variant());
        item.put("score", candidate.score());
        item.put("gridSize", candidate.gridSize());
        item.put("colorCount", candidate.colorCount());
        item.put("metrics", candidate.metrics());
        return item;
    }

    private record ImageVariant(String name, String url, String key) {}
}
