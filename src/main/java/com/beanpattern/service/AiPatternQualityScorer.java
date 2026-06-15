package com.beanpattern.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiPatternQualityScorer {

    public ScoredMetrics score(List<List<Map<String, Object>>> mappedPixelData,
                               int actualColorCount,
                               int targetColorCount,
                               boolean preferredRefined) {
        QualityMetrics metrics = calculateMetrics(mappedPixelData, actualColorCount, targetColorCount);
        double score = 0.0;
        score += 30.0 * metrics.sameNeighborRatio();
        score += 20.0 * metrics.colorReasonableScore();
        score += 20.0 * metrics.subjectFillScore();
        score += 15.0 * metrics.detailBalanceScore();
        score -= 25.0 * metrics.noiseRatio();
        score -= 20.0 * metrics.edgeWhiteRatio();
        score -= 40.0 * metrics.centerSeamPenalty();
        if (preferredRefined) {
            score += 3.0;
        }
        return new ScoredMetrics(round(score), metrics);
    }

    private QualityMetrics calculateMetrics(List<List<Map<String, Object>>> data,
                                            int actualColorCount,
                                            int targetColorCount) {
        int rows = data == null ? 0 : data.size();
        int cols = rows == 0 || data.get(0) == null ? 0 : data.get(0).size();
        if (rows == 0 || cols == 0) {
            return new QualityMetrics(0, 1, 1, 0, 0, 0, 0, 1, actualColorCount);
        }

        int totalCells = 0;
        int whiteCells = 0;
        int edgeCells = 0;
        int edgeWhiteCells = 0;
        int isolatedCells = 0;
        int sameEdges = 0;
        int totalEdges = 0;
        double colorDistanceTotal = 0.0;
        int colorDistanceEdges = 0;

        for (int y = 0; y < rows; y++) {
            List<Map<String, Object>> row = data.get(y);
            if (row == null) continue;
            for (int x = 0; x < row.size(); x++) {
                Map<String, Object> cell = row.get(x);
                if (cell == null || Boolean.TRUE.equals(cell.get("isExternal"))) continue;
                totalCells++;

                boolean white = isWhite(cell);
                if (white) whiteCells++;
                boolean edge = y == 0 || y == rows - 1 || x == 0 || x == row.size() - 1;
                if (edge) {
                    edgeCells++;
                    if (white) edgeWhiteCells++;
                }

                int neighbors = 0;
                int sameNeighbors = 0;
                int[][] directions = new int[][]{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
                for (int[] direction : directions) {
                    Map<String, Object> other = cellAt(data, y + direction[1], x + direction[0]);
                    if (other == null || Boolean.TRUE.equals(other.get("isExternal"))) continue;
                    neighbors++;
                    if (sameColor(cell, other)) {
                        sameNeighbors++;
                    }
                }
                if (neighbors > 0 && sameNeighbors <= 1) {
                    isolatedCells++;
                }

                Map<String, Object> right = cellAt(data, y, x + 1);
                if (right != null && !Boolean.TRUE.equals(right.get("isExternal"))) {
                    totalEdges++;
                    if (sameColor(cell, right)) sameEdges++;
                    colorDistanceTotal += colorDistance(cell, right);
                    colorDistanceEdges++;
                }
                Map<String, Object> down = cellAt(data, y + 1, x);
                if (down != null && !Boolean.TRUE.equals(down.get("isExternal"))) {
                    totalEdges++;
                    if (sameColor(cell, down)) sameEdges++;
                    colorDistanceTotal += colorDistance(cell, down);
                    colorDistanceEdges++;
                }
            }
        }

        double whiteRatio = ratio(whiteCells, totalCells);
        double edgeWhiteRatio = ratio(edgeWhiteCells, edgeCells);
        double noiseRatio = ratio(isolatedCells, totalCells);
        double sameNeighborRatio = ratio(sameEdges, totalEdges);
        double subjectFillScore = clamp01(1.0 - Math.max(0, whiteRatio - 0.18) / 0.55);
        double colorReasonableScore = colorReasonableScore(actualColorCount, targetColorCount);
        double avgDistance = colorDistanceEdges == 0 ? 0 : colorDistanceTotal / colorDistanceEdges;
        double detailBalanceScore = detailBalanceScore(avgDistance);
        double centerSeamPenalty = hasCenterSeam(data) ? 1.0 : 0.0;

        return new QualityMetrics(
                round(sameNeighborRatio),
                round(noiseRatio),
                round(whiteRatio),
                round(edgeWhiteRatio),
                round(colorReasonableScore),
                round(subjectFillScore),
                round(detailBalanceScore),
                centerSeamPenalty,
                actualColorCount
        );
    }

    private double colorReasonableScore(int actualColorCount, int targetColorCount) {
        if (actualColorCount <= 0) return 0.0;
        if (targetColorCount > 0) {
            double score = 1.0 - Math.abs(actualColorCount - targetColorCount) / Math.max(1.0, targetColorCount);
            return clamp01(score);
        }
        if (actualColorCount >= 12 && actualColorCount <= 36) return 1.0;
        if (actualColorCount < 12) return clamp01(actualColorCount / 12.0);
        return clamp01(1.0 - (actualColorCount - 36) / 36.0);
    }

    private double detailBalanceScore(double avgDistance) {
        if (avgDistance <= 0) return 0.0;
        double center = 42.0;
        double tolerance = 55.0;
        return clamp01(1.0 - Math.abs(avgDistance - center) / tolerance);
    }

    private boolean hasCenterSeam(List<List<Map<String, Object>>> data) {
        int rows = data.size();
        int cols = rows == 0 || data.get(0) == null ? 0 : data.get(0).size();
        if (rows < 12 || cols < 12) return false;

        int centerRow = rows / 2;
        int centerCol = cols / 2;
        double rowWhite = lineWhiteRatio(data, centerRow, true);
        double colWhite = lineWhiteRatio(data, centerCol, false);
        double rowPrev = lineWhiteRatio(data, centerRow - 1, true);
        double rowNext = lineWhiteRatio(data, centerRow + 1, true);
        double colPrev = lineWhiteRatio(data, centerCol - 1, false);
        double colNext = lineWhiteRatio(data, centerCol + 1, false);

        return rowWhite >= 0.92
                && colWhite >= 0.92
                && rowWhite - Math.max(rowPrev, rowNext) >= 0.35
                && colWhite - Math.max(colPrev, colNext) >= 0.35;
    }

    private double lineWhiteRatio(List<List<Map<String, Object>>> data, int index, boolean row) {
        int white = 0;
        int total = 0;
        if (row) {
            if (index < 0 || index >= data.size() || data.get(index) == null) return 0;
            for (Map<String, Object> cell : data.get(index)) {
                if (cell == null || Boolean.TRUE.equals(cell.get("isExternal"))) continue;
                total++;
                if (isWhite(cell)) white++;
            }
        } else {
            for (List<Map<String, Object>> cells : data) {
                if (cells == null || index < 0 || index >= cells.size()) continue;
                Map<String, Object> cell = cells.get(index);
                if (cell == null || Boolean.TRUE.equals(cell.get("isExternal"))) continue;
                total++;
                if (isWhite(cell)) white++;
            }
        }
        return ratio(white, total);
    }

    private Map<String, Object> cellAt(List<List<Map<String, Object>>> data, int y, int x) {
        if (y < 0 || y >= data.size()) return null;
        List<Map<String, Object>> row = data.get(y);
        if (row == null || x < 0 || x >= row.size()) return null;
        return row.get(x);
    }

    private boolean sameColor(Map<String, Object> a, Map<String, Object> b) {
        Object aId = a.get("id");
        Object bId = b.get("id");
        return aId != null && aId.equals(bId);
    }

    private boolean isWhite(Map<String, Object> cell) {
        int r = number(cell.get("r"));
        int g = number(cell.get("g"));
        int b = number(cell.get("b"));
        return r >= 245 && g >= 245 && b >= 245;
    }

    private double colorDistance(Map<String, Object> a, Map<String, Object> b) {
        int dr = number(a.get("r")) - number(b.get("r"));
        int dg = number(a.get("g")) - number(b.get("g"));
        int db = number(a.get("b")) - number(b.get("b"));
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    private int number(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private double ratio(int value, int total) {
        return total <= 0 ? 0.0 : (double) value / total;
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    public record ScoredMetrics(double score, QualityMetrics metrics) {}

    public record QualityMetrics(
            double sameNeighborRatio,
            double noiseRatio,
            double whiteRatio,
            double edgeWhiteRatio,
            double colorReasonableScore,
            double subjectFillScore,
            double detailBalanceScore,
            double centerSeamPenalty,
            int actualColorCount
    ) {}
}
