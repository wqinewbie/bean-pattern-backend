package com.beanpattern.service;

import com.beanpattern.service.BeadColorService.BeadColor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PatternExperimentService {

    private static final int DEFAULT_EXPERIMENT_COLOR_LIMIT = 24;

    private final BeadColorService beadColorService;
    private final AiImageProcessor aiImageProcessor;

    public PatternExperimentService(BeadColorService beadColorService, AiImageProcessor aiImageProcessor) {
        this.beadColorService = beadColorService;
        this.aiImageProcessor = aiImageProcessor;
    }

    public CompareResult compare(byte[] imageBytes,
                                 String brand,
                                 int colorCount,
                                 boolean mirror,
                                 int gridSize,
                                 int similarityThreshold) {
        PatternResult baseline = baseline(imageBytes, brand, colorCount, mirror, gridSize, similarityThreshold);
        AiImageProcessor.ProcessedResult optimized = aiImageProcessor.process(
                imageBytes,
                brand,
                colorCount,
                mirror,
                gridSize,
                similarityThreshold
        );
        PatternResult clean = fromProcessed("optimized", optimized);
        return new CompareResult(baseline, clean);
    }

    public PatternResult optimized(byte[] imageBytes,
                                   String brand,
                                   int colorCount,
                                   boolean mirror,
                                   int gridSize,
                                   int similarityThreshold) {
        return fromProcessed("optimized", aiImageProcessor.process(
                imageBytes,
                brand,
                colorCount,
                mirror,
                gridSize,
                similarityThreshold
        ));
    }

    private PatternResult baseline(byte[] imageBytes,
                                   String brand,
                                   int colorCount,
                                   boolean mirror,
                                   int gridSize,
                                   int similarityThreshold) {
        int[][][] rgbGrid = sampleAverage(imageBytes, gridSize);
        BeadColor[][] matched = beadColorService.matchGrid(rgbGrid, brand, colorCount, "standard");
        MappedResult mapped = convertToMappedPixelData(matched);
        if (similarityThreshold > 0) {
            mapped = mergeSimilarColors(mapped, similarityThreshold);
        }
        if (mirror) {
            mapped = mirrorGridRows(mapped);
        }
        return fromMapped("baseline", mapped);
    }

    private int[][][] sampleAverage(byte[] imageBytes, int gridSize) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IllegalArgumentException("unable to read image bytes");
            }
            int w = image.getWidth();
            int h = image.getHeight();
            double aspect = (double) w / h;
            int gw = aspect >= 1 ? gridSize : Math.max(1, (int) Math.round(gridSize * aspect));
            int gh = aspect >= 1 ? Math.max(1, (int) Math.round(gridSize / aspect)) : gridSize;
            double cellW = (double) w / gw;
            double cellH = (double) h / gh;
            int[][][] grid = new int[gh][gw][3];
            for (int gy = 0; gy < gh; gy++) {
                int y0 = (int) Math.floor(gy * cellH);
                int y1 = Math.min((int) Math.ceil((gy + 1) * cellH), h);
                for (int gx = 0; gx < gw; gx++) {
                    int x0 = (int) Math.floor(gx * cellW);
                    int x1 = Math.min((int) Math.ceil((gx + 1) * cellW), w);
                    grid[gy][gx] = averageCell(image, x0, y0, x1, y1);
                }
            }
            return grid;
        } catch (Exception exc) {
            throw new IllegalArgumentException("sample image failed: " + exc.getMessage(), exc);
        }
    }

    private int[] averageCell(BufferedImage image, int x0, int y0, int x1, int y1) {
        long r = 0;
        long g = 0;
        long b = 0;
        int count = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int rgb = image.getRGB(x, y);
                if (((rgb >> 24) & 0xff) < 128) {
                    continue;
                }
                r += (rgb >> 16) & 0xff;
                g += (rgb >> 8) & 0xff;
                b += rgb & 0xff;
                count++;
            }
        }
        if (count == 0) {
            return new int[]{255, 255, 255};
        }
        return new int[]{
                (int) Math.round((double) r / count),
                (int) Math.round((double) g / count),
                (int) Math.round((double) b / count)
        };
    }

    private MappedResult convertToMappedPixelData(BeadColor[][] matchedGrid) {
        List<List<Map<String, Object>>> mappedPixelData = new ArrayList<>();
        for (BeadColor[] row : matchedGrid) {
            List<Map<String, Object>> mappedRow = new ArrayList<>();
            for (BeadColor cell : row) {
                mappedRow.add(colorCell(cell.id(), cell.name(), cell.r(), cell.g(), cell.b()));
            }
            mappedPixelData.add(mappedRow);
        }
        List<Map<String, Object>> colorStats = calcColorStats(mappedPixelData);
        return new MappedResult(mappedPixelData, colorStats, buildGridData(mappedPixelData), buildColorPalette(colorStats));
    }

    private MappedResult mergeSimilarColors(MappedResult mapped, int threshold) {
        List<List<Map<String, Object>>> data = deepCopyGrid(mapped.mappedPixelData);
        int th = Math.max(0, Math.min(100, threshold));
        if (data.isEmpty() || th <= 0) {
            return mapped;
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Map<String, Object>> colorMap = new LinkedHashMap<>();
        for (List<Map<String, Object>> row : data) {
            for (Map<String, Object> cell : row) {
                String id = String.valueOf(cell.get("id"));
                counts.merge(id, 1, Integer::sum);
                colorMap.putIfAbsent(id, cloneColorCell(cell));
            }
        }

        List<String> ids = new ArrayList<>(counts.keySet());
        ids.sort((a, b) -> Integer.compare(counts.get(b), counts.get(a)));
        Set<String> replaced = new HashSet<>();

        for (int i = 0; i < ids.size(); i++) {
            String aId = ids.get(i);
            if (replaced.contains(aId)) {
                continue;
            }
            Map<String, Object> a = colorMap.get(aId);
            for (int j = i + 1; j < ids.size(); j++) {
                String bId = ids.get(j);
                if (replaced.contains(bId)) {
                    continue;
                }
                Map<String, Object> b = colorMap.get(bId);
                if (colorDistance(a, b) < th) {
                    replaced.add(bId);
                    replaceColor(data, bId, a);
                }
            }
        }

        List<Map<String, Object>> colorStats = calcColorStats(data);
        return new MappedResult(data, colorStats, buildGridData(data), buildColorPalette(colorStats));
    }

    private void replaceColor(List<List<Map<String, Object>>> data, String fromId, Map<String, Object> toColor) {
        for (List<Map<String, Object>> row : data) {
            for (int x = 0; x < row.size(); x++) {
                if (fromId.equals(String.valueOf(row.get(x).get("id")))) {
                    row.set(x, cloneColorCell(toColor));
                }
            }
        }
    }

    private MappedResult mirrorGridRows(MappedResult mapped) {
        List<List<Map<String, Object>>> mirrored = new ArrayList<>(mapped.mappedPixelData.size());
        for (List<Map<String, Object>> row : mapped.mappedPixelData) {
            List<Map<String, Object>> reversed = new ArrayList<>(row);
            java.util.Collections.reverse(reversed);
            mirrored.add(reversed);
        }
        List<Map<String, Object>> colorStats = calcColorStats(mirrored);
        return new MappedResult(mirrored, colorStats, buildGridData(mirrored), buildColorPalette(colorStats));
    }

    private PatternResult fromProcessed(String name, AiImageProcessor.ProcessedResult processed) {
        return new PatternResult(
                name,
                processed.mappedPixelData(),
                processed.gridData(),
                processed.colorPalette(),
                processed.colorList(),
                processed.totalBeads(),
                metrics(processed.mappedPixelData(), processed.colorCount())
        );
    }

    private PatternResult fromMapped(String name, MappedResult mapped) {
        int totalBeads = mapped.colorStats.stream()
                .mapToInt(item -> ((Number) item.get("count")).intValue())
                .sum();
        return new PatternResult(
                name,
                mapped.mappedPixelData,
                mapped.gridData,
                mapped.colorPalette,
                mapped.colorStats,
                totalBeads,
                metrics(mapped.mappedPixelData, mapped.colorStats.size())
        );
    }

    private Map<String, Object> metrics(List<List<Map<String, Object>>> data, int colorCount) {
        int rows = data == null ? 0 : data.size();
        int cols = rows == 0 || data.get(0) == null ? 0 : data.get(0).size();
        int total = 0;
        int isolated = 0;
        int sameEdges = 0;
        int totalEdges = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < data.get(y).size(); x++) {
                Map<String, Object> cell = data.get(y).get(x);
                total++;
                int sameNeighbors = 0;
                for (int[] direction : DIRECTIONS_4) {
                    Map<String, Object> other = cellAt(data, y + direction[1], x + direction[0]);
                    if (other != null && sameColor(cell, other)) {
                        sameNeighbors++;
                    }
                }
                if (sameNeighbors <= 1) {
                    isolated++;
                }
                Map<String, Object> right = cellAt(data, y, x + 1);
                if (right != null) {
                    totalEdges++;
                    if (sameColor(cell, right)) {
                        sameEdges++;
                    }
                }
                Map<String, Object> down = cellAt(data, y + 1, x);
                if (down != null) {
                    totalEdges++;
                    if (sameColor(cell, down)) {
                        sameEdges++;
                    }
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("cols", cols);
        result.put("colorCount", colorCount);
        result.put("isolatedRatio", round(ratio(isolated, total)));
        result.put("sameNeighborRatio", round(ratio(sameEdges, totalEdges)));
        return result;
    }

    private static final int[][] DIRECTIONS_4 = new int[][]{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};

    private Map<String, Object> cellAt(List<List<Map<String, Object>>> data, int y, int x) {
        if (y < 0 || y >= data.size()) {
            return null;
        }
        List<Map<String, Object>> row = data.get(y);
        if (x < 0 || x >= row.size()) {
            return null;
        }
        return row.get(x);
    }

    private boolean sameColor(Map<String, Object> a, Map<String, Object> b) {
        return String.valueOf(a.get("id")).equals(String.valueOf(b.get("id")));
    }

    private List<Map<String, Object>> calcColorStats(List<List<Map<String, Object>>> mappedPixelData) {
        Map<String, Map<String, Object>> statsMap = new LinkedHashMap<>();
        for (List<Map<String, Object>> row : mappedPixelData) {
            for (Map<String, Object> cell : row) {
                String id = String.valueOf(cell.get("id"));
                statsMap.putIfAbsent(id, cloneColorCell(cell));
                Map<String, Object> stat = statsMap.get(id);
                stat.put("count", ((Number) stat.getOrDefault("count", 0)).intValue() + 1);
            }
        }
        List<Map<String, Object>> stats = new ArrayList<>(statsMap.values());
        stats.sort(Comparator.comparingInt(item -> -((Number) item.get("count")).intValue()));
        return stats;
    }

    private List<List<String>> buildGridData(List<List<Map<String, Object>>> mappedPixelData) {
        List<List<String>> gridData = new ArrayList<>();
        for (List<Map<String, Object>> row : mappedPixelData) {
            List<String> idRow = new ArrayList<>();
            for (Map<String, Object> cell : row) {
                idRow.add(String.valueOf(cell.get("id")));
            }
            gridData.add(idRow);
        }
        return gridData;
    }

    private List<Map<String, Object>> buildColorPalette(List<Map<String, Object>> colorStats) {
        List<Map<String, Object>> palette = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Map<String, Object> stat : colorStats) {
            String id = String.valueOf(stat.get("id"));
            if (seen.add(id)) {
                palette.add(cloneColorCell(stat));
            }
        }
        return palette;
    }

    private Map<String, Object> colorCell(String id, String name, int r, int g, int b) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("hex", rgbToHex(r, g, b));
        item.put("r", r);
        item.put("g", g);
        item.put("b", b);
        item.put("isExternal", false);
        return item;
    }

    private Map<String, Object> cloneColorCell(Map<String, Object> source) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", source.get("id"));
        item.put("name", source.get("name"));
        item.put("hex", source.get("hex"));
        item.put("r", source.get("r"));
        item.put("g", source.get("g"));
        item.put("b", source.get("b"));
        item.put("isExternal", false);
        if (source.containsKey("count")) {
            item.put("count", source.get("count"));
        }
        return item;
    }

    private double colorDistance(Map<String, Object> a, Map<String, Object> b) {
        int dr = ((Number) a.get("r")).intValue() - ((Number) b.get("r")).intValue();
        int dg = ((Number) a.get("g")).intValue() - ((Number) b.get("g")).intValue();
        int db = ((Number) a.get("b")).intValue() - ((Number) b.get("b")).intValue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    private List<List<Map<String, Object>>> deepCopyGrid(List<List<Map<String, Object>>> grid) {
        List<List<Map<String, Object>>> copy = new ArrayList<>(grid.size());
        for (List<Map<String, Object>> row : grid) {
            List<Map<String, Object>> newRow = new ArrayList<>(row.size());
            for (Map<String, Object> cell : row) {
                newRow.add(cloneColorCell(cell));
            }
            copy.add(newRow);
        }
        return copy;
    }

    private double ratio(int value, int total) {
        return total <= 0 ? 0.0 : (double) value / total;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private static String rgbToHex(int r, int g, int b) {
        return String.format("#%02x%02x%02x",
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b)));
    }

    private record MappedResult(
            List<List<Map<String, Object>>> mappedPixelData,
            List<Map<String, Object>> colorStats,
            List<List<String>> gridData,
            List<Map<String, Object>> colorPalette
    ) {}

    public record CompareResult(PatternResult baseline, PatternResult optimized) {}

    public record PatternResult(
            String mode,
            List<List<Map<String, Object>>> mappedPixelData,
            List<List<String>> gridData,
            List<Map<String, Object>> colorPalette,
            List<Map<String, Object>> colorList,
            int totalBeads,
            Map<String, Object> metrics
    ) {}
}
