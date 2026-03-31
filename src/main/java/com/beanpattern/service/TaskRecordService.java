package com.beanpattern.service;

import com.beanpattern.entity.ImageTaskEntity;
import com.beanpattern.mapper.ImageTaskMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务记录服务：统一管理图片处理/拼豆图纸的落库记录。
 */
@Service
public class TaskRecordService {

    public static final String TASK_IMAGE_PROCESS = "IMAGE_PROCESS";
    public static final String TASK_BEAD_LOCAL = "BEAD_LOCAL";
    public static final String TASK_BEAD_AI = "BEAD_AI";

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private final ImageTaskMapper imageTaskMapper;

    public TaskRecordService(ImageTaskMapper imageTaskMapper) {
        this.imageTaskMapper = imageTaskMapper;
    }

    public ImageTaskEntity createTask(long userId, String taskType, String sourceUrl) {
        ImageTaskEntity task = new ImageTaskEntity();
        task.setUserId(userId);
        task.setTaskType(taskType);
        task.setSourceUrl(sourceUrl);
        task.setStatus(STATUS_CREATED);
        imageTaskMapper.insert(task);
        return task;
    }

    /**
     * 标记任务成功，同时保存结果图、图纸、颜色统计。
     */
    public void markSuccess(long taskId, String resultUrl, String patternUrl, String colorStats) {
        imageTaskMapper.updateResult(taskId, resultUrl, patternUrl, colorStats, STATUS_SUCCESS, null);
    }

    /**
     * 兼容旧调用（AI生成只有 resultUrl）
     */
    public void markSuccess(long taskId, String resultUrl) {
        markSuccess(taskId, resultUrl, null, null);
    }

    public void markFailed(long taskId, String errorMessage) {
        imageTaskMapper.updateResult(taskId, null, null, null, STATUS_FAILED, errorMessage);
    }

    public ImageTaskEntity findById(long taskId) {
        return imageTaskMapper.findById(taskId);
    }

    public List<ImageTaskEntity> listByUser(long userId, int limit) {
        return imageTaskMapper.listByUser(userId, limit);
    }

    public void markSaved(long taskId, boolean saved) {
        imageTaskMapper.updateIsSaved(taskId, saved ? 1 : 0);
    }

    public List<ImageTaskEntity> listSavedByUser(long userId, int limit) {
        return imageTaskMapper.listSavedByUser(userId, limit);
    }

    /** 数据库分页查询（替代内存分页） */
    public List<ImageTaskEntity> listByUserPage(long userId, int offset, int size) {
        return imageTaskMapper.listByUserPage(userId, offset, size);
    }

    /** 统计用户任务总数（用于分页） */
    public int countByUser(long userId) {
        return imageTaskMapper.countByUser(userId);
    }
}
