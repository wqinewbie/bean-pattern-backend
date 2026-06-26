package com.beanpattern.service;

import com.beanpattern.service.BeadColorService.BeadColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.*;

/**
 * AI 生成图片后处理服务
 * 将前端 color-matcher.js 中的采样、匹配、合并、镜像逻辑移到后端
 */
@Service
public class AiImageProcessor {

    private static final Logger log = LoggerFactory.getLogger(AiImageProcessor.class);

    @Autowired
    private BeadColorService beadColorService;

    /**
     * 处理 AI 生成图片，产出完整的像素数据
     *
     * @param imageUrl            AI 生成的图片 URL
     * @param brand               品牌
     * @param colorCount          色数（0 = 不限）
     * @param mirror              是否镜像
     * @param gridSize            目标网格尺寸
     * @param similarityThreshold 相近色合并阈值（0 = 不合并）
     */
    public ProcessedResult process(String imageUrl, String brand, int colorCount,
                                    boolean mirror, int gridSize, int similarityThreshold) {
        return processGrid(sampleImage(imageUrl, gridSize), brand, colorCount, mirror, similarityThreshold);
    }

    public ProcessedResult process(byte[] imageBytes, String brand, int colorCount,
                                    boolean mirror, int gridSize, int similarityThreshold) {
        return processGrid(sampleImageBytes(imageBytes, gridSize), brand, colorCount, mirror, similarityThreshold);
    }

    private ProcessedResult processGrid(int[][][] rgbGrid, String brand, int colorCount,
                                        boolean mirror, int similarityThreshold) {
        BeadColor[][] matchedGrid = beadColorService.matchGrid(rgbGrid, brand, colorCount, "standard");
        MappedResult mapped = convertToMappedPixelData(matchedGrid);

        if (similarityThreshold > 0) {
            mapped = mergeSimilarColors(mapped, similarityThreshold);
        }

        mapped = harmonizePlainBackground(mapped);
        mapped = harmonizeFaceSkinTones(mapped);

        if (mirror) {
            mapped = mirrorGridRows(mapped);
        }

        List<Map<String, Object>> colorStats = calcColorStats(mapped.mappedPixelData);
        int totalBeads = colorStats.stream().mapToInt(c -> ((Number) c.get("count")).intValue()).sum();
        List<List<String>> gridData = buildGridData(mapped.mappedPixelData);
        List<Map<String, Object>> colorPalette = buildColorPalette(colorStats);

        return new ProcessedResult(
                mapped.mappedPixelData,
                colorStats,
                gridData,
                colorPalette,
                totalBeads,
                colorStats.size()
        );
    }

    /**
     * 下载图片并采样为 RGB 网格
     */
    private int[][][] sampleImage(String imageUrl, int gridSize) {
        try {
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            if (image == null) {
                throw new RuntimeException("无法读取图片: " + imageUrl);
            }
            return sampleBufferedImage(image, gridSize);
        } catch (Exception e) {
            log.error("采样图片失败: {}", imageUrl, e);
            throw new RuntimeException("采样图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将 BeadColor 网格转换为 mappedPixelData 格式
     */
    private int[][][] sampleImageBytes(byte[] imageBytes, int gridSize) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new RuntimeException("unable to read image bytes");
            }
            return sampleBufferedImage(image, gridSize);
        } catch (Exception e) {
            log.error("sample image bytes failed", e);
            throw new RuntimeException("sample image failed: " + e.getMessage(), e);
        }
    }

    private int[][][] sampleBufferedImage(BufferedImage image, int gridSize) {
        int w = image.getWidth();
        int h = image.getHeight();

        if (isAlreadyPixelGrid(w, h, gridSize)) {
            return readExactPixelGrid(image);
        }

        double aspect = (double) w / h;
        int gw, gh;
        if (aspect >= 1) {
            gw = gridSize;
            gh = Math.max(1, (int) Math.round(gridSize / aspect));
        } else {
            gh = gridSize;
            gw = Math.max(1, (int) Math.round(gridSize * aspect));
        }

        double cellW = (double) w / gw;
        double cellH = (double) h / gh;
        int[][][] rgbGrid = new int[gh][gw][3];

        for (int gy = 0; gy < gh; gy++) {
            int y0 = (int) Math.floor(gy * cellH);
            int y1 = Math.min((int) Math.ceil((gy + 1) * cellH), h);

            for (int gx = 0; gx < gw; gx++) {
                int x0 = (int) Math.floor(gx * cellW);
                int x1 = Math.min((int) Math.ceil((gx + 1) * cellW), w);
                rgbGrid[gy][gx] = sampleCellAverage(image, x0, y0, x1, y1);
            }
        }

        return rgbGrid;
    }

    private int[] sampleCellAverage(BufferedImage image, int x0, int y0, int x1, int y1) {
        long r = 0;
        long g = 0;
        long b = 0;
        int count = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int rgb = image.getRGB(x, y);
                if (((rgb >> 24) & 0xff) < 128) continue;
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

    private boolean isAlreadyPixelGrid(int width, int height, int gridSize) {
        int maxDim = Math.max(width, height);
        int minDim = Math.min(width, height);
        return minDim >= 8 && maxDim <= 128 && Math.abs(maxDim - gridSize) <= 16;
    }

    private int[][][] readExactPixelGrid(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[][][] rgbGrid = new int[h][w][3];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = image.getRGB(x, y);
                rgbGrid[y][x] = ((rgb >> 24) & 0xff) < 128
                        ? new int[]{255, 255, 255}
                        : new int[]{(rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff};
            }
        }
        return rgbGrid;
    }

    private int[] sampleCellMedian(BufferedImage image, int x0, int y0, int x1, int y1) {
        int capacity = Math.max(1, (x1 - x0) * (y1 - y0));
        int[] rs = new int[capacity];
        int[] gs = new int[capacity];
        int[] bs = new int[capacity];
        int count = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int rgb = image.getRGB(x, y);
                if (((rgb >> 24) & 0xff) < 128) continue;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                rs[count] = r;
                gs[count] = g;
                bs[count] = b;
                count++;
            }
        }

        if (count == 0) {
            return new int[]{255, 255, 255};
        }

        Arrays.sort(rs, 0, count);
        Arrays.sort(gs, 0, count);
        Arrays.sort(bs, 0, count);
        int mid = count / 2;
        return new int[]{rs[mid], gs[mid], bs[mid]};
    }

    private MappedResult convertToMappedPixelData(BeadColor[][] matchedGrid) {
        List<List<Map<String, Object>>> mappedPixelData = new ArrayList<>();
        Map<String, Map<String, Object>> colorStatsMap = new LinkedHashMap<>();

        for (BeadColor[] row : matchedGrid) {
            List<Map<String, Object>> mappedRow = new ArrayList<>();
            for (BeadColor cell : row) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", cell.id());
                item.put("name", cell.name());
                item.put("hex", rgbToHex(cell.r(), cell.g(), cell.b()));
                item.put("r", cell.r());
                item.put("g", cell.g());
                item.put("b", cell.b());
                item.put("isExternal", false);
                mappedRow.add(item);

                if (!colorStatsMap.containsKey(cell.id())) {
                    Map<String, Object> stat = new LinkedHashMap<>();
                    stat.put("id", cell.id());
                    stat.put("name", cell.name());
                    stat.put("hex", rgbToHex(cell.r(), cell.g(), cell.b()));
                    stat.put("r", cell.r());
                    stat.put("g", cell.g());
                    stat.put("b", cell.b());
                    stat.put("count", 0);
                    colorStatsMap.put(cell.id(), stat);
                }
                Map<String, Object> stat = colorStatsMap.get(cell.id());
                stat.put("count", ((Number) stat.get("count")).intValue() + 1);
            }
            mappedPixelData.add(mappedRow);
        }

        List<Map<String, Object>> colorStats = new ArrayList<>(colorStatsMap.values());
        colorStats.sort((a, b) -> Integer.compare(
                ((Number) b.get("count")).intValue(),
                ((Number) a.get("count")).intValue()));

        List<List<String>> gridData = buildGridData(mappedPixelData);
        List<Map<String, Object>> colorPalette = buildColorPalette(colorStats);

        return new MappedResult(mappedPixelData, colorStats, gridData, colorPalette);
    }

    /**
     * 合并相近色（与前端 mergeSimilarColors 算法一致）
     */
    private MappedResult mergeSimilarColors(MappedResult mapped, int threshold) {
        List<List<Map<String, Object>>> data = deepCopyGrid(mapped.mappedPixelData);
        int th = Math.max(0, Math.min(100, threshold));
        if (data.isEmpty() || th <= 0) {
            return mapped;
        }

        // 统计颜色频率
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Map<String, Object>> colorMap = new LinkedHashMap<>();
                    for (List<Map<String, Object>> row : data) {
            for (Map<String, Object> cell : row) {
                if (Boolean.TRUE.equals(cell.get("isExternal"))) continue;
                String id = (String) cell.get("id");
                if (id == null) continue;
                counts.merge(id, 1, Integer::sum);
                if (!colorMap.containsKey(id)) {
                    Map<String, Object> c = new LinkedHashMap<>();
                    c.put("id", cell.get("id"));
                    c.put("name", cell.get("name"));
                    c.put("hex", cell.get("hex"));
                    c.put("r", cell.get("r"));
                    c.put("g", cell.get("g"));
                    c.put("b", cell.get("b"));
                    colorMap.put(id, c);
                }
            }
        }

        // 按频率降序排列
        List<String> ids = new ArrayList<>(counts.keySet());
        ids.sort((a, b) -> Integer.compare(counts.get(b), counts.get(a)));

        Set<String> replaced = new HashSet<>();

        for (int i = 0; i < ids.size(); i++) {
            String aId = ids.get(i);
            if (replaced.contains(aId)) continue;
            Map<String, Object> colorA = colorMap.get(aId);
            if (colorA == null) continue;

            for (int j = i + 1; j < ids.size(); j++) {
                String bId = ids.get(j);
                if (replaced.contains(bId)) continue;
                Map<String, Object> colorB = colorMap.get(bId);
                if (colorB == null) continue;

                if (colorDistance(colorA, colorB) < th) {
                    replaced.add(bId);
                    // 替换所有 bId 为 aId 的颜色
        for (List<Map<String, Object>> row : data) {
                        for (int x = 0; x < row.size(); x++) {
                            Map<String, Object> cell = row.get(x);
                            if (bId.equals(cell.get("id"))) {
                                Map<String, Object> replacement = new LinkedHashMap<>();
                                replacement.put("id", colorA.get("id"));
                                replacement.put("name", colorA.get("name"));
                                replacement.put("hex", colorA.get("hex"));
                                replacement.put("r", colorA.get("r"));
                                replacement.put("g", colorA.get("g"));
                                replacement.put("b", colorA.get("b"));
                                replacement.put("isExternal", false);
                                row.set(x, replacement);
                            }
                        }
                    }
                }
            }
        }

        List<Map<String, Object>> newColorStats = calcColorStats(data);
        List<List<String>> gridData = buildGridData(data);
        List<Map<String, Object>> colorPalette = buildColorPalette(newColorStats);

        return new MappedResult(data, newColorStats, gridData, colorPalette);
    }

    private MappedResult limitColorCount(MappedResult mapped, int maxColors) {
        if (maxColors <= 0) {
            return mapped;
        }
        List<Map<String, Object>> stats = calcColorStats(mapped.mappedPixelData);
        if (stats.size() <= maxColors) {
            return mapped;
        }

        List<List<Map<String, Object>>> data = deepCopyGrid(mapped.mappedPixelData);
        Map<String, Map<String, Object>> colorMap = new LinkedHashMap<>();
        for (Map<String, Object> item : stats) {
            colorMap.put(String.valueOf(item.get("id")), item);
        }

        Set<String> kept = new LinkedHashSet<>();
        for (int i = 0; i < Math.min(maxColors, stats.size()); i++) {
            kept.add(String.valueOf(stats.get(i).get("id")));
        }

        Map<String, Map<String, Object>> replacementById = new HashMap<>();
        for (int i = maxColors; i < stats.size(); i++) {
            Map<String, Object> color = stats.get(i);
            String id = String.valueOf(color.get("id"));
            replacementById.put(id, closestColor(color, kept, colorMap));
        }

        if (replacementById.isEmpty()) {
            return mapped;
        }

        for (List<Map<String, Object>> row : data) {
            for (int x = 0; x < row.size(); x++) {
                Map<String, Object> cell = row.get(x);
                Map<String, Object> replacement = replacementById.get(String.valueOf(cell.get("id")));
                if (replacement != null) {
                    row.set(x, cloneColorCell(replacement));
                }
            }
        }

        List<Map<String, Object>> newColorStats = calcColorStats(data);
        List<List<String>> gridData = buildGridData(data);
        List<Map<String, Object>> colorPalette = buildColorPalette(newColorStats);
        return new MappedResult(data, newColorStats, gridData, colorPalette);
    }

    private Map<String, Object> closestColor(Map<String, Object> source,
                                             Set<String> targetIds,
                                             Map<String, Map<String, Object>> colorMap) {
        Map<String, Object> best = null;
        double bestDistance = Double.MAX_VALUE;
        for (String targetId : targetIds) {
            Map<String, Object> target = colorMap.get(targetId);
            if (target == null) {
                continue;
            }
            double distance = colorDistance(source, target);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = target;
            }
        }
        return best != null ? best : source;
    }

    private MappedResult cleanupIsolatedColorNoise(MappedResult mapped, FaceBounds faceBounds) {
        List<List<Map<String, Object>>> source = mapped.mappedPixelData;
        if (source.isEmpty() || source.get(0).isEmpty()) {
            return mapped;
        }

        List<List<Map<String, Object>>> data = deepCopyGrid(source);
        int rows = source.size();
        boolean changed = false;

        for (int y = 0; y < rows; y++) {
            List<Map<String, Object>> row = source.get(y);
            for (int x = 0; x < row.size(); x++) {
                Map<String, Object> cell = row.get(x);
                if (isProtectedFacialFeature(faceBounds, y, x, cell)) {
                    continue;
                }
                if (isStrongLineOrFeatureColor(cell)) {
                    continue;
                }

                Map<String, NeighborCount> neighborCounts = countNeighborColors(source, y, x);
                NeighborCount same = neighborCounts.get(String.valueOf(cell.get("id")));
                int sameCount = same == null ? 0 : same.count();
                if (sameCount > 1) {
                    continue;
                }

                NeighborCount dominant = neighborCounts.values().stream()
                        .filter(item -> !isStrongLineOrFeatureColor(item.cell()))
                        .max(Comparator.comparingInt(NeighborCount::count))
                        .orElse(null);
                if (dominant != null && dominant.count() >= 3) {
                    data.get(y).set(x, cloneColorCell(dominant.cell()));
                    changed = true;
                }
            }
        }

        if (!changed) {
            return mapped;
        }

        List<Map<String, Object>> colorStats = calcColorStats(data);
        List<List<String>> gridData = buildGridData(data);
        List<Map<String, Object>> colorPalette = buildColorPalette(colorStats);
        return new MappedResult(data, colorStats, gridData, colorPalette);
    }

    private MappedResult softenHarshBlackBlocks(MappedResult mapped, FaceBounds faceBounds) {
        List<List<Map<String, Object>>> source = mapped.mappedPixelData;
        if (source.isEmpty() || source.get(0).isEmpty()) {
            return mapped;
        }

        List<List<Map<String, Object>>> data = deepCopyGrid(source);
        boolean changed = false;

        for (int pass = 0; pass < 2; pass++) {
            List<List<Map<String, Object>>> current = deepCopyGrid(data);
            for (int y = 0; y < current.size(); y++) {
                List<Map<String, Object>> row = current.get(y);
                for (int x = 0; x < row.size(); x++) {
                    Map<String, Object> cell = row.get(x);
                    if (!isHarshBlack(cell)) {
                        continue;
                    }
                    if (isProtectedFacialFeature(faceBounds, y, x, cell)) {
                        continue;
                    }

                    int darkNeighbors = countNeighborsMatching(current, y, x, this::isHarshBlack);
                    boolean nearSkin = countNeighborsMatching(current, y, x, this::isSkinCell) >= 2;
                    if (darkNeighbors >= (nearSkin ? 3 : 4)) {
                        continue;
                    }

                    Map<String, Object> replacement = dominantSoftNeighbor(current, y, x);
                    if (replacement != null) {
                        data.get(y).set(x, cloneColorCell(replacement));
                        changed = true;
                    }
                }
            }
        }

        if (!changed) {
            return mapped;
        }

        List<Map<String, Object>> colorStats = calcColorStats(data);
        List<List<String>> gridData = buildGridData(data);
        List<Map<String, Object>> colorPalette = buildColorPalette(colorStats);
        return new MappedResult(data, colorStats, gridData, colorPalette);
    }

    private MappedResult cleanupTinyColorArtifacts(MappedResult mapped, FaceBounds faceBounds) {
        List<List<Map<String, Object>>> source = mapped.mappedPixelData;
        if (source.isEmpty() || source.get(0).isEmpty()) {
            return mapped;
        }

        List<List<Map<String, Object>>> data = deepCopyGrid(source);
        boolean[][] visited = new boolean[source.size()][];
        for (int y = 0; y < source.size(); y++) {
            visited[y] = new boolean[source.get(y).size()];
        }

        boolean changed = false;
        for (int y = 0; y < source.size(); y++) {
            for (int x = 0; x < source.get(y).size(); x++) {
                if (visited[y][x]) {
                    continue;
                }

                List<int[]> component = collectSameColorComponent(source, visited, y, x);
                if (!shouldReplaceTinyArtifact(source, component, faceBounds)) {
                    continue;
                }

                Map<String, Object> replacement = dominantBoundaryNeighbor(source, component);
                if (replacement == null) {
                    continue;
                }
                for (int[] point : component) {
                    data.get(point[0]).set(point[1], cloneColorCell(replacement));
                }
                changed = true;
            }
        }

        if (!changed) {
            return mapped;
        }

        List<Map<String, Object>> colorStats = calcColorStats(data);
        List<List<String>> gridData = buildGridData(data);
        List<Map<String, Object>> colorPalette = buildColorPalette(colorStats);
        return new MappedResult(data, colorStats, gridData, colorPalette);
    }

    private List<int[]> collectSameColorComponent(List<List<Map<String, Object>>> data,
                                                  boolean[][] visited,
                                                  int startY,
                                                  int startX) {
        List<int[]> component = new ArrayList<>();
        String id = String.valueOf(data.get(startY).get(startX).get("id"));
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startY, startX});
        visited[startY][startX] = true;

        int[][] directions = new int[][]{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            component.add(point);
            for (int[] direction : directions) {
                int ny = point[0] + direction[1];
                int nx = point[1] + direction[0];
                if (ny < 0 || ny >= data.size()) {
                    continue;
                }
                if (nx < 0 || nx >= data.get(ny).size() || visited[ny][nx]) {
                    continue;
                }
                String otherId = String.valueOf(data.get(ny).get(nx).get("id"));
                if (!id.equals(otherId)) {
                    continue;
                }
                visited[ny][nx] = true;
                queue.addLast(new int[]{ny, nx});
            }
        }
        return component;
    }

    private boolean shouldReplaceTinyArtifact(List<List<Map<String, Object>>> data,
                                              List<int[]> component,
                                              FaceBounds faceBounds) {
        if (component.isEmpty()) {
            return false;
        }
        int size = component.size();
        Map<String, Object> sample = data.get(component.get(0)[0]).get(component.get(0)[1]);
        if (component.stream().anyMatch(point ->
                isProtectedFacialFeature(faceBounds, point[0], point[1], sample))) {
            return false;
        }
        boolean strongFeature = isStrongLineOrFeatureColor(sample);
        boolean nearSkin = countBoundaryNeighborsMatching(data, component, this::isSkinCell) >= 2;
        boolean nearSameTone = countBoundaryNeighborsMatching(data, component, cell -> colorDistance(sample, cell) < 34) >= 2;
        if (strongFeature && (nearSkin || nearSameTone)) {
            return false;
        }
        if (size <= 2) {
            Map<String, Object> boundary = dominantBoundaryNeighbor(data, component);
            return boundary != null && colorDistance(sample, boundary) >= 28;
        }
        if (size > 5) {
            return false;
        }

        Map<String, Object> boundary = dominantBoundaryNeighbor(data, component);
        if (boundary == null) {
            return false;
        }

        double distance = colorDistance(sample, boundary);
        int sameToneNeighbors = countBoundaryNeighborsMatching(data, component, cell -> colorDistance(sample, cell) < 34);
        return distance >= 42 && sameToneNeighbors <= 1;
    }

    private Map<String, Object> dominantBoundaryNeighbor(List<List<Map<String, Object>>> data,
                                                         List<int[]> component) {
        Set<String> points = new HashSet<>();
        for (int[] point : component) {
            points.add(point[0] + ":" + point[1]);
        }

        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        int[][] directions = new int[][]{
                {-1, -1}, {0, -1}, {1, -1},
                {-1, 0},           {1, 0},
                {-1, 1},  {0, 1},  {1, 1}
        };
        for (int[] point : component) {
            for (int[] direction : directions) {
                int ny = point[0] + direction[1];
                int nx = point[1] + direction[0];
                if (ny < 0 || ny >= data.size() || nx < 0 || nx >= data.get(ny).size()) {
                    continue;
                }
                if (points.contains(ny + ":" + nx)) {
                    continue;
                }
                Map<String, Object> cell = data.get(ny).get(nx);
                if (Boolean.TRUE.equals(cell.get("isExternal"))) {
                    continue;
                }
                String id = String.valueOf(cell.get("id"));
                NeighborCount existing = counts.get(id);
                counts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
            }
        }
        return counts.values().stream()
                .max(Comparator.comparingInt(NeighborCount::count))
                .map(NeighborCount::cell)
                .orElse(null);
    }

    private int countBoundaryNeighborsMatching(List<List<Map<String, Object>>> data,
                                               List<int[]> component,
                                               java.util.function.Predicate<Map<String, Object>> predicate) {
        Set<String> points = new HashSet<>();
        Set<String> counted = new HashSet<>();
        for (int[] point : component) {
            points.add(point[0] + ":" + point[1]);
        }
        int count = 0;
        int[][] directions = new int[][]{
                {-1, -1}, {0, -1}, {1, -1},
                {-1, 0},           {1, 0},
                {-1, 1},  {0, 1},  {1, 1}
        };
        for (int[] point : component) {
            for (int[] direction : directions) {
                int ny = point[0] + direction[1];
                int nx = point[1] + direction[0];
                String key = ny + ":" + nx;
                if (ny < 0 || ny >= data.size() || nx < 0 || nx >= data.get(ny).size()
                        || points.contains(key) || counted.contains(key)) {
                    continue;
                }
                counted.add(key);
                if (predicate.test(data.get(ny).get(nx))) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countNeighborsMatching(List<List<Map<String, Object>>> data,
                                       int y,
                                       int x,
                                       java.util.function.Predicate<Map<String, Object>> predicate) {
        int count = 0;
        for (int ny = Math.max(0, y - 1); ny <= Math.min(data.size() - 1, y + 1); ny++) {
            List<Map<String, Object>> row = data.get(ny);
            for (int nx = Math.max(0, x - 1); nx <= Math.min(row.size() - 1, x + 1); nx++) {
                if (ny == y && nx == x) {
                    continue;
                }
                if (predicate.test(row.get(nx))) {
                    count++;
                }
            }
        }
        return count;
    }

    private Map<String, Object> dominantSoftNeighbor(List<List<Map<String, Object>>> data, int y, int x) {
        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        for (int ny = Math.max(0, y - 2); ny <= Math.min(data.size() - 1, y + 2); ny++) {
            List<Map<String, Object>> row = data.get(ny);
            for (int nx = Math.max(0, x - 2); nx <= Math.min(row.size() - 1, x + 2); nx++) {
                if (ny == y && nx == x) {
                    continue;
                }
                Map<String, Object> cell = row.get(nx);
                if (isHarshBlack(cell) || Boolean.TRUE.equals(cell.get("isExternal"))) {
                    continue;
                }
                String id = String.valueOf(cell.get("id"));
                NeighborCount existing = counts.get(id);
                counts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
            }
        }
        return counts.values().stream()
                .max(Comparator.comparingInt(NeighborCount::count))
                .map(NeighborCount::cell)
                .orElse(null);
    }

    private Map<String, NeighborCount> countNeighborColors(List<List<Map<String, Object>>> data, int y, int x) {
        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        int[][] directions = new int[][]{
                {-1, -1}, {0, -1}, {1, -1},
                {-1, 0},           {1, 0},
                {-1, 1},  {0, 1},  {1, 1}
        };
        for (int[] direction : directions) {
            int ny = y + direction[1];
            int nx = x + direction[0];
            if (ny < 0 || ny >= data.size()) {
                continue;
            }
            List<Map<String, Object>> row = data.get(ny);
            if (nx < 0 || nx >= row.size()) {
                continue;
            }
            Map<String, Object> cell = row.get(nx);
            String id = String.valueOf(cell.get("id"));
            NeighborCount existing = counts.get(id);
            counts.put(id, existing == null
                    ? new NeighborCount(cell, 1)
                    : new NeighborCount(existing.cell(), existing.count() + 1));
        }
        return counts;
    }

    private boolean isStrongLineOrFeatureColor(Map<String, Object> cell) {
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        double lum = luminance(cell);
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        boolean saturated = max - min >= 80;
        return lum < 85 || (saturated && (r > 150 || b > 150));
    }

    private boolean isHarshBlack(Map<String, Object> cell) {
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        return r <= 55 && g <= 55 && b <= 55 && luminance(cell) < 52;
    }

    private void addPixelArtOuterOutline(List<List<Map<String, Object>>> data, Map<String, Object> outlineCell) {
        Map<String, Object> backgroundCell = dominantEdgeBackgroundCell(data);
        if (backgroundCell == null || outlineCell == null || luminance(backgroundCell) < 185) {
            return;
        }

        List<int[]> outlinePoints = new ArrayList<>();
        for (int y = 0; y < data.size(); y++) {
            for (int x = 0; x < data.get(y).size(); x++) {
                Map<String, Object> cell = data.get(y).get(x);
                if (!isBackgroundCell(cell, backgroundCell)) {
                    continue;
                }
                int foregroundNeighbors = countNeighborsMatching(data, y, x,
                        neighbor -> !isBackgroundCell(neighbor, backgroundCell));
                if (foregroundNeighbors >= 1 && foregroundNeighbors <= 5) {
                    outlinePoints.add(new int[]{y, x});
                }
            }
        }

        for (int[] point : outlinePoints) {
            data.get(point[0]).set(point[1], cloneColorCell(outlineCell));
        }
    }

    private Map<String, Object> dominantEdgeBackgroundCell(List<List<Map<String, Object>>> data) {
        if (data.isEmpty() || data.get(0).isEmpty()) {
            return null;
        }
        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        int rows = data.size();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < data.get(y).size(); x++) {
                boolean edge = y == 0 || y == rows - 1 || x == 0 || x == data.get(y).size() - 1;
                if (!edge) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                if (cell == null || Boolean.TRUE.equals(cell.get("isExternal"))) {
                    continue;
                }
                String id = String.valueOf(cell.get("id"));
                NeighborCount existing = counts.get(id);
                counts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
            }
        }
        return counts.values().stream()
                .max(Comparator.comparingInt(NeighborCount::count))
                .map(NeighborCount::cell)
                .orElse(null);
    }

    private boolean isBackgroundCell(Map<String, Object> cell, Map<String, Object> backgroundCell) {
        if (cell == null || backgroundCell == null || Boolean.TRUE.equals(cell.get("isExternal"))) {
            return false;
        }
        if (sameColor(cell, backgroundCell)) {
            return true;
        }
        return luminance(cell) >= 188 && colorDistance(cell, backgroundCell) <= 36;
    }

    private Map<String, Object> cloneColorCell(Map<String, Object> source) {
        Map<String, Object> replacement = new LinkedHashMap<>();
        replacement.put("id", source.get("id"));
        replacement.put("name", source.get("name"));
        replacement.put("hex", source.get("hex"));
        replacement.put("r", source.get("r"));
        replacement.put("g", source.get("g"));
        replacement.put("b", source.get("b"));
        replacement.put("isExternal", false);
        return replacement;
    }

    private MappedResult mirrorGridRows(MappedResult mapped) {
        List<List<Map<String, Object>>> data = mapped.mappedPixelData;
        List<List<Map<String, Object>>> mirrored = new ArrayList<>(data.size());
        for (List<Map<String, Object>> row : data) {
            List<Map<String, Object>> reversed = new ArrayList<>(row);
            Collections.reverse(reversed);
            mirrored.add(reversed);
        }
        List<Map<String, Object>> newColorStats = calcColorStats(mirrored);
        List<List<String>> gridData = buildGridData(mirrored);
        List<Map<String, Object>> colorPalette = buildColorPalette(newColorStats);
        return new MappedResult(mirrored, newColorStats, gridData, colorPalette);
    }

    private MappedResult harmonizeFaceSkinTones(MappedResult mapped) {
        List<List<Map<String, Object>>> source = mapped.mappedPixelData;
        if (source.isEmpty() || source.get(0).isEmpty()) {
            return mapped;
        }

        FaceBounds face = detectFaceBounds(source);
        if (face == null) {
            return mapped;
        }

        Map<String, Object> mainSkin = findCellById(source, face.dominantSkinId());
        if (mainSkin == null) {
            return mapped;
        }

        List<Map<String, Object>> allowedSkinTones = chooseAllowedFaceSkinTones(source, face, mainSkin);
        if (allowedSkinTones.isEmpty()) {
            return mapped;
        }

        Set<String> allowedIds = new LinkedHashSet<>();
        for (Map<String, Object> tone : allowedSkinTones) {
            allowedIds.add(String.valueOf(tone.get("id")));
        }

        List<List<Map<String, Object>>> data = deepCopyGrid(source);
        boolean changed = false;
        for (int y = face.minY(); y <= face.maxY() && y < source.size(); y++) {
            for (int x = face.minX(); x <= face.maxX() && x < source.get(y).size(); x++) {
                if (x < 0) {
                    continue;
                }
                Map<String, Object> cell = source.get(y).get(x);
                if (Boolean.TRUE.equals(cell.get("isExternal")) || isProtectedFacialFeature(face, y, x, cell)) {
                    continue;
                }

                String id = String.valueOf(cell.get("id"));
                boolean allowedSkin = allowedIds.contains(id);
                boolean skinLike = isFaceSkinCandidate(cell, mainSkin);
                boolean noisySkinTone = skinLike && isNoisyFaceSkinTone(source, face, y, x, cell, allowedSkin, allowedIds);
                boolean strayInsideSkin = !skinLike && isFaceSkinArtifact(source, face, y, x, cell);
                if (skinLike && !noisySkinTone) {
                    continue;
                }
                if (!skinLike && !strayInsideSkin) {
                    continue;
                }

                Map<String, Object> replacement = dominantAllowedSkinNeighbor(source, face, y, x, allowedIds);
                if (replacement == null) {
                    replacement = nearestAllowedSkinTone(cell, allowedSkinTones);
                }
                if (replacement != null && !sameColor(cell, replacement)) {
                    data.get(y).set(x, cloneColorCell(replacement));
                    changed = true;
                }
            }
        }

        if (!changed) {
            return mapped;
        }

        List<Map<String, Object>> newColorStats = calcColorStats(data);
        List<List<String>> gridData = buildGridData(data);
        List<Map<String, Object>> colorPalette = buildColorPalette(newColorStats);
        return new MappedResult(data, newColorStats, gridData, colorPalette);
    }

    private MappedResult harmonizePlainBackground(MappedResult mapped) {
        List<List<Map<String, Object>>> source = mapped.mappedPixelData;
        if (source.isEmpty() || source.get(0).isEmpty()) {
            return mapped;
        }

        Map<String, Object> background = dominantEdgeBackgroundCell(source);
        if (background == null || luminance(background) < 205) {
            return mapped;
        }

        List<List<Map<String, Object>>> data = deepCopyGrid(source);
        boolean[][] visited = new boolean[source.size()][];
        for (int y = 0; y < source.size(); y++) {
            visited[y] = new boolean[source.get(y).size()];
        }

        boolean changed = false;
        Deque<int[]> queue = new ArrayDeque<>();
        for (int y = 0; y < source.size(); y++) {
            enqueueBackgroundEdge(source, visited, queue, y, 0, background);
            enqueueBackgroundEdge(source, visited, queue, y, source.get(y).size() - 1, background);
        }
        for (int x = 0; x < source.get(0).size(); x++) {
            enqueueBackgroundEdge(source, visited, queue, 0, x, background);
            enqueueBackgroundEdge(source, visited, queue, source.size() - 1, x, background);
        }

        int[][] directions = new int[][]{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            int y = point[0];
            int x = point[1];
            Map<String, Object> cell = source.get(y).get(x);
            if (!sameColor(cell, background)) {
                data.get(y).set(x, cloneColorCell(background));
                changed = true;
            }

            for (int[] direction : directions) {
                int ny = y + direction[1];
                int nx = x + direction[0];
                if (ny < 0 || ny >= source.size() || nx < 0 || nx >= source.get(ny).size()
                        || visited[ny][nx]) {
                    continue;
                }
                if (!isPlainBackgroundCandidate(source.get(ny).get(nx), background)) {
                    continue;
                }
                visited[ny][nx] = true;
                queue.addLast(new int[]{ny, nx});
            }
        }

        if (!changed) {
            return mapped;
        }

        List<Map<String, Object>> newColorStats = calcColorStats(data);
        List<List<String>> gridData = buildGridData(data);
        List<Map<String, Object>> colorPalette = buildColorPalette(newColorStats);
        return new MappedResult(data, newColorStats, gridData, colorPalette);
    }

    private void enqueueBackgroundEdge(List<List<Map<String, Object>>> data,
                                       boolean[][] visited,
                                       Deque<int[]> queue,
                                       int y,
                                       int x,
                                       Map<String, Object> background) {
        if (y < 0 || y >= data.size() || x < 0 || x >= data.get(y).size() || visited[y][x]) {
            return;
        }
        if (!isPlainBackgroundCandidate(data.get(y).get(x), background)) {
            return;
        }
        visited[y][x] = true;
        queue.addLast(new int[]{y, x});
    }

    private boolean isPlainBackgroundCandidate(Map<String, Object> cell, Map<String, Object> background) {
        if (cell == null || background == null || Boolean.TRUE.equals(cell.get("isExternal"))) {
            return false;
        }
        if (sameColor(cell, background)) {
            return true;
        }
        double lum = luminance(cell);
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        return lum >= 200 && max - min <= 30 && colorDistance(cell, background) <= 62;
    }

    private List<Map<String, Object>> chooseAllowedFaceSkinTones(List<List<Map<String, Object>>> data,
                                                                  FaceBounds face,
                                                                  Map<String, Object> mainSkin) {
        Map<String, NeighborCount> skinCounts = new LinkedHashMap<>();
        for (int y = face.minY(); y <= face.maxY() && y < data.size(); y++) {
            for (int x = face.minX(); x <= face.maxX() && x < data.get(y).size(); x++) {
                if (x < 0) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                if (!isFaceSkinCandidate(cell, mainSkin) || isProtectedFacialFeature(face, y, x, cell)) {
                    continue;
                }
                String id = String.valueOf(cell.get("id"));
                NeighborCount existing = skinCounts.get(id);
                skinCounts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
            }
        }

        List<Map<String, Object>> tones = new ArrayList<>();
        tones.add(mainSkin);

        Map<String, Object> lightTone = skinCounts.values().stream()
                .filter(item -> !sameColor(item.cell(), mainSkin))
                .filter(item -> item.count() >= 3)
                .filter(item -> luminance(item.cell()) >= luminance(mainSkin) - 4)
                .filter(item -> colorDistance(item.cell(), mainSkin) <= 58)
                .max(Comparator.comparingDouble(item -> item.count() * 2.0 + luminance(item.cell()) * 0.05))
                .map(NeighborCount::cell)
                .orElse(null);
        if (lightTone != null) {
            tones.add(lightTone);
        }

        skinCounts.values().stream()
                .filter(item -> !sameColor(item.cell(), mainSkin) && !sameColor(item.cell(), lightTone))
                .filter(item -> item.count() >= Math.max(3, face.width() * face.height() / 110))
                .filter(item -> isSoftBlushTone(item.cell(), mainSkin))
                .sorted((a, b) -> Integer.compare(b.count(), a.count()))
                .limit(1)
                .map(NeighborCount::cell)
                .forEach(tones::add);

        return tones;
    }

    private boolean isFaceSkinCandidate(Map<String, Object> cell, Map<String, Object> mainSkin) {
        if (cell == null || Boolean.TRUE.equals(cell.get("isExternal"))) {
            return false;
        }
        double lum = luminance(cell);
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        boolean warmLight = r >= 190 && g >= 135 && b >= 105 && r >= g - 5 && g >= b - 30 && lum >= 135;
        boolean closeToMain = mainSkin != null && colorDistance(cell, mainSkin) <= 82 && lum >= 120;
        return isSkinCell(cell) || warmLight || closeToMain;
    }

    private boolean isSoftBlushTone(Map<String, Object> cell, Map<String, Object> mainSkin) {
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        double lum = luminance(cell);
        double mainLum = luminance(mainSkin);
        return r >= g + 18
                && r >= b + 28
                && lum >= 145
                && lum <= mainLum + 18
                && colorDistance(cell, mainSkin) <= 82;
    }

    private boolean isIsolatedFaceTone(List<List<Map<String, Object>>> data, FaceBounds face, int y, int x) {
        Map<String, Object> cell = data.get(y).get(x);
        int same = countNeighborsMatching(data, y, x, other -> sameColor(cell, other));
        int skin = countNeighborsMatching(data, y, x, other -> isFaceSkinCandidate(other, cell));
        return same <= 1 && skin >= 4;
    }

    private boolean isNoisyFaceSkinTone(List<List<Map<String, Object>>> data,
                                        FaceBounds face,
                                        int y,
                                        int x,
                                        Map<String, Object> cell,
                                        boolean allowedSkin,
                                        Set<String> allowedIds) {
        int same = countNeighborsMatching(data, y, x, other -> sameColor(cell, other));
        int allowedNeighbors = countNeighborsMatching(data, y, x,
                other -> allowedIds.contains(String.valueOf(other.get("id"))));
        int skinNeighbors = countNeighborsMatching(data, y, x,
                other -> isFaceSkinCandidate(other, cell));
        if (allowedSkin) {
            return same <= 1 && skinNeighbors >= 5 && allowedNeighbors >= 3;
        }
        if (isSoftBlushTone(cell, findCellById(data, face.dominantSkinId())) && same >= 2) {
            return false;
        }
        boolean smallPatch = same <= 3 && skinNeighbors >= 4;
        boolean surroundedByAllowed = allowedNeighbors >= 4 && same <= 4;
        boolean centerFace = y > face.minY() + face.height() * 0.08 && y < face.maxY() - face.height() * 0.06;
        return centerFace && (smallPatch || surroundedByAllowed || isIsolatedFaceTone(data, face, y, x));
    }

    private boolean isFaceSkinArtifact(List<List<Map<String, Object>>> data,
                                       FaceBounds face,
                                       int y,
                                       int x,
                                       Map<String, Object> cell) {
        if (cell == null || isStrongLineOrFeatureColor(cell) || isHarshBlack(cell)) {
            return false;
        }
        double lum = luminance(cell);
        if (lum < 85 || lum > 235) {
            return false;
        }
        int skinNeighbors = countNeighborsMatching(data, y, x, this::isSkinCell);
        int sameNeighbors = countNeighborsMatching(data, y, x, other -> sameColor(cell, other));
        boolean centerFace = y > face.minY() + face.height() * 0.10 && y < face.maxY() - face.height() * 0.08;
        return centerFace && skinNeighbors >= 3 && sameNeighbors <= 2;
    }

    private Map<String, Object> dominantAllowedSkinNeighbor(List<List<Map<String, Object>>> data,
                                                            FaceBounds face,
                                                            int y,
                                                            int x,
                                                            Set<String> allowedIds) {
        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        for (int ny = Math.max(face.minY(), y - 2); ny <= Math.min(face.maxY(), y + 2) && ny < data.size(); ny++) {
            for (int nx = Math.max(face.minX(), x - 2); nx <= Math.min(face.maxX(), x + 2) && nx < data.get(ny).size(); nx++) {
                if (ny == y && nx == x || nx < 0) {
                    continue;
                }
                Map<String, Object> cell = data.get(ny).get(nx);
                String id = String.valueOf(cell.get("id"));
                if (!allowedIds.contains(id)) {
                    continue;
                }
                NeighborCount existing = counts.get(id);
                counts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
            }
        }
        return counts.values().stream()
                .max(Comparator.comparingInt(NeighborCount::count))
                .map(NeighborCount::cell)
                .orElse(null);
    }

    private Map<String, Object> nearestAllowedSkinTone(Map<String, Object> source,
                                                       List<Map<String, Object>> allowedSkinTones) {
        return allowedSkinTones.stream()
                .min(Comparator.comparingDouble(tone -> colorDistance(source, tone)))
                .orElse(null);
    }

    private FaceBounds detectFaceBounds(List<List<Map<String, Object>>> data) {
        if (data.isEmpty() || data.get(0).isEmpty()) {
            return null;
        }

        int rows = data.size();
        int cols = data.get(0).size();
        boolean[][] visited = new boolean[rows][];
        for (int y = 0; y < rows; y++) {
            visited[y] = new boolean[data.get(y).size()];
        }

        FaceBounds best = null;
        int bestCount = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < data.get(y).size(); x++) {
                if (visited[y][x] || !isSkinCell(data.get(y).get(x))) {
                    continue;
                }

                int minX = x;
                int minY = y;
                int maxX = x;
                int maxY = y;
                int skinCount = 0;
                Map<String, Integer> skinCounts = new LinkedHashMap<>();
                Deque<int[]> queue = new ArrayDeque<>();
                queue.add(new int[]{y, x});
                visited[y][x] = true;
                while (!queue.isEmpty()) {
                    int[] point = queue.removeFirst();
                    int cy = point[0];
                    int cx = point[1];
                    Map<String, Object> cell = data.get(cy).get(cx);
                    skinCount++;
                    minX = Math.min(minX, cx);
                    minY = Math.min(minY, cy);
                    maxX = Math.max(maxX, cx);
                    maxY = Math.max(maxY, cy);
                    String id = (String) cell.get("id");
                    if (id != null) {
                        skinCounts.merge(id, 1, Integer::sum);
                    }

                    int[][] directions = new int[][]{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
                    for (int[] direction : directions) {
                        int ny = cy + direction[1];
                        int nx = cx + direction[0];
                        if (ny < 0 || ny >= rows || nx < 0 || nx >= data.get(ny).size()
                                || visited[ny][nx] || !isSkinCell(data.get(ny).get(nx))) {
                            continue;
                        }
                        visited[ny][nx] = true;
                        queue.addLast(new int[]{ny, nx});
                    }
                }

                int faceW = maxX - minX + 1;
                int faceH = maxY - minY + 1;
                if (skinCount < 32 || faceW < 8 || faceH < 8 || faceW > cols * 0.8 || faceH > rows * 0.8) {
                    continue;
                }
                String dominantSkinId = skinCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
                if (skinCount > bestCount) {
                    bestCount = skinCount;
                    best = new FaceBounds(minX, minY, maxX, maxY, dominantSkinId);
                }
            }
        }

        return best;
    }

    private boolean isProtectedFacialFeature(FaceBounds face, int y, int x, Map<String, Object> cell) {
        if (face == null || !face.contains(y, x)) {
            return false;
        }

        int faceW = face.width();
        int faceH = face.height();
        double relX = (x - face.minX()) / Math.max(1.0, faceW - 1.0);
        double relY = (y - face.minY()) / Math.max(1.0, faceH - 1.0);
        boolean eyeBand = relY >= 0.20 && relY <= 0.56 && relX >= 0.10 && relX <= 0.90;
        boolean mouthBand = relY >= 0.52 && relY <= 0.84 && relX >= 0.25 && relX <= 0.75;
        if (!eyeBand && !mouthBand) {
            return false;
        }

        if (isSkinCell(cell)) {
            return false;
        }

        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        double lum = luminance(cell);
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        boolean darkLine = lum < 115;
        boolean warmMouth = mouthBand && r >= g + 8 && g >= b - 25 && lum >= 55 && lum <= 185;
        boolean saturatedEyeOrMouth = max - min >= 55 && lum >= 45 && lum <= 210;
        return darkLine || warmMouth || saturatedEyeOrMouth;
    }

    private MappedResult stabilizeAvatarFacialLines(MappedResult mapped, FaceBounds detectedFace) {
        List<List<Map<String, Object>>> data = deepCopyGrid(mapped.mappedPixelData);
        if (data.isEmpty() || data.get(0).isEmpty()) {
            return mapped;
        }

        int rows = data.size();
        FaceBounds face = detectedFace != null ? detectedFace : detectFaceBounds(data);
        if (face == null) {
            return mapped;
        }
        int minX = face.minX();
        int minY = face.minY();
        int maxX = face.maxX();
        int maxY = face.maxY();
        int faceH = face.height();

        Map<String, Object> skinCell = findCellById(data, face.dominantSkinId());
        Map<String, Object> blackCell = darkestCell(data);
        if (skinCell == null || blackCell == null) {
            return mapped;
        }

        // The AI image is already generated as grid-ready pixel art. Keep facial pixels
        // mostly source-driven; broad cleanup is safer than repainting eyes or mouth.
        stabilizeClothingBlocks(data, face);
        softenLargeBlackInteriors(data, blackCell, face);
        neutralizeWarmDarkHairNoise(data, face);

        List<Map<String, Object>> colorStats = calcColorStats(data);
        List<List<String>> gridData = buildGridData(data);
        List<Map<String, Object>> colorPalette = buildColorPalette(colorStats);
        return new MappedResult(data, colorStats, gridData, colorPalette);
    }

    private boolean isSkinCell(Map<String, Object> cell) {
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        return r >= 205 && g >= 145 && g <= 225 && b >= 115 && b <= 210 && r >= g && g >= b - 20;
    }

    private void preserveReadableEyeHighlights(List<List<Map<String, Object>>> data,
                                                FaceBounds face,
                                                Map<String, Object> outlineCell,
                                                Map<String, Object> skinCell) {
        if (face == null || outlineCell == null) {
            return;
        }

        Map<String, Object> irisCell = chooseSoftEyeIrisCell(data, face, outlineCell);
        if (irisCell == null) {
            irisCell = bestWarmEyeCellInFace(data, face);
        }
        Map<String, Object> highlightCell = softWhiteCell(data);
        restoreOneEyeHighlight(data, face, true, irisCell, highlightCell, skinCell);
        restoreOneEyeHighlight(data, face, false, irisCell, highlightCell, skinCell);
    }

    private void restoreOneEyeHighlight(List<List<Map<String, Object>>> data,
                                        FaceBounds face,
                                        boolean leftHalf,
                                        Map<String, Object> irisCell,
                                        Map<String, Object> highlightCell,
                                        Map<String, Object> skinCell) {
        EyeAnchor anchor = findEyeAnchor(data, face, leftHalf);
        if (anchor == null) {
            return;
        }

        int cx = anchor.x();
        int cy = anchor.y();
        if (countSkinNeighbors(data, face, cy, cx, 2) < 2) {
            return;
        }

        if (irisCell != null) {
            paintEyeOnlyCell(data, face, cy, cx, irisCell);
        }

        int lightSide = leftHalf ? 1 : -1;
        if (highlightCell != null) {
            paintEyeOnlyCell(data, face, cy, cx + lightSide, highlightCell);
            if (face.width() >= 18) {
                paintEyeOnlyCell(data, face, cy - 1, cx + lightSide, highlightCell);
            }
        } else if (skinCell != null) {
            paintEyeOnlyCell(data, face, cy, cx + lightSide, skinCell);
        }
    }

    private void paintEyeOnlyCell(List<List<Map<String, Object>>> data,
                                  FaceBounds face,
                                  int y,
                                  int x,
                                  Map<String, Object> cell) {
        if (cell == null || y < 0 || y >= data.size() || x < 0 || x >= data.get(y).size()
                || !face.contains(y, x)) {
            return;
        }
        if (countSkinNeighbors(data, face, y, x, 2) < 2) {
            return;
        }
        double relY = (y - face.minY()) / Math.max(1.0, face.height() - 1.0);
        if (relY < 0.30 || relY > 0.62) {
            return;
        }
        Map<String, Object> current = data.get(y).get(x);
        if (isSkinCell(current) && luminance(cell) < 90) {
            return;
        }
        data.get(y).set(x, cloneColorCell(cell));
    }

    private void neutralizeWarmDarkHairNoise(List<List<Map<String, Object>>> data, FaceBounds face) {
        Map<String, Object> neutralDark = chooseNeutralDarkHairCell(data);
        if (neutralDark == null || face == null) {
            return;
        }

        int yLimit = Math.min(data.size() - 1, face.maxY() + Math.max(1, (int) Math.round(face.height() * 0.08)));
        for (int y = 0; y <= yLimit; y++) {
            for (int x = 0; x < data.get(y).size(); x++) {
                Map<String, Object> cell = data.get(y).get(x);
                if (!isWarmDarkHairNoise(cell) || isProtectedFacialFeature(face, y, x, cell)) {
                    continue;
                }
                int darkNeighbors = countNeighborsMatching(data, y, x, this::isDarkRegionCell);
                int skinNeighbors = countNeighborsMatching(data, y, x, this::isSkinCell);
                if (darkNeighbors >= 3 && skinNeighbors <= 2) {
                    data.get(y).set(x, cloneColorCell(neutralDark));
                }
            }
        }
    }

    private Map<String, Object> chooseNeutralDarkHairCell(List<List<Map<String, Object>>> data) {
        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        for (List<Map<String, Object>> row : data) {
            for (Map<String, Object> cell : row) {
                if (!isNeutralDarkHairCell(cell)) {
                    continue;
                }
                String id = String.valueOf(cell.get("id"));
                NeighborCount existing = counts.get(id);
                counts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
            }
        }
        return counts.values().stream()
                .max(Comparator.comparingInt(NeighborCount::count))
                .map(NeighborCount::cell)
                .orElse(null);
    }

    private boolean isNeutralDarkHairCell(Map<String, Object> cell) {
        if (cell == null || Boolean.TRUE.equals(cell.get("isExternal")) || isSkinCell(cell)) {
            return false;
        }
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        double lum = luminance(cell);
        return lum >= 20 && lum <= 105
                && Math.abs(r - g) <= 18
                && Math.abs(g - b) <= 18
                && Math.abs(r - b) <= 24;
    }

    private boolean isWarmDarkHairNoise(Map<String, Object> cell) {
        if (cell == null || Boolean.TRUE.equals(cell.get("isExternal")) || isSkinCell(cell)) {
            return false;
        }
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        double lum = luminance(cell);
        return lum < 125 && r >= g + 8 && r >= b + 12;
    }

    private void smoothLargeDarkRegions(List<List<Map<String, Object>>> data, FaceBounds face) {
        if (data.isEmpty() || data.get(0).isEmpty()) {
            return;
        }

        boolean[][] visited = new boolean[data.size()][];
        for (int y = 0; y < data.size(); y++) {
            visited[y] = new boolean[data.get(y).size()];
        }

        List<int[]> replacements = new ArrayList<>();
        List<Map<String, Object>> replacementCells = new ArrayList<>();
        for (int y = 0; y < data.size(); y++) {
            for (int x = 0; x < data.get(y).size(); x++) {
                if (visited[y][x] || !isDarkRegionCell(data.get(y).get(x))) {
                    continue;
                }

                List<int[]> component = collectDarkRegionComponent(data, visited, y, x);
                if (component.size() < 18) {
                    continue;
                }
                for (int[] point : component) {
                    int py = point[0];
                    int px = point[1];
                    Map<String, Object> cell = data.get(py).get(px);
                    if (isProtectedFacialFeature(face, py, px, cell) || isDarkRegionBoundaryCell(data, py, px)) {
                        continue;
                    }
                    Map<String, Object> replacement = dominantSoftDarkNeighbor(data, py, px);
                    if (replacement != null && !sameColor(replacement, cell)) {
                        replacements.add(point);
                        replacementCells.add(replacement);
                    }
                }
            }
        }

        for (int i = 0; i < replacements.size(); i++) {
            int[] point = replacements.get(i);
            data.get(point[0]).set(point[1], cloneColorCell(replacementCells.get(i)));
        }
    }

    private List<int[]> collectDarkRegionComponent(List<List<Map<String, Object>>> data,
                                                   boolean[][] visited,
                                                   int startY,
                                                   int startX) {
        List<int[]> component = new ArrayList<>();
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startY, startX});
        visited[startY][startX] = true;

        int[][] directions = new int[][]{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            component.add(point);
            for (int[] direction : directions) {
                int ny = point[0] + direction[1];
                int nx = point[1] + direction[0];
                if (ny < 0 || ny >= data.size() || nx < 0 || nx >= data.get(ny).size()
                        || visited[ny][nx] || !isDarkRegionCell(data.get(ny).get(nx))) {
                    continue;
                }
                visited[ny][nx] = true;
                queue.addLast(new int[]{ny, nx});
            }
        }
        return component;
    }

    private boolean isDarkRegionBoundaryCell(List<List<Map<String, Object>>> data, int y, int x) {
        return countNeighborsMatching(data, y, x, this::isDarkRegionCell) < 7;
    }

    private Map<String, Object> dominantSoftDarkNeighbor(List<List<Map<String, Object>>> data, int y, int x) {
        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        for (int ny = Math.max(0, y - 2); ny <= Math.min(data.size() - 1, y + 2); ny++) {
            for (int nx = Math.max(0, x - 2); nx <= Math.min(data.get(ny).size() - 1, x + 2); nx++) {
                if (ny == y && nx == x) {
                    continue;
                }
                Map<String, Object> cell = data.get(ny).get(nx);
                if (!isSoftDarkSmoothingCandidate(cell)) {
                    continue;
                }
                String id = String.valueOf(cell.get("id"));
                NeighborCount existing = counts.get(id);
                counts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
            }
        }
        return counts.values().stream()
                .filter(item -> item.count() >= 4)
                .max(Comparator.comparingInt(NeighborCount::count))
                .map(NeighborCount::cell)
                .orElse(null);
    }

    private boolean isDarkRegionCell(Map<String, Object> cell) {
        return cell != null
                && !Boolean.TRUE.equals(cell.get("isExternal"))
                && !isSkinCell(cell)
                && luminance(cell) < 118;
    }

    private boolean isSoftDarkSmoothingCandidate(Map<String, Object> cell) {
        return isDarkRegionCell(cell) && !isHarshBlack(cell);
    }

    private void softenLargeBlackInteriors(List<List<Map<String, Object>>> data,
                                           Map<String, Object> outlineCell,
                                           FaceBounds face) {
        if (data.isEmpty() || data.get(0).isEmpty() || outlineCell == null) {
            return;
        }

        boolean[][] visited = new boolean[data.size()][];
        for (int y = 0; y < data.size(); y++) {
            visited[y] = new boolean[data.get(y).size()];
        }

        for (int y = 0; y < data.size(); y++) {
            for (int x = 0; x < data.get(y).size(); x++) {
                if (visited[y][x] || !isHarshBlack(data.get(y).get(x))) {
                    continue;
                }

                List<int[]> component = collectHarshBlackComponent(data, visited, y, x);
                if (component.size() < 10) {
                    continue;
                }

                Map<String, Object> fillCell = chooseSoftFillForBlackComponent(data, component, outlineCell);
                if (fillCell == null || sameColor(fillCell, outlineCell)) {
                    continue;
                }

                for (int[] point : component) {
                    int py = point[0];
                    int px = point[1];
                    Map<String, Object> cell = data.get(py).get(px);
                    if (isProtectedFacialFeature(face, py, px, cell)) {
                        continue;
                    }
                    if (isBlackBoundaryCell(data, py, px)) {
                        continue;
                    }
                    data.get(py).set(px, cloneColorCell(fillCell));
                }
            }
        }
    }

    private List<int[]> collectHarshBlackComponent(List<List<Map<String, Object>>> data,
                                                   boolean[][] visited,
                                                   int startY,
                                                   int startX) {
        List<int[]> component = new ArrayList<>();
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startY, startX});
        visited[startY][startX] = true;

        int[][] directions = new int[][]{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            component.add(point);
            for (int[] direction : directions) {
                int ny = point[0] + direction[1];
                int nx = point[1] + direction[0];
                if (ny < 0 || ny >= data.size() || nx < 0 || nx >= data.get(ny).size()
                        || visited[ny][nx] || !isHarshBlack(data.get(ny).get(nx))) {
                    continue;
                }
                visited[ny][nx] = true;
                queue.addLast(new int[]{ny, nx});
            }
        }
        return component;
    }

    private boolean isBlackBoundaryCell(List<List<Map<String, Object>>> data, int y, int x) {
        if (y <= 0 || y >= data.size() - 1 || x <= 0 || x >= data.get(y).size() - 1) {
            return true;
        }

        int harshNeighbors = countNeighborsMatching(data, y, x, this::isHarshBlack);
        if (harshNeighbors < 7) {
            return true;
        }

        int[][] cross = new int[][]{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        for (int[] direction : cross) {
            int ny = y + direction[1];
            int nx = x + direction[0];
            if (ny < 0 || ny >= data.size() || nx < 0 || nx >= data.get(ny).size()
                    || !isHarshBlack(data.get(ny).get(nx))) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> chooseSoftFillForBlackComponent(List<List<Map<String, Object>>> data,
                                                                List<int[]> component,
                                                                Map<String, Object> outlineCell) {
        Set<String> points = new HashSet<>();
        for (int[] point : component) {
            points.add(point[0] + ":" + point[1]);
        }

        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        int[][] directions = new int[][]{
                {-1, -1}, {0, -1}, {1, -1},
                {-1, 0},           {1, 0},
                {-1, 1},  {0, 1},  {1, 1}
        };
        for (int[] point : component) {
            for (int[] direction : directions) {
                int ny = point[0] + direction[1];
                int nx = point[1] + direction[0];
                if (ny < 0 || ny >= data.size() || nx < 0 || nx >= data.get(ny).size()
                        || points.contains(ny + ":" + nx)) {
                    continue;
                }
                Map<String, Object> cell = data.get(ny).get(nx);
                if (!isSoftBlackFillCandidate(cell, outlineCell)) {
                    continue;
                }
                String id = String.valueOf(cell.get("id"));
                NeighborCount existing = counts.get(id);
                counts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
            }
        }

        Map<String, Object> boundaryFill = counts.values().stream()
                .max(Comparator.comparingInt(NeighborCount::count))
                .map(NeighborCount::cell)
                .orElse(null);
        return boundaryFill != null ? boundaryFill : chooseGlobalSoftBlackFill(data, outlineCell);
    }

    private Map<String, Object> chooseGlobalSoftBlackFill(List<List<Map<String, Object>>> data,
                                                          Map<String, Object> outlineCell) {
        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        for (List<Map<String, Object>> row : data) {
            for (Map<String, Object> cell : row) {
                if (!isSoftBlackFillCandidate(cell, outlineCell)) {
                    continue;
                }
                String id = String.valueOf(cell.get("id"));
                NeighborCount existing = counts.get(id);
                counts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
            }
        }
        return counts.values().stream()
                .max(Comparator.comparingInt(NeighborCount::count))
                .map(NeighborCount::cell)
                .orElse(null);
    }

    private boolean isSoftBlackFillCandidate(Map<String, Object> cell, Map<String, Object> outlineCell) {
        if (cell == null || Boolean.TRUE.equals(cell.get("isExternal"))
                || isHarshBlack(cell) || isSkinCell(cell) || sameColor(cell, outlineCell)) {
            return false;
        }
        double lum = luminance(cell);
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        boolean darkNeutralOrBrown = max - min <= 58 || (r >= g - 8 && g >= b - 18);
        return lum >= 48 && lum <= 135 && darkNeutralOrBrown && colorDistance(cell, outlineCell) >= 20;
    }

    private void enhanceExistingEyes(List<List<Map<String, Object>>> data,
                                     FaceBounds face,
                                     Map<String, Object> eyeCell,
                                     Map<String, Object> skinCell) {
        if (hasExistingEyePixelsInBand(data, face, true) && hasExistingEyePixelsInBand(data, face, false)) {
            return;
        }
        EyeComponent left = bestEyeComponent(data, face, true);
        EyeComponent right = bestEyeComponent(data, face, false);
        if (left == null || right == null) {
            repairEyeCenterByFacePosition(data, face, true, eyeCell, skinCell);
            repairEyeCenterByFacePosition(data, face, false, eyeCell, skinCell);
            return;
        }
        if (Math.abs(left.centerX() - right.centerX()) < Math.max(3.0, face.width() * 0.18)) {
            repairEyeCenterByFacePosition(data, face, true, eyeCell, skinCell);
            repairEyeCenterByFacePosition(data, face, false, eyeCell, skinCell);
            return;
        }
        if (hasReadableExistingEyes(data, face, left, right)) {
            return;
        }
        Map<String, Object> leftOutline = chooseEyeOutlineCell(data, left, eyeCell);
        Map<String, Object> rightOutline = chooseEyeOutlineCell(data, right, eyeCell);
        strengthenEyeComponent(data, left, leftOutline, chooseSoftEyeIrisCell(data, face, leftOutline), skinCell);
        strengthenEyeComponent(data, right, rightOutline, chooseSoftEyeIrisCell(data, face, rightOutline), skinCell);
        repairEyeCenterByFacePosition(data, face, true, leftOutline, skinCell);
        repairEyeCenterByFacePosition(data, face, false, rightOutline, skinCell);
    }

    private boolean hasReadableExistingEyes(List<List<Map<String, Object>>> data,
                                            FaceBounds face,
                                            EyeComponent left,
                                            EyeComponent right) {
        return isReadableExistingEye(data, face, left) && isReadableExistingEye(data, face, right);
    }

    private boolean isReadableExistingEye(List<List<Map<String, Object>>> data,
                                          FaceBounds face,
                                          EyeComponent eye) {
        int width = eye.maxX() - eye.minX() + 1;
        int height = eye.maxY() - eye.minY() + 1;
        if (eye.size() < 3 || width < 2 || height < 1) {
            return false;
        }
        if (eye.size() > Math.max(18, face.width() * face.height() / 18)) {
            return false;
        }

        int dark = 0;
        int softOrLight = 0;
        int skinNeighbors = 0;
        for (int[] point : eye.points()) {
            Map<String, Object> cell = data.get(point[0]).get(point[1]);
            double lum = luminance(cell);
            if (lum < 90) {
                dark++;
            }
            if (lum >= 90 || isSkinCell(cell)) {
                softOrLight++;
            }
            skinNeighbors += countSkinNeighbors(data, face, point[0], point[1], 1);
        }

        boolean hasShape = width >= 3 || eye.size() >= 5;
        boolean hasSkinContext = skinNeighbors >= Math.max(2, eye.size());
        boolean notOnlyBlackDot = !(eye.size() <= 4 && dark >= eye.size() - 1 && softOrLight == 0);
        return hasShape && hasSkinContext && notOnlyBlackDot;
    }

    private boolean hasExistingEyePixelsInBand(List<List<Map<String, Object>>> data,
                                               FaceBounds face,
                                               boolean leftHalf) {
        int centerX = (face.minX() + face.maxX()) / 2;
        int y0 = face.minY() + (int) Math.round(face.height() * 0.34);
        int y1 = face.minY() + (int) Math.round(face.height() * 0.58);
        int x0 = face.minX() + (int) Math.round(face.width() * (leftHalf ? 0.16 : 0.50));
        int x1 = face.minX() + (int) Math.round(face.width() * (leftHalf ? 0.50 : 0.84));
        int darkEyePixels = 0;
        int distinctRows = 0;
        int distinctCols = 0;
        Set<Integer> rows = new HashSet<>();
        Set<Integer> cols = new HashSet<>();

        for (int y = Math.max(face.minY(), y0); y <= Math.min(face.maxY(), y1) && y < data.size(); y++) {
            for (int x = Math.max(face.minX(), x0); x <= Math.min(face.maxX(), x1) && x < data.get(y).size(); x++) {
                if (leftHalf ? x >= centerX : x <= centerX) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                if (isSkinCell(cell) || isOrangeHairCell(cell) || luminance(cell) > 100) {
                    continue;
                }
                if (countSkinNeighbors(data, face, y, x, 2) < 2) {
                    continue;
                }
                darkEyePixels++;
                rows.add(y);
                cols.add(x);
            }
        }
        distinctRows = rows.size();
        distinctCols = cols.size();
        return darkEyePixels >= 4 && distinctRows >= 1 && distinctCols >= 2;
    }

    private EyeComponent bestEyeComponent(List<List<Map<String, Object>>> data,
                                          FaceBounds face,
                                          boolean leftHalf) {
        boolean[][] visited = new boolean[data.size()][];
        for (int y = 0; y < data.size(); y++) {
            visited[y] = new boolean[data.get(y).size()];
        }

        EyeComponent best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        int centerX = (face.minX() + face.maxX()) / 2;
        int y0 = face.minY() + (int) Math.round(face.height() * 0.30);
        int y1 = face.minY() + (int) Math.round(face.height() * 0.62);
        for (int y = Math.max(face.minY(), y0); y <= Math.min(face.maxY(), y1); y++) {
            for (int x = face.minX(); x <= face.maxX() && x < data.get(y).size(); x++) {
                if (x < 0 || visited[y][x] || (leftHalf ? x >= centerX : x <= centerX)) {
                    continue;
                }
                if (!isEyeCandidateCell(face, y, x, data.get(y).get(x))) {
                    visited[y][x] = true;
                    continue;
                }
                EyeComponent component = collectEyeComponent(data, visited, face, y, x);
                if (component.size() < 1 || component.size() > Math.max(8, face.width() / 3)) {
                    continue;
                }
                double targetX = face.minX() + face.width() * (leftHalf ? 0.36 : 0.64);
                double targetY = face.minY() + face.height() * 0.48;
                double score = component.darkness()
                        - Math.abs(component.centerX() - targetX) * 5.0
                        - Math.abs(component.centerY() - targetY) * 4.0
                        + component.size() * 8.0;
                if (score > bestScore) {
                    bestScore = score;
                    best = component;
                }
            }
        }
        return best;
    }

    private EyeComponent collectEyeComponent(List<List<Map<String, Object>>> data,
                                             boolean[][] visited,
                                             FaceBounds face,
                                             int startY,
                                             int startX) {
        List<int[]> points = new ArrayList<>();
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startY, startX});
        visited[startY][startX] = true;
        double darkness = 0;
        int sumX = 0;
        int sumY = 0;

        int[][] directions = new int[][]{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            points.add(point);
            sumY += point[0];
            sumX += point[1];
            darkness += 255.0 - luminance(data.get(point[0]).get(point[1]));
            for (int[] direction : directions) {
                int ny = point[0] + direction[1];
                int nx = point[1] + direction[0];
                if (ny < 0 || ny >= data.size() || nx < 0 || nx >= data.get(ny).size()
                        || visited[ny][nx] || !face.contains(ny, nx)) {
                    continue;
                }
                if (!isEyeCandidateCell(face, ny, nx, data.get(ny).get(nx))) {
                    continue;
                }
                visited[ny][nx] = true;
                queue.addLast(new int[]{ny, nx});
            }
        }
        return new EyeComponent(points, (double) sumX / points.size(), (double) sumY / points.size(), darkness);
    }

    private boolean isEyeCandidateCell(FaceBounds face, int y, int x, Map<String, Object> cell) {
        double relX = (x - face.minX()) / Math.max(1.0, face.width() - 1.0);
        double relY = (y - face.minY()) / Math.max(1.0, face.height() - 1.0);
        if (relY < 0.30 || relY > 0.62 || relX < 0.14 || relX > 0.86 || isSkinCell(cell)) {
            return false;
        }
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        double lum = luminance(cell);
        boolean orangeHair = r > 145 && r - g > 38 && g >= b + 6;
        boolean shirtBlue = b >= r + 18 && g >= r + 12;
        return lum >= 35 && lum <= 170 && !orangeHair && !shirtBlue;
    }

    private void strengthenEyeComponent(List<List<Map<String, Object>>> data,
                                        EyeComponent component,
                                        Map<String, Object> outlineCell,
                                        Map<String, Object> irisCell,
                                        Map<String, Object> highlightCell) {
        for (int[] point : component.points()) {
            data.get(point[0]).set(point[1], cloneColorCell(outlineCell));
        }

        if (component.size() < 3 || irisCell == null || sameColor(irisCell, outlineCell)) {
            return;
        }

        int centerX = (int) Math.round(component.centerX());
        int centerY = (int) Math.round(component.centerY());
        int minY = component.minY();
        int minX = component.minX();
        int maxY = component.maxY();
        int maxX = component.maxX();
        int painted = 0;
        for (int[] point : component.points()) {
            int distance = Math.abs(point[0] - centerY) + Math.abs(point[1] - centerX);
            boolean interiorColumn = maxX - minX >= 2 && point[1] > minX && point[1] < maxX;
            boolean compactCenter = maxX - minX < 2 && distance <= 1;
            if (interiorColumn || compactCenter) {
                data.get(point[0]).set(point[1], cloneColorCell(irisCell));
                painted++;
            }
        }

        if (painted == 0) {
            int[] nearest = component.points().stream()
                    .min(Comparator.comparingInt(point ->
                            Math.abs(point[0] - centerY) + Math.abs(point[1] - centerX)))
                    .orElse(null);
            if (nearest != null) {
                data.get(nearest[0]).set(nearest[1], cloneColorCell(irisCell));
            }
        }

        paintEyeDetailCell(data, centerY, centerX, irisCell);
        paintEyeDetailCell(data, centerY + 1, centerX, irisCell);
        if (maxX - minX >= 3) {
            paintEyeDetailCell(data, centerY, centerX + 1, irisCell);
        }

        if (component.size() >= 5 && highlightCell != null && colorDistance(highlightCell, outlineCell) > 80) {
            int highlightY = Math.max(minY, centerY - 1);
            int highlightX = Math.max(minX, centerX - 1);
            if (highlightY <= maxY && highlightX <= maxX
                    && highlightY >= 0 && highlightY < data.size()
                    && highlightX >= 0 && highlightX < data.get(highlightY).size()) {
                data.get(highlightY).set(highlightX, cloneColorCell(highlightCell));
            }
        }
    }

    private void paintEyeDetailCell(List<List<Map<String, Object>>> data,
                                    int y,
                                    int x,
                                    Map<String, Object> cell) {
        if (cell == null || y < 0 || y >= data.size() || x < 0 || x >= data.get(y).size()) {
            return;
        }
        data.get(y).set(x, cloneColorCell(cell));
    }

    private void repairEyeCenterByFacePosition(List<List<Map<String, Object>>> data,
                                               FaceBounds face,
                                               boolean leftHalf,
                                               Map<String, Object> outlineCell,
                                               Map<String, Object> highlightCell) {
        EyeAnchor anchor = findEyeAnchor(data, face, leftHalf);
        Map<String, Object> irisCell = chooseSoftEyeIrisCell(data, face, outlineCell);
        if (irisCell == null) {
            return;
        }
        int cx = anchor == null
                ? face.minX() + (int) Math.round(face.width() * (leftHalf ? 0.34 : 0.66))
                : anchor.x();
        int cy = anchor == null
                ? face.minY() + (int) Math.round(face.height() * 0.44)
                : anchor.y();
        int changed = 0;
        for (int y = cy - 1; y <= cy + 1; y++) {
            for (int x = cx - 1; x <= cx + 1; x++) {
                if (y < 0 || y >= data.size() || x < 0 || x >= data.get(y).size() || !face.contains(y, x)) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                if (isSkinCell(cell) || isOrangeHairCell(cell) || luminance(cell) > 155
                        || countSkinNeighbors(data, face, y, x, 2) < 2) {
                    continue;
                }
                if (Math.abs(x - cx) + Math.abs(y - cy) <= 1) {
                    data.get(y).set(x, cloneColorCell(irisCell));
                    changed++;
                }
            }
        }
        if (changed > 0 && highlightCell != null) {
            paintEyeDetailCell(data, cy - 1, cx - 1, highlightCell);
        }
        if (anchor != null) {
            paintReadableEyeShape(data, face, cx, cy, outlineCell, irisCell, highlightCell);
        }
    }

    private void paintReadableEyeShape(List<List<Map<String, Object>>> data,
                                       FaceBounds face,
                                       int cx,
                                       int cy,
                                       Map<String, Object> outlineCell,
                                       Map<String, Object> irisCell,
                                       Map<String, Object> highlightCell) {
        if (outlineCell == null || irisCell == null) {
            return;
        }
        if (countSkinNeighbors(data, face, cy, cx, 2) < 2) {
            return;
        }

        paintFacialFeatureCell(data, face, cy, cx - 1, outlineCell);
        paintFacialFeatureCell(data, face, cy - 1, cx, outlineCell);
        paintFacialFeatureCell(data, face, cy, cx, irisCell);
        paintFacialFeatureCell(data, face, cy, cx + 1, outlineCell);
        if (highlightCell != null && colorDistance(highlightCell, outlineCell) > 80) {
            paintFacialFeatureCell(data, face, cy - 1, cx - 1, highlightCell);
        }
    }

    private void paintFacialFeatureCell(List<List<Map<String, Object>>> data,
                                        FaceBounds face,
                                        int y,
                                        int x,
                                        Map<String, Object> cell) {
        if (cell == null || y < 0 || y >= data.size() || x < 0 || x >= data.get(y).size()
                || !face.contains(y, x)) {
            return;
        }
        if (isOrangeHairCell(data.get(y).get(x)) && countSkinNeighbors(data, face, y, x, 1) < 2) {
            return;
        }
        data.get(y).set(x, cloneColorCell(cell));
    }

    private EyeAnchor findEyeAnchor(List<List<Map<String, Object>>> data, FaceBounds face, boolean leftHalf) {
        int centerX = (face.minX() + face.maxX()) / 2;
        int y0 = face.minY() + (int) Math.round(face.height() * 0.34);
        int y1 = face.minY() + (int) Math.round(face.height() * 0.62);
        int x0 = face.minX() + (int) Math.round(face.width() * (leftHalf ? 0.12 : 0.42));
        int x1 = face.minX() + (int) Math.round(face.width() * (leftHalf ? 0.58 : 0.92));
        double targetY = face.minY() + face.height() * 0.46;
        double targetX = face.minX() + face.width() * (leftHalf ? 0.38 : 0.70);
        EyeAnchor best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int y = Math.max(face.minY(), y0); y <= Math.min(face.maxY(), y1) && y < data.size(); y++) {
            for (int x = Math.max(face.minX(), x0); x <= Math.min(face.maxX(), x1) && x < data.get(y).size(); x++) {
                if (leftHalf ? x >= centerX + 2 : x <= centerX - 2) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                if (!isEyeCandidateCell(face, y, x, cell)) {
                    continue;
                }
                int skinNeighbors = countSkinNeighbors(data, face, y, x, 2);
                if (skinNeighbors < 2) {
                    continue;
                }
                double lum = luminance(cell);
                int r = intValue(cell.get("r"));
                int g = intValue(cell.get("g"));
                int b = intValue(cell.get("b"));
                boolean warmEye = r >= g - 25 && g >= b - 45 && lum >= 55 && lum <= 145;
                double score = (150.0 - lum)
                        + skinNeighbors * 12.0
                        + (warmEye ? 24.0 : 0.0)
                        - Math.abs(y - targetY) * 5.0
                        - Math.abs(x - targetX) * 2.8;
                if (score > bestScore) {
                    bestScore = score;
                    best = new EyeAnchor(x, y);
                }
            }
        }
        return best;
    }

    private int countSkinNeighbors(List<List<Map<String, Object>>> data,
                                   FaceBounds face,
                                   int y,
                                   int x,
                                   int radius) {
        int count = 0;
        for (int yy = y - radius; yy <= y + radius; yy++) {
            if (yy < 0 || yy >= data.size()) {
                continue;
            }
            for (int xx = x - radius; xx <= x + radius && xx < data.get(yy).size(); xx++) {
                if (xx < 0 || (yy == y && xx == x) || !face.contains(yy, xx)) {
                    continue;
                }
                if (isSkinCell(data.get(yy).get(xx))) {
                    count++;
                }
            }
        }
        return count;
    }

    private Map<String, Object> chooseEyeOutlineCell(List<List<Map<String, Object>>> data,
                                                     EyeComponent component,
                                                     Map<String, Object> fallback) {
        Map<String, Object> best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int[] point : component.points()) {
            Map<String, Object> cell = data.get(point[0]).get(point[1]);
            double lum = luminance(cell);
            if (lum < 18 || lum > 95 || isSkinCell(cell) || isOrangeHairCell(cell)) {
                continue;
            }
            double score = 110.0 - lum + (isHarshBlack(cell) ? -25.0 : 0.0);
            if (score > bestScore) {
                bestScore = score;
                best = cell;
            }
        }
        return best != null ? best : fallback;
    }

    private Map<String, Object> chooseSoftEyeIrisCell(List<List<Map<String, Object>>> data,
                                                      FaceBounds face,
                                                      Map<String, Object> outlineCell) {
        Map<String, Object> best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        Set<String> visited = new HashSet<>();
        for (List<Map<String, Object>> row : data) {
            for (Map<String, Object> cell : row) {
                String id = String.valueOf(cell.get("id"));
                if (!visited.add(id) || Boolean.TRUE.equals(cell.get("isExternal"))) {
                    continue;
                }
                int r = intValue(cell.get("r"));
                int g = intValue(cell.get("g"));
                int b = intValue(cell.get("b"));
                double lum = luminance(cell);
                boolean warmNeutral = r >= g - 18 && g >= b - 38 && r >= b + 10;
                boolean tooCloseToOutline = colorDistance(cell, outlineCell) < 35;
                boolean clothingLike = b >= r - 18 && g >= r - 35;
                boolean tooBrightOrange = r > 215 && r - g > 70;
                if (!warmNeutral || lum < 72 || lum > 150 || tooCloseToOutline
                        || isSkinCell(cell) || clothingLike || tooBrightOrange) {
                    continue;
                }
                double score = 100.0
                        - Math.abs(lum - 126.0) * 1.1
                        - Math.abs(r - 170.0) * 0.26
                        - Math.abs(g - 110.0) * 0.22
                        - Math.abs(b - 76.0) * 0.18;
                if (score > bestScore) {
                    bestScore = score;
                    best = cell;
                }
            }
        }
        if (best != null) {
            return best;
        }
        best = bestWarmEyeCellInFace(data, face);
        return best != null && colorDistance(best, outlineCell) >= 35 ? best : null;
    }

    private Map<String, Object> chooseEyeCell(List<List<Map<String, Object>>> data,
                                              FaceBounds face,
                                              EyeComponent component,
                                              Map<String, Object> fallback) {
        Map<String, Object> best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int[] point : component.points()) {
            Map<String, Object> cell = data.get(point[0]).get(point[1]);
            double lum = luminance(cell);
            int r = intValue(cell.get("r"));
            int g = intValue(cell.get("g"));
            int b = intValue(cell.get("b"));
            boolean pureBlack = r < 20 && g < 20 && b < 20;
            boolean coolArtifact = b >= r + 18 && g >= r + 10;
            if (lum < 25 || lum > 145 || isSkinCell(cell) || isOrangeHairCell(cell) || coolArtifact) {
                continue;
            }
            double warmBrown = r >= g - 8 && g >= b - 35 ? 25.0 : 0.0;
            double score = (145.0 - lum) + warmBrown - (pureBlack ? 80.0 : 0.0);
            if (score > bestScore) {
                bestScore = score;
                best = cell;
            }
        }
        if (best != null) {
            return best;
        }
        best = bestWarmEyeCellInFace(data, face);
        if (best != null) {
            return best;
        }
        return fallback;
    }

    private Map<String, Object> bestWarmEyeCellInFace(List<List<Map<String, Object>>> data, FaceBounds face) {
        Map<String, Object> best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        int y0 = face.minY() + (int) Math.round(face.height() * 0.28);
        int y1 = face.minY() + (int) Math.round(face.height() * 0.64);
        for (int y = y0; y <= y1 && y < data.size(); y++) {
            for (int x = face.minX(); x <= face.maxX() && x < data.get(y).size(); x++) {
                if (x < 0) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                double lum = luminance(cell);
                int r = intValue(cell.get("r"));
                int g = intValue(cell.get("g"));
                int b = intValue(cell.get("b"));
                boolean warmDark = lum >= 25 && lum <= 135 && r >= g - 12 && g >= b - 45;
                if (!warmDark || isSkinCell(cell) || isOrangeHairCell(cell)) {
                    continue;
                }
                double score = 150.0 - lum + (r - b) * 0.18;
                if (score > bestScore) {
                    bestScore = score;
                    best = cell;
                }
            }
        }
        return best;
    }

    private Map<String, Object> softWhiteCell(List<List<Map<String, Object>>> data) {
        Map<String, Object> best = null;
        double bestLum = 0;
        for (List<Map<String, Object>> row : data) {
            for (Map<String, Object> cell : row) {
                double lum = luminance(cell);
                int r = intValue(cell.get("r"));
                int g = intValue(cell.get("g"));
                int b = intValue(cell.get("b"));
                if (lum > bestLum && r >= 225 && g >= 225 && b >= 220) {
                    bestLum = lum;
                    best = cell;
                }
            }
        }
        return best;
    }

    private double luminance(Map<String, Object> cell) {
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    private int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private Map<String, Object> findCellById(List<List<Map<String, Object>>> data, String id) {
        if (id == null) {
            return null;
        }
        for (List<Map<String, Object>> row : data) {
            for (Map<String, Object> cell : row) {
                if (id.equals(cell.get("id"))) {
                    return cell;
                }
            }
        }
        return null;
    }

    private Map<String, Object> darkestCell(List<List<Map<String, Object>>> data) {
        Map<String, Object> best = null;
        double bestLum = Double.MAX_VALUE;
        for (List<Map<String, Object>> row : data) {
            for (Map<String, Object> cell : row) {
                if (Boolean.TRUE.equals(cell.get("isExternal"))) {
                    continue;
                }
                double lum = luminance(cell);
                if (lum < bestLum) {
                    bestLum = lum;
                    best = cell;
                }
            }
        }
        return best;
    }

    private Map<String, Object> chooseMouthCell(List<List<Map<String, Object>>> data,
                                                int minX,
                                                int minY,
                                                int maxX,
                                                int maxY) {
        int faceW = maxX - minX + 1;
        int faceH = maxY - minY + 1;
        int centerX = (minX + maxX) / 2;
        int x0 = centerX - Math.max(2, (int) Math.round(faceW * 0.18));
        int x1 = centerX + Math.max(2, (int) Math.round(faceW * 0.18));
        int y0 = minY + (int) Math.round(faceH * 0.55);
        int y1 = minY + (int) Math.round(faceH * 0.82);
        Map<String, Object> best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int y = y0; y <= y1 && y < data.size(); y++) {
            for (int x = x0; x <= x1 && x < data.get(y).size(); x++) {
                if (x < 0) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                double lum = luminance(cell);
                int r = intValue(cell.get("r"));
                int g = intValue(cell.get("g"));
                int b = intValue(cell.get("b"));
                if (lum >= 45 && lum <= 165
                        && r >= g + 6
                        && g >= b - 25
                        && !isSkinCell(cell)
                        && !isOrangeHairCell(cell)) {
                    double centerPenalty = Math.abs(x - centerX) * 5.0
                            + Math.abs(y - (minY + faceH * 0.68)) * 3.0;
                    double score = (170.0 - lum) + (r - b) * 0.35 - centerPenalty;
                    if (score > bestScore) {
                        bestScore = score;
                        best = cell;
                    }
                }
            }
        }
        return best;
    }

    private void stabilizeClothingBlocks(List<List<Map<String, Object>>> data, FaceBounds face) {
        Map<String, Object> clothingCell = dominantClothingCell(data, face);
        if (clothingCell == null) {
            return;
        }

        int rows = data.size();
        int torsoY0 = Math.min(rows - 1, face.maxY() + Math.max(1, (int) Math.round(face.height() * 0.08)));
        int torsoY1 = rows - 1;
        int xPad = Math.max(2, (int) Math.round(face.width() * 0.18));
        int x0 = Math.max(0, face.minX() - xPad);
        int x1 = Math.min(data.get(0).size() - 1, face.maxX() + xPad);

        for (int y = torsoY0; y <= torsoY1; y++) {
            if (y < 0 || y >= rows) {
                continue;
            }
            for (int x = x0; x <= x1 && x < data.get(y).size(); x++) {
                Map<String, Object> cell = data.get(y).get(x);
                if (sameColor(cell, clothingCell) || isLikelyClothingCell(cell, clothingCell)) {
                    continue;
                }
                boolean centralChest = y >= torsoY0 + Math.max(1, (torsoY1 - torsoY0) / 6)
                        && x >= x0 + Math.max(1, (x1 - x0) / 8)
                        && x <= x1 - Math.max(1, (x1 - x0) / 8);
                int clothingNeighbors = countNeighborsMatching(data, y, x,
                        neighbor -> isLikelyClothingCell(neighbor, clothingCell));
                boolean centralColorDrift = centralChest
                        && colorDistance(cell, clothingCell) > 62
                        && clothingNeighbors >= 2;
                boolean artifact = isOrangeHairCell(cell)
                        || isSkinCell(cell)
                        || (isHarshBlack(cell) && clothingNeighbors >= 2)
                        || centralColorDrift;
                if (artifact) {
                    data.get(y).set(x, cloneColorCell(clothingCell));
                }
            }
        }
    }

    private Map<String, Object> dominantClothingCell(List<List<Map<String, Object>>> data, FaceBounds face) {
        int rows = data.size();
        int y0 = Math.min(rows - 1, face.maxY() + Math.max(1, (int) Math.round(face.height() * 0.06)));
        int xPad = Math.max(2, (int) Math.round(face.width() * 0.20));
        int x0 = Math.max(0, face.minX() - xPad);
        int x1 = Math.min(data.get(0).size() - 1, face.maxX() + xPad);
        double centerX = (x0 + x1) / 2.0;
        double halfWidth = Math.max(1.0, (x1 - x0 + 1) / 2.0);
        Map<String, NeighborCount> counts = new LinkedHashMap<>();
        Map<String, Double> scores = new HashMap<>();
        for (int y = y0; y < rows; y++) {
            for (int x = x0; x <= x1 && x < data.get(y).size(); x++) {
                Map<String, Object> cell = data.get(y).get(x);
                if (!isClothingCandidateCell(cell)) {
                    continue;
                }
                String id = String.valueOf(cell.get("id"));
                NeighborCount existing = counts.get(id);
                counts.put(id, existing == null
                        ? new NeighborCount(cell, 1)
                        : new NeighborCount(existing.cell(), existing.count() + 1));
                double centralWeight = Math.max(0.18, 1.0 - Math.abs(x - centerX) / halfWidth);
                scores.merge(id, clothingCandidateScore(cell) * centralWeight, Double::sum);
            }
        }
        return counts.values().stream()
                .max(Comparator.comparingDouble(item -> scores.getOrDefault(String.valueOf(item.cell().get("id")), 0.0)))
                .map(NeighborCount::cell)
                .orElse(null);
    }

    private boolean isClothingCandidateCell(Map<String, Object> cell) {
        if (cell == null || Boolean.TRUE.equals(cell.get("isExternal"))
                || isSkinCell(cell) || isOrangeHairCell(cell) || isHarshBlack(cell)) {
            return false;
        }
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        double lum = luminance(cell);
        boolean coolOrNeutral = b >= r - 20 && g >= r - 35;
        boolean lightNeutral = lum >= 215 && Math.abs(r - g) <= 24 && Math.abs(g - b) <= 24;
        return lum >= 55 && lum <= 250 && (coolOrNeutral || lightNeutral);
    }

    private double clothingCandidateScore(Map<String, Object> cell) {
        double lum = luminance(cell);
        if (lum < 95) {
            return 0.25;
        }
        if (lum > 215) {
            return 1.35;
        }
        return 1.0;
    }

    private boolean isLikelyClothingCell(Map<String, Object> cell, Map<String, Object> clothingCell) {
        return isClothingCandidateCell(cell) && colorDistance(cell, clothingCell) <= 75;
    }

    private Map<String, Object> chooseWarmFeatureCell(List<List<Map<String, Object>>> data,
                                                      int minX,
                                                      int minY,
                                                      int maxX,
                                                      int maxY) {
        Map<String, Object> best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        int faceH = maxY - minY + 1;
        int y0 = minY + (int) Math.round(faceH * 0.40);
        int y1 = minY + (int) Math.round(faceH * 0.86);
        for (int y = y0; y <= y1 && y < data.size(); y++) {
            for (int x = minX; x <= maxX && x < data.get(y).size(); x++) {
                if (x < 0) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                if (isSkinCell(cell)) {
                    continue;
                }
                int r = intValue(cell.get("r"));
                int g = intValue(cell.get("g"));
                int b = intValue(cell.get("b"));
                double lum = luminance(cell);
                if (lum < 60 || lum > 210 || r < g + 12 || g < b - 20 || isOrangeHairCell(cell)) {
                    continue;
                }
                double score = (r - g) + (r - b) * 0.45 - Math.abs(lum - 135) * 0.3;
                if (score > bestScore) {
                    bestScore = score;
                    best = cell;
                }
            }
        }
        return best;
    }

    private Map<String, Object> chooseDarkMouthLineCell(List<List<Map<String, Object>>> data,
                                                        int minX,
                                                        int minY,
                                                        int maxX,
                                                        int maxY) {
        int faceW = maxX - minX + 1;
        int faceH = maxY - minY + 1;
        int centerX = (minX + maxX) / 2;
        int x0 = centerX - Math.max(2, (int) Math.round(faceW * 0.22));
        int x1 = centerX + Math.max(2, (int) Math.round(faceW * 0.22));
        int y0 = minY + (int) Math.round(faceH * 0.56);
        int y1 = minY + (int) Math.round(faceH * 0.80);
        Map<String, Object> best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int y = y0; y <= y1 && y < data.size(); y++) {
            for (int x = x0; x <= x1 && x < data.get(y).size(); x++) {
                if (x < 0) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                if (isSkinCell(cell) || isOrangeHairCell(cell)) {
                    continue;
                }
                int r = intValue(cell.get("r"));
                int g = intValue(cell.get("g"));
                int b = intValue(cell.get("b"));
                double lum = luminance(cell);
                boolean mouthLike = lum >= 35 && lum <= 125 && r >= g - 8 && g >= b - 35;
                if (!mouthLike) {
                    continue;
                }
                double centerPenalty = Math.abs(x - centerX) * 6.0
                        + Math.abs(y - (minY + faceH * 0.68)) * 4.0;
                double score = (140.0 - lum) + (r - b) * 0.25 - centerPenalty;
                if (score > bestScore) {
                    bestScore = score;
                    best = cell;
                }
            }
        }
        return best;
    }

    private MouthAnchor findMouthAnchor(List<List<Map<String, Object>>> data, FaceBounds face) {
        int centerX = (face.minX() + face.maxX()) / 2;
        int y0 = face.minY() + (int) Math.round(face.height() * 0.54);
        int y1 = face.minY() + (int) Math.round(face.height() * 0.78);
        int x0 = centerX - Math.max(2, (int) Math.round(face.width() * 0.24));
        int x1 = centerX + Math.max(2, (int) Math.round(face.width() * 0.24));
        MouthAnchor best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int y = y0; y <= y1 && y < data.size(); y++) {
            for (int x = x0; x <= x1 && x < data.get(y).size(); x++) {
                if (x < 0) {
                    continue;
                }
                Map<String, Object> cell = data.get(y).get(x);
                if (!isMouthAnchorCell(cell)) {
                    continue;
                }
                double score = (180.0 - luminance(cell))
                        - Math.abs(x - centerX) * 7.0
                        - Math.abs(y - (face.minY() + face.height() * 0.66)) * 4.0;
                if (score > bestScore) {
                    bestScore = score;
                    best = new MouthAnchor(x, y);
                }
            }
        }
        return best;
    }

    private boolean isMouthAnchorCell(Map<String, Object> cell) {
        if (isSkinCell(cell) || isOrangeHairCell(cell)) {
            return false;
        }
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        double lum = luminance(cell);
        return lum >= 35 && lum <= 155 && r >= g - 10 && g >= b - 35;
    }

    private void drawReadableMouth(List<List<Map<String, Object>>> data,
                                   int minX,
                                   int minY,
                                   int maxX,
                                   int maxY,
                                   Map<String, Object> skinCell,
                                   Map<String, Object> mouthCell,
                                   MouthAnchor mouthAnchor) {
        int rows = data.size();
        int faceW = maxX - minX + 1;
        int faceH = maxY - minY + 1;
        int centerX = mouthAnchor == null ? (minX + maxX) / 2 : mouthAnchor.x();
        int mouthY = mouthAnchor == null
                ? minY + (int) Math.round(faceH * 0.66)
                : mouthAnchor.y();
        int radius = Math.max(2, Math.min(3, (int) Math.round(faceW * 0.10)));
        int clearY0 = Math.max(minY, mouthY - 1);
        int clearY1 = Math.min(maxY, mouthY + 2);

        for (int y = clearY0; y <= clearY1; y++) {
            if (y < 0 || y >= rows) {
                continue;
            }
            for (int x = centerX - radius - 1; x <= centerX + radius + 1; x++) {
                if (x >= minX && x <= maxX && x >= 0 && x < data.get(y).size()) {
                    data.get(y).set(x, cloneColorCell(skinCell));
                }
            }
        }

        for (int dx = -radius; dx <= radius; dx++) {
            int y = mouthY + (int) Math.round((1.0 - Math.abs(dx) / Math.max(1.0, radius)) * 1.2);
            int x = centerX + dx;
            if (y >= 0 && y < rows && x >= 0 && x < data.get(y).size()) {
                data.get(y).set(x, cloneColorCell(mouthCell));
            }
        }
    }

    private boolean isOrangeHairCell(Map<String, Object> cell) {
        int r = intValue(cell.get("r"));
        int g = intValue(cell.get("g"));
        int b = intValue(cell.get("b"));
        double lum = luminance(cell);
        return r >= 145 && r - g >= 30 && g - b >= 8 && lum >= 90;
    }

    private List<Map<String, Object>> calcColorStats(List<List<Map<String, Object>>> mappedPixelData) {
        Map<String, Map<String, Object>> statsMap = new LinkedHashMap<>();
        for (List<Map<String, Object>> row : mappedPixelData) {
            for (Map<String, Object> cell : row) {
                if (Boolean.TRUE.equals(cell.get("isExternal"))) continue;
                String id = (String) cell.get("id");
                if (id == null) continue;
                if (!statsMap.containsKey(id)) {
                    Map<String, Object> stat = new LinkedHashMap<>();
                    stat.put("id", cell.get("id"));
                    stat.put("name", cell.get("name"));
                    stat.put("hex", cell.get("hex"));
                    stat.put("r", cell.get("r"));
                    stat.put("g", cell.get("g"));
                    stat.put("b", cell.get("b"));
                    stat.put("count", 0);
                    statsMap.put(id, stat);
                }
                Map<String, Object> stat = statsMap.get(id);
                stat.put("count", ((Number) stat.get("count")).intValue() + 1);
            }
        }
        List<Map<String, Object>> list = new ArrayList<>(statsMap.values());
        list.sort((a, b) -> Integer.compare(
                ((Number) b.get("count")).intValue(),
                ((Number) a.get("count")).intValue()));
        return list;
    }

    private List<List<String>> buildGridData(List<List<Map<String, Object>>> mappedPixelData) {
        List<List<String>> gridData = new ArrayList<>();
        for (List<Map<String, Object>> row : mappedPixelData) {
            List<String> idRow = new ArrayList<>();
            for (Map<String, Object> cell : row) {
                idRow.add((String) cell.get("id"));
            }
            gridData.add(idRow);
        }
        return gridData;
    }

    private List<Map<String, Object>> buildColorPalette(List<Map<String, Object>> colorStats) {
        List<Map<String, Object>> palette = new ArrayList<>();
        for (Map<String, Object> stat : colorStats) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", stat.get("id"));
            entry.put("name", stat.get("name"));
            entry.put("hex", stat.get("hex"));
            entry.put("r", stat.get("r"));
            entry.put("g", stat.get("g"));
            entry.put("b", stat.get("b"));
            palette.add(entry);
        }
        return palette;
    }

    private double colorDistance(Map<String, Object> a, Map<String, Object> b) {
        int dr = ((Number) a.get("r")).intValue() - ((Number) b.get("r")).intValue();
        int dg = ((Number) a.get("g")).intValue() - ((Number) b.get("g")).intValue();
        int db = ((Number) a.get("b")).intValue() - ((Number) b.get("b")).intValue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    private boolean sameColor(Map<String, Object> a, Map<String, Object> b) {
        if (a == null || b == null) {
            return false;
        }
        Object aId = a.get("id");
        Object bId = b.get("id");
        return aId != null && aId.equals(bId);
    }

    private List<List<Map<String, Object>>> deepCopyGrid(List<List<Map<String, Object>>> grid) {
        List<List<Map<String, Object>>> copy = new ArrayList<>(grid.size());
        for (List<Map<String, Object>> row : grid) {
            List<Map<String, Object>> newRow = new ArrayList<>(row.size());
            for (Map<String, Object> cell : row) {
                newRow.add(new LinkedHashMap<>(cell));
            }
            copy.add(newRow);
        }
        return copy;
    }

    private static String rgbToHex(int r, int g, int b) {
        return String.format("#%02x%02x%02x",
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b)));
    }

    // ---- 内部数据类 ----

    record MappedResult(
            List<List<Map<String, Object>>> mappedPixelData,
            List<Map<String, Object>> colorStats,
            List<List<String>> gridData,
            List<Map<String, Object>> colorPalette
    ) {}

    public record ProcessedResult(
            List<List<Map<String, Object>>> mappedPixelData,
            List<Map<String, Object>> colorList,
            List<List<String>> gridData,
            List<Map<String, Object>> colorPalette,
            int totalBeads,
            int colorCount
    ) {}

    private record NeighborCount(Map<String, Object> cell, int count) {}

    private record FaceBounds(int minX, int minY, int maxX, int maxY, String dominantSkinId) {
        int width() {
            return maxX - minX + 1;
        }

        int height() {
            return maxY - minY + 1;
        }

        boolean contains(int y, int x) {
            return y >= minY && y <= maxY && x >= minX && x <= maxX;
        }
    }

    private record EyeComponent(List<int[]> points, double centerX, double centerY, double darkness) {
        int size() {
            return points.size();
        }

        int minX() {
            return points.stream().mapToInt(point -> point[1]).min().orElse((int) Math.round(centerX));
        }

        int minY() {
            return points.stream().mapToInt(point -> point[0]).min().orElse((int) Math.round(centerY));
        }

        int maxX() {
            return points.stream().mapToInt(point -> point[1]).max().orElse((int) Math.round(centerX));
        }

        int maxY() {
            return points.stream().mapToInt(point -> point[0]).max().orElse((int) Math.round(centerY));
        }
    }

    private record EyeAnchor(int x, int y) {}

    private record MouthAnchor(int x, int y) {}
}
