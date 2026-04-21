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
        return bpHistoryMapper.insert(history);
    }

    public int insert(BpHistory history) {
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
        return bpHistoryMapper.findById(id);
    }

    public List<BpHistory> listByUserId(Long userId) {
        return bpHistoryMapper.listByUserId(userId);
    }

    public List<BpHistory> listByUserId(Long userId, int limit) {
        return bpHistoryMapper.listByUserIdWithLimit(userId, limit);
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
}
