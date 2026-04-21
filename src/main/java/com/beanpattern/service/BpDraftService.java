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
        normalizePixelData(draft);
        if (draft.getId() != null) {
            return bpDraftMapper.update(draft);
        }
        return bpDraftMapper.insert(draft);
    }

    public int insert(BpDraft draft) {
        normalizePixelData(draft);
        if (draft.getExpiresAt() == null) {
            draft.setExpiresAt(LocalDateTime.now().plusDays(EXPIRE_DAYS));
        }
        return bpDraftMapper.insert(draft);
    }

    public int update(BpDraft draft) {
        normalizePixelData(draft);
        return bpDraftMapper.update(draft);
    }

    public int delete(Long id) {
        return bpDraftMapper.deleteById(id);
    }

    public BpDraft getById(Long id) {
        BpDraft draft = bpDraftMapper.findById(id);
        hydrateMappedPixelData(draft);
        return draft;
    }

    public List<BpDraft> listByUserId(Long userId) {
        List<BpDraft> list = bpDraftMapper.listByUserId(userId);
        if (list != null) {
            list.forEach(this::hydrateMappedPixelData);
        }
        return list;
    }

    public List<BpDraft> listByUserId(Long userId, int limit) {
        List<BpDraft> list = bpDraftMapper.listByUserIdWithLimit(userId, limit);
        if (list != null) {
            list.forEach(this::hydrateMappedPixelData);
        }
        return list;
    }

    public int countByUserId(Long userId) {
        return bpDraftMapper.countByUserId(userId);
    }

    public int linkBoxId(Long draftId, Long boxId) {
        return bpDraftMapper.linkBoxId(draftId, boxId);
    }

    public List<BpDraft> listExpired() {
        return bpDraftMapper.listExpired();
    }

    public int deleteExpired() {
        return bpDraftMapper.deleteExpired();
    }

    private void normalizePixelData(BpDraft draft) {
        if (draft == null) return;
        if (draft.getMappedPixelData() != null && !draft.getMappedPixelData().isBlank()) {
            draft.setPixelData(draft.getMappedPixelData());
            return;
        }
        if ((draft.getMappedPixelData() == null || draft.getMappedPixelData().isBlank())
            && draft.getPixelData() != null && !draft.getPixelData().isBlank()) {
            draft.setMappedPixelData(draft.getPixelData());
        }
    }

    private void hydrateMappedPixelData(BpDraft draft) {
        if (draft == null) return;
        if ((draft.getMappedPixelData() == null || draft.getMappedPixelData().isBlank())
            && draft.getPixelData() != null && !draft.getPixelData().isBlank()) {
            draft.setMappedPixelData(draft.getPixelData());
        }
    }
}
