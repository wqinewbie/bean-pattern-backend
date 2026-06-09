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
        BeadColor[][] matchedGrid = beadColorService.matchGrid(rgbGrid, brand, colorCount, "pixel");
        MappedResult mapped = convertToMappedPixelData(matchedGrid);

        if (similarityThreshold > 0) {
            mapped = mergeSimilarColors(mapped, similarityThreshold);
        }

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
                rgbGrid[gy][gx] = sampleCellMedian(image, x0, y0, x1, y1);
            }
        }

        return rgbGrid;
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
                rs[count] = (rgb >> 16) & 0xff;
                gs[count] = (rgb >> 8) & 0xff;
                bs[count] = rgb & 0xff;
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
}
