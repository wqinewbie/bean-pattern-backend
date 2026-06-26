package com.beanpattern.service;

import com.beanpattern.entity.AiGenerateTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        int similarityThreshold = resolveSimilarityThreshold(task);
        List<Integer> gridSizes = resolveGridSizes(task);

        List<ImageVariant> variants = resolveVariants(task);
        List<AiPatternCandidate> candidates = new ArrayList<>();
        List<Map<String, Object>> candidateErrors = new ArrayList<>();

        for (Integer gridSize : gridSizes) {
            for (ImageVariant variant : variants) {
                tryProcessCandidate(
                        variant,
                        gridSize,
                        brand,
                        targetColorCount,
                        mirror,
                        similarityThreshold,
                        candidates,
                        candidateErrors
                );
            }
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException(
                    "AI pattern pipeline failed: no valid image candidate; errors=" + candidateErrors);
        }

        AiPatternCandidate selected = candidates.stream()
                .max((a, b) -> Double.compare(a.score(), b.score()))
                .orElseThrow();
        return new AiPatternPipelineResult(selected, candidates, buildProcessMeta(task, selected, candidates, candidateErrors));
    }

    private void tryProcessCandidate(ImageVariant variant,
                                     int gridSize,
                                     String brand,
                                     int targetColorCount,
                                     boolean mirror,
                                     int similarityThreshold,
                                     List<AiPatternCandidate> candidates,
                                     List<Map<String, Object>> candidateErrors) {
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
            double score = scored.score() - variantResolutionPenalty(variant, image.bytes());
            candidates.add(new AiPatternCandidate(
                    variant.name(),
                    variant.url(),
                    variant.key(),
                    gridSize,
                    processed,
                    scored.metrics(),
                    score
            ));
        } catch (Exception exc) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("variant", variant.name());
            error.put("gridSize", gridSize);
            error.put("message", exc.getMessage());
            candidateErrors.add(error);
        }
    }

    private double variantResolutionPenalty(ImageVariant variant, byte[] bytes) {
        if (!"REFINED".equals(variant.name()) || bytes == null || bytes.length == 0) {
            return 0.0;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return 0.0;
            }
            int maxDim = Math.max(image.getWidth(), image.getHeight());
            if (maxDim < 384) {
                return 14.0;
            }
            if (maxDim < 512) {
                return 8.0;
            }
            return 0.0;
        } catch (Exception exc) {
            return 0.0;
        }
    }

    private ImageStorageService.StoredImage readVariant(ImageVariant variant) {
        if (StringUtils.hasText(variant.key())) {
            try {
                return imageStorageService.readKey(variant.key());
            } catch (Exception exc) {
                if (!StringUtils.hasText(variant.url())) {
                    throw exc;
                }
            }
        }
        return imageStorageService.readPublicUrl(variant.url());
    }

    private List<ImageVariant> resolveVariants(AiGenerateTask task) {
        List<ImageVariant> variants = new ArrayList<>();
        addVariant(variants, "RAW", task.getRawAiImageUrl(), task.getRawAiImageKey());
        addVariant(variants, "REFINED", task.getAiImageUrl(), task.getAiImageKey());
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

    private List<Integer> resolveGridSizes(AiGenerateTask task) {
        Set<Integer> ordered = new LinkedHashSet<>();
        Integer defaultGrid = task.getDefaultGrid();
        if (isUsableGrid(task, defaultGrid)) {
            ordered.add(defaultGrid);
        }

        int target = defaultGrid != null ? defaultGrid : fallbackGrid(task);
        List<Integer> candidates = new ArrayList<>(AiSizePresetService.parseCandidateGrids(task.getCandidateGrids()));
        candidates.removeIf(value -> !isUsableGrid(task, value));
        candidates.sort(Comparator.comparingInt(value -> Math.abs(value - target)));
        ordered.addAll(candidates);

        if (isUsableGrid(task, task.getFinalGridWidth())) {
            ordered.add(task.getFinalGridWidth());
        }
        if (isUsableGrid(task, task.getFinalGridHeight())) {
            ordered.add(task.getFinalGridHeight());
        }

        ordered.add(fallbackGrid(task));
        return new ArrayList<>(ordered);
    }

    private boolean isUsableGrid(AiGenerateTask task, Integer value) {
        if (value == null || value <= 0) {
            return false;
        }
        Integer min = task.getGridMin();
        Integer max = task.getGridMax();
        if (min != null && value < min) {
            return false;
        }
        return max == null || value <= max;
    }

    private int fallbackGrid(AiGenerateTask task) {
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
            meta.put("attemptedGridSizes", resolveGridSizes(task));
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
