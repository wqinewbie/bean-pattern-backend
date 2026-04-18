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
        // 设置 status 默认值：0=处理中 1=已完成 2=已失效
        if (box.getStatus() == null) {
            box.setStatus(1); // 默认设为已完成
        }
        return bpBoxMapper.insert(box);
    }

    public int insert(BpBox box) {
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
