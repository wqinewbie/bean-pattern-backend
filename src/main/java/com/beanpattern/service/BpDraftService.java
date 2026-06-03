package com.beanpattern.service;

import com.beanpattern.entity.BpDraft;
import com.beanpattern.mapper.BpDraftMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BpDraftService {

    private static final int EXPIRE_DAYS = 30;

    private final BpDraftMapper bpDraftMapper;

    public BpDraftService(BpDraftMapper bpDraftMapper) {
        this.bpDraftMapper = bpDraftMapper;
    }

    public int save(BpDraft draft) {
        if (draft.getId() != null) {
            return bpDraftMapper.update(draft);
        }
        return insert(draft);
    }

    public int insert(BpDraft draft) {
        if (draft.getExpiresAt() == null) {
            draft.setExpiresAt(LocalDateTime.now().plusDays(EXPIRE_DAYS));
        }
        return bpDraftMapper.insert(draft);
    }

    public int update(BpDraft draft) {
        return bpDraftMapper.update(draft);
    }

    public int delete(Long id) {
        return bpDraftMapper.deleteById(id);
    }

    public BpDraft getById(Long id) {
        return bpDraftMapper.findById(id);
    }

    public List<BpDraft> listByUserId(Long userId) {
        return bpDraftMapper.listByUserId(userId);
    }

    public List<BpDraft> listByUserId(Long userId, int limit) {
        return bpDraftMapper.listByUserIdWithLimit(userId, limit);
    }

    public List<BpDraft> listByUserIdWithPage(Long userId, int limit, int offset) {
        return bpDraftMapper.listByUserIdWithPage(userId, limit, offset);
    }

    public int countByUserId(Long userId) {
        return bpDraftMapper.countByUserId(userId);
    }

    public int linkBoxId(Long draftId, Long boxId) {
        return bpDraftMapper.linkBoxId(draftId, boxId);
    }

    public int clearBoxId(Long boxId) {
        return bpDraftMapper.clearBoxId(boxId);
    }

    public List<BpDraft> listExpired() {
        return bpDraftMapper.listExpired();
    }

    public int deleteExpired() {
        return bpDraftMapper.deleteExpired();
    }
}
