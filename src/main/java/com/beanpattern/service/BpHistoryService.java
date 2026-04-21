package com.beanpattern.service;

import com.beanpattern.entity.BpHistory;
import com.beanpattern.mapper.BpHistoryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BpHistoryService {

    private static final int EXPIRE_DAYS = 30;

    private final BpHistoryMapper bpHistoryMapper;

    public BpHistoryService(BpHistoryMapper bpHistoryMapper) {
        this.bpHistoryMapper = bpHistoryMapper;
    }

    public int save(BpHistory history) {
        normalizePixelData(history);
        return bpHistoryMapper.insert(history);
    }

    public int insert(BpHistory history) {
        normalizePixelData(history);
        String sourceType = history.getSourceType();
        if (sourceType == null || sourceType.isBlank()) {
            sourceType = "LOCAL";
            history.setSourceType(sourceType);
        }
        if (!"LOCAL".equals(sourceType) && !"AI".equals(sourceType)) {
            sourceType = "LOCAL";
            history.setSourceType(sourceType);
        }
        if (history.getExpiresAt() == null) {
            history.setExpiresAt(LocalDateTime.now().plusDays(EXPIRE_DAYS));
        }
        return bpHistoryMapper.insert(history);
    }

    public int delete(Long id) {
        return bpHistoryMapper.deleteById(id);
    }

    public BpHistory getById(Long id) {
        BpHistory history = bpHistoryMapper.findById(id);
        hydrateMappedPixelData(history);
        return history;
    }

    public List<BpHistory> listByUserId(Long userId) {
        List<BpHistory> list = bpHistoryMapper.listByUserId(userId);
        if (list != null) {
            list.forEach(this::hydrateMappedPixelData);
        }
        return list;
    }

    public List<BpHistory> listByUserId(Long userId, int limit) {
        List<BpHistory> list = bpHistoryMapper.listByUserIdWithLimit(userId, limit);
        if (list != null) {
            list.forEach(this::hydrateMappedPixelData);
        }
        return list;
    }

    public int countByUserId(Long userId) {
        return bpHistoryMapper.countByUserId(userId);
    }

    public int linkBoxId(Long historyId, Long boxId) {
        return bpHistoryMapper.linkBoxId(historyId, boxId);
    }

    public List<BpHistory> listExpired() {
        return bpHistoryMapper.listExpired();
    }

    public int deleteExpired() {
        return bpHistoryMapper.deleteExpired();
    }

    private void normalizePixelData(BpHistory history) {
        if (history == null) return;
        if (history.getMappedPixelData() != null && !history.getMappedPixelData().isBlank()) {
            history.setPixelData(history.getMappedPixelData());
            return;
        }
        if ((history.getMappedPixelData() == null || history.getMappedPixelData().isBlank())
            && history.getPixelData() != null && !history.getPixelData().isBlank()) {
            history.setMappedPixelData(history.getPixelData());
        }
    }

    private void hydrateMappedPixelData(BpHistory history) {
        if (history == null) return;
        if ((history.getMappedPixelData() == null || history.getMappedPixelData().isBlank())
            && history.getPixelData() != null && !history.getPixelData().isBlank()) {
            history.setMappedPixelData(history.getPixelData());
        }
    }
}
