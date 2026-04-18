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
        
        // 设置 sourceType 默认值（必须是有效值：LOCAL, AI, DRAW）
        if (box.getSourceType() == null || box.getSourceType().isBlank() ||
            (!box.getSourceType().equals("LOCAL") && !box.getSourceType().equals("AI") && !box.getSourceType().equals("DRAW"))) {
            box.setSourceType("LOCAL");
        }
        // 设置 status 默认值：0=处理中 1=已完成 2=已失效
        if (box.getStatus() == null) {
            box.setStatus(1); // 默认设为已完成
        }
        
        System.out.println("设置后的 userId: " + box.getUserId());
        int result = bpBoxMapper.insert(box);
        System.out.println("INSERT 返回值: " + result + ", 生成的 ID: " + box.getId());
        System.out.println("=== BpBoxService.save() 结束 ===");
        
        return result;
    }

    public int insert(BpBox box) {
        System.out.println("=== BpBoxService.insert() ===");
        System.out.println("userId: " + box.getUserId());
        
        // 设置 sourceType 默认值（必须是有效值：LOCAL, AI, DRAW）
        if (box.getSourceType() == null || box.getSourceType().isBlank() ||
            (!box.getSourceType().equals("LOCAL") && !box.getSourceType().equals("AI") && !box.getSourceType().equals("DRAW"))) {
            box.setSourceType("LOCAL");
        }
        // 设置 status 默认值：0=处理中 1=已完成 2=已失效
        if (box.getStatus() == null) {
            box.setStatus(1); // 默认设为已完成
        }
        return bpBoxMapper.insert(box);
    }

    public int update(BpBox box) {
        return bpBoxMapper.update(box);
    }

    public int delete(Long id) {
        return bpBoxMapper.deleteById(id);
    }

    public BpBox getById(Long id) {
        return bpBoxMapper.findById(id);
    }

    public List<BpBox> listByUserId(Long userId) {
        return bpBoxMapper.listByUserId(userId);
    }

    public List<BpBox> listByUserId(Long userId, int limit) {
        return bpBoxMapper.listByUserIdWithLimit(userId, limit);
    }

    public int countByUserId(Long userId) {
        return bpBoxMapper.countByUserId(userId);
    }

    public int linkBoxId(Long draftOrHistoryId, Long boxId) {
        return bpBoxMapper.linkBoxId(draftOrHistoryId, boxId);
    }
}
