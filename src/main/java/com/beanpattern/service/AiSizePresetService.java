package com.beanpattern.service;

import com.beanpattern.entity.AiSizePreset;
import com.beanpattern.mapper.AiSizePresetMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

@Service
public class AiSizePresetService {

    public static final int MIN_ALLOWED_GRID = 16;
    public static final int MAX_ALLOWED_GRID = 128;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AiSizePresetMapper mapper;

    public AiSizePresetService(AiSizePresetMapper mapper) {
        this.mapper = mapper;
    }

    public List<AiSizePreset> getEnabledPresets() {
        return mapper.findEnabled();
    }

    public List<AiSizePreset> getAllPresets() {
        return mapper.findAll();
    }

    public AiSizePreset resolveForGenerate(String sizePreset, String legacySizeMode) {
        String key = normalizePresetKey(sizePreset);
        if (!StringUtils.hasText(key)) {
            key = "small".equalsIgnoreCase(String.valueOf(legacySizeMode)) ? "small" : "standard";
        }

        AiSizePreset preset = mapper.findEnabledByKey(key);
        if (preset != null) {
            return preset;
        }

        AiSizePreset fallback = mapper.findEnabledByKey("standard");
        if (fallback != null) {
            return fallback;
        }

        List<AiSizePreset> enabled = mapper.findEnabled();
        if (!enabled.isEmpty()) {
            return enabled.get(0);
        }

        throw new IllegalStateException("No enabled AI size preset");
    }

    public void save(AiSizePreset preset) {
        if (preset != null && preset.getId() != null) {
            AiSizePreset existing = mapper.findById(preset.getId());
            if (existing == null) {
                throw new IllegalArgumentException("尺寸档位不存在");
            }
            preset.setPresetKey(existing.getPresetKey());
        }
        normalizeAndValidate(preset);
        if (mapper.countByKey(preset.getPresetKey(), preset.getId()) > 0) {
            throw new IllegalArgumentException("尺寸档位标识已存在");
        }
        if (Integer.valueOf(0).equals(preset.getEnabled()) && mapper.countEnabledExcept(preset.getId()) == 0) {
            throw new IllegalArgumentException("至少保留一个启用的尺寸档位");
        }

        if (preset.getId() == null) {
            mapper.insert(preset);
        } else {
            mapper.update(preset);
        }
    }

    public void delete(Long id) {
        AiSizePreset existing = mapper.findById(id);
        if (existing == null) {
            return;
        }
        if (Integer.valueOf(1).equals(existing.getEnabled()) && mapper.countEnabledExcept(id) == 0) {
            throw new IllegalArgumentException("至少保留一个启用的尺寸档位");
        }
        mapper.deleteById(id);
    }

    public List<Integer> parseCandidateGrids(AiSizePreset preset) {
        return parseCandidateGrids(preset.getCandidateGrids());
    }

    public static List<Integer> parseCandidateGrids(String candidateGrids) {
        if (!StringUtils.hasText(candidateGrids)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(candidateGrids, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("候选格数必须是 JSON 数组");
        }
    }

    private void normalizeAndValidate(AiSizePreset preset) {
        if (preset == null) {
            throw new IllegalArgumentException("尺寸档位不能为空");
        }
        String key = normalizePresetKey(preset.getPresetKey());
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("请输入尺寸档位标识");
        }
        if (!key.matches("[a-z][a-z0-9_-]{1,31}")) {
            throw new IllegalArgumentException("尺寸档位标识只能使用小写字母、数字、中横线和下划线");
        }
        if (!StringUtils.hasText(preset.getName())) {
            throw new IllegalArgumentException("请输入尺寸档位名称");
        }
        if (preset.getGridMin() == null || preset.getGridMax() == null) {
            throw new IllegalArgumentException("请输入尺寸范围");
        }
        if (preset.getGridMin() < MIN_ALLOWED_GRID || preset.getGridMax() > MAX_ALLOWED_GRID) {
            throw new IllegalArgumentException("尺寸范围必须在 " + MIN_ALLOWED_GRID + "-" + MAX_ALLOWED_GRID + " 之间");
        }
        if (preset.getGridMin() > preset.getGridMax()) {
            throw new IllegalArgumentException("最小格数不能大于最大格数");
        }

        TreeSet<Integer> normalized = new TreeSet<>(parseCandidateGrids(preset.getCandidateGrids()));
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("候选格数不能为空");
        }
        for (Integer value : normalized) {
            if (value == null || value < preset.getGridMin() || value > preset.getGridMax()) {
                throw new IllegalArgumentException("候选格数必须在尺寸范围内");
            }
        }
        if (preset.getDefaultGrid() == null || !normalized.contains(preset.getDefaultGrid())) {
            throw new IllegalArgumentException("默认格数必须在候选格数内");
        }

        preset.setPresetKey(key);
        preset.setName(preset.getName().trim());
        preset.setDescription(StringUtils.hasText(preset.getDescription()) ? preset.getDescription().trim() : "");
        preset.setCandidateGrids(toJson(new ArrayList<>(normalized)));
        preset.setSortOrder(preset.getSortOrder() == null ? 0 : preset.getSortOrder());
        preset.setRecommended(preset.getRecommended() != null && preset.getRecommended() != 0 ? 1 : 0);
        preset.setEnabled(preset.getEnabled() == null || preset.getEnabled() != 0 ? 1 : 0);
    }

    private static String normalizePresetKey(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String toJson(List<Integer> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            throw new IllegalArgumentException("候选格数序列化失败");
        }
    }
}
