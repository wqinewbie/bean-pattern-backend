package com.beanpattern.service;

import com.beanpattern.entity.BpBox;
import com.beanpattern.mapper.BpBoxMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BpBoxService {

    private final BpBoxMapper bpBoxMapper;

    public BpBoxService(BpBoxMapper bpBoxMapper) {
        this.bpBoxMapper = bpBoxMapper;
    }

    public int save(BpBox box) {
        normalizePixelData(box);
        System.out.println("=== BpBoxService.save() 开始 ===");
        System.out.println("传入的 box: " + box);
        System.out.println("userId: " + box.getUserId() + ", sourceType: " + box.getSourceType());
        System.out.println("gridSize: " + box.getGridSize() + ", colorCount: " + box.getColorCount());
        System.out.println("gridData 长度: " + (box.getGridData() != null ? box.getGridData().length() : 0));
        System.out.println("colorPalette 长度: " + (box.getColorPalette() != null ? box.getColorPalette().length() : 0));

        if (box.getId() != null) {
            System.out.println("更新模式");
            return bpBoxMapper.update(box);
        }

        if (box.getSourceType() == null || box.getSourceType().isBlank() ||
            (!box.getSourceType().equals("LOCAL") && !box.getSourceType().equals("AI") && !box.getSourceType().equals("DRAW"))) {
            box.setSourceType("LOCAL");
        }
        if (box.getStatus() == null) {
            box.setStatus(1);
        }

        System.out.println("设置后的 userId: " + box.getUserId());
        int result = bpBoxMapper.insert(box);
        System.out.println("INSERT 返回值: " + result + ", 生成的 ID: " + box.getId());
        System.out.println("=== BpBoxService.save() 结束 ===");

        return result;
    }

    public int insert(BpBox box) {
        normalizePixelData(box);
        System.out.println("=== BpBoxService.insert() ===");
        System.out.println("userId: " + box.getUserId());

        if (box.getSourceType() == null || box.getSourceType().isBlank() ||
            (!box.getSourceType().equals("LOCAL") && !box.getSourceType().equals("AI") && !box.getSourceType().equals("DRAW"))) {
            box.setSourceType("LOCAL");
        }
        if (box.getStatus() == null) {
            box.setStatus(1);
        }
        return bpBoxMapper.insert(box);
    }

    public int update(BpBox box) {
        normalizePixelData(box);
        return bpBoxMapper.update(box);
    }

    public int delete(Long id) {
        return bpBoxMapper.deleteById(id);
    }

    public BpBox getById(Long id) {
        BpBox box = bpBoxMapper.findById(id);
        hydrateMappedPixelData(box);
        return box;
    }

    public List<BpBox> listByUserId(Long userId) {
        List<BpBox> list = bpBoxMapper.listByUserId(userId);
        if (list != null) {
            list.forEach(this::hydrateMappedPixelData);
        }
        return list;
    }

    public List<BpBox> listByUserId(Long userId, int limit) {
        List<BpBox> list = bpBoxMapper.listByUserIdWithLimit(userId, limit);
        if (list != null) {
            list.forEach(this::hydrateMappedPixelData);
        }
        return list;
    }

    public int countByUserId(Long userId) {
        return bpBoxMapper.countByUserId(userId);
    }

    public int linkBoxId(Long draftOrHistoryId, Long boxId) {
        return bpBoxMapper.linkBoxId(draftOrHistoryId, boxId);
    }

    private void normalizePixelData(BpBox box) {
        if (box == null) return;
        if (box.getMappedPixelData() != null && !box.getMappedPixelData().isBlank()) {
            box.setPixelData(box.getMappedPixelData());
            return;
        }
        if ((box.getMappedPixelData() == null || box.getMappedPixelData().isBlank())
            && box.getPixelData() != null && !box.getPixelData().isBlank()) {
            box.setMappedPixelData(box.getPixelData());
        }
    }

    private void hydrateMappedPixelData(BpBox box) {
        if (box == null) return;
        if ((box.getMappedPixelData() == null || box.getMappedPixelData().isBlank())
            && box.getPixelData() != null && !box.getPixelData().isBlank()) {
            box.setMappedPixelData(box.getPixelData());
        }
    }
}
