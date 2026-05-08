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
        if (box.getId() != null) {
            return bpBoxMapper.update(box);
        }

        if (box.getSourceType() == null || box.getSourceType().isBlank() ||
            (!box.getSourceType().equals("LOCAL") && !box.getSourceType().equals("AI") && !box.getSourceType().equals("DRAW"))) {
            box.setSourceType("LOCAL");
        }
        if (box.getStatus() == null) {
            box.setStatus(1);
        }
        if ((box.getCoverUrl() == null || box.getCoverUrl().isBlank()) && box.getSourceUrl() != null && !box.getSourceUrl().isBlank()) {
            box.setCoverUrl(box.getSourceUrl());
        }
        if (box.getFocusCompletedCells() == null) {
            box.setFocusCompletedCells(0);
        }
        if (box.getFocusTotalCells() == null) {
            int size = box.getGridSize() == null ? 1 : box.getGridSize();
            box.setFocusTotalCells(Math.max(1, size * size));
        }
        if (box.getFocusProgress() == null || box.getFocusProgress().isBlank()) {
            box.setFocusProgress("0");
        }

        return bpBoxMapper.insert(box);
    }

    public int insert(BpBox box) {
        if (box.getSourceType() == null || box.getSourceType().isBlank() ||
            (!box.getSourceType().equals("LOCAL") && !box.getSourceType().equals("AI") && !box.getSourceType().equals("DRAW"))) {
            box.setSourceType("LOCAL");
        }
        if (box.getStatus() == null) {
            box.setStatus(1);
        }
        if ((box.getCoverUrl() == null || box.getCoverUrl().isBlank()) && box.getSourceUrl() != null && !box.getSourceUrl().isBlank()) {
            box.setCoverUrl(box.getSourceUrl());
        }
        if (box.getFocusCompletedCells() == null) {
            box.setFocusCompletedCells(0);
        }
        if (box.getFocusTotalCells() == null) {
            int size = box.getGridSize() == null ? 1 : box.getGridSize();
            box.setFocusTotalCells(Math.max(1, size * size));
        }
        if (box.getFocusProgress() == null || box.getFocusProgress().isBlank()) {
            box.setFocusProgress("0");
        }
        return bpBoxMapper.insert(box);
    }

    public int update(BpBox box) {
        return bpBoxMapper.update(box);
    }

    public int updateName(Long id, String name) {
        return bpBoxMapper.updateName(id, name);
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

    public List<BpBox> listByUserIdWithPage(Long userId, int limit, int offset) {
        return bpBoxMapper.listByUserIdWithPage(userId, limit, offset);
    }

    public int countByUserId(Long userId) {
        return bpBoxMapper.countByUserId(userId);
    }
}
