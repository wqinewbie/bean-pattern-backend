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
        return bpBoxMapper.insert(box);
    }

    public int insert(BpBox box) {
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
