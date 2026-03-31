package com.beanpattern.service;

import com.beanpattern.mapper.BeadColorMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BeadColorService {

    public record BeadColor(String id, String name, int r, int g, int b) {}

    private final BeadColorMapper beadColorMapper;

    // 运行时缓存：key = "brand:colorCount" 或 "brand:all"
    private final Map<String, List<BeadColor>> paletteCache = new ConcurrentHashMap<>();
    private final Map<String, double[][]> labCache = new ConcurrentHashMap<>();

    public BeadColorService(BeadColorMapper beadColorMapper) {
        this.beadColorMapper = beadColorMapper;
    }

    /**
     * 获取指定品牌 + 色数套装的颜色列表
     * colorCount <= 0 表示获取该品牌全量颜色
     */
    public List<BeadColor> getColors(String brand, int colorCount) {
        String key = brand.toLowerCase() + ":" + colorCount;
        return paletteCache.computeIfAbsent(key, k -> loadColors(brand, colorCount));
    }

    /** 兼容旧接口：只传品牌，返回全量颜色 */
    public List<BeadColor> getColors(String brand) {
        return getColors(brand, 0);
    }

    /** 查询某品牌所有可用套装色数 */
    public List<Integer> getKits(String brand) {
        return beadColorMapper.queryKitsByBrand(brand);
    }

    /** 查询所有品牌 */
    public List<String> getBrandNames() {
        return beadColorMapper.queryAllBrands();
    }

    public BeadColor[][] matchGrid(int[][][] rgbGrid, String brand) {
        return matchGrid(rgbGrid, brand, 0, "standard");
    }

    public BeadColor[][] matchGrid(int[][][] rgbGrid, String brand, String algo) {
        return matchGrid(rgbGrid, brand, 0, algo);
    }

    public BeadColor[][] matchGrid(int[][][] rgbGrid, String brand, int colorCount, String algo) {
        String key = brand.toLowerCase() + ":" + colorCount;
        List<BeadColor> palette = paletteCache.computeIfAbsent(key, k -> loadColors(brand, colorCount));
        double[][] labs = labCache.computeIfAbsent(key, k -> buildLabCache(palette));

        double wL, wA, wB;
        switch (algo == null ? "standard" : algo) {
            case "portrait" -> { wL = 1.0; wA = 6.0; wB = 4.0; }
            case "pixel"    -> { wL = 2.5; wA = 2.0; wB = 2.0; }
            default         -> { wL = 1.0; wA = 3.5; wB = 3.5; }
        }

        int rows = rgbGrid.length;
        BeadColor[][] result = new BeadColor[rows][];
        for (int y = 0; y < rows; y++) {
            int cols = rgbGrid[y].length;
            result[y] = new BeadColor[cols];
            for (int x = 0; x < cols; x++)
                result[y][x] = findClosest(
                        rgbGrid[y][x][0], rgbGrid[y][x][1], rgbGrid[y][x][2],
                        palette, labs, wL, wA, wB);
        }
        return result;
    }

    // ---- 私有方法 ----

    private List<BeadColor> loadColors(String brand, int colorCount) {
        List<Map<String, Object>> rows = colorCount > 0
                ? beadColorMapper.queryColorsByBrandAndCount(brand, colorCount)
                : beadColorMapper.queryAllColorsByBrand(brand);
        List<BeadColor> list = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            list.add(new BeadColor(
                    (String) row.get("id"),
                    (String) row.get("name"),
                    toInt(row.get("r")),
                    toInt(row.get("g")),
                    toInt(row.get("b"))
            ));
        }
        return list;
    }

    private double[][] buildLabCache(List<BeadColor> palette) {
        double[][] labs = new double[palette.size()][3];
        for (int i = 0; i < palette.size(); i++)
            labs[i] = rgbToLab(palette.get(i).r(), palette.get(i).g(), palette.get(i).b());
        return labs;
    }

    private int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        return 0;
    }

    private BeadColor findClosest(int r, int g, int b, List<BeadColor> palette,
                                   double[][] labs, double wL, double wA, double wB) {
        double[] src = rgbToLab(r, g, b);
        BeadColor best = palette.get(0);
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < palette.size(); i++) {
            double dL = src[0] - labs[i][0];
            double da = src[1] - labs[i][1];
            double db = src[2] - labs[i][2];
            double d = dL * dL * wL + da * da * wA + db * db * wB;
            if (d < bestDist) { bestDist = d; best = palette.get(i); }
        }
        return best;
    }

    private static double[] rgbToLab(int r, int g, int b) {
        double R = r / 255.0, G = g / 255.0, B = b / 255.0;
        R = R > 0.04045 ? Math.pow((R + 0.055) / 1.055, 2.4) : R / 12.92;
        G = G > 0.04045 ? Math.pow((G + 0.055) / 1.055, 2.4) : G / 12.92;
        B = B > 0.04045 ? Math.pow((B + 0.055) / 1.055, 2.4) : B / 12.92;
        double X = (R * 0.4124 + G * 0.3576 + B * 0.1805) / 0.95047;
        double Y = (R * 0.2126 + G * 0.7152 + B * 0.0722);
        double Z = (R * 0.0193 + G * 0.1192 + B * 0.9505) / 1.08883;
        X = X > 0.008856 ? Math.cbrt(X) : 7.787 * X + 16.0 / 116;
        Y = Y > 0.008856 ? Math.cbrt(Y) : 7.787 * Y + 16.0 / 116;
        Z = Z > 0.008856 ? Math.cbrt(Z) : 7.787 * Z + 16.0 / 116;
        return new double[]{116 * Y - 16, 500 * (X - Y), 200 * (Y - Z)};
    }
}
