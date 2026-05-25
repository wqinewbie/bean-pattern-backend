package com.beanpattern.service;

import com.beanpattern.entity.ReviewTaskSubmission;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserTaskProgress;
import com.beanpattern.mapper.ReviewTaskSubmissionMapper;
import com.beanpattern.mapper.TaskConfigMapper;
import com.beanpattern.mapper.UserTaskProgressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 审核型任务服务。
 */
@Service
public class ReviewTaskService {

    private final ReviewTaskSubmissionMapper reviewTaskSubmissionMapper;
    private final TaskConfigMapper taskConfigMapper;
    private final UserTaskProgressMapper userTaskProgressMapper;
    private final NotificationService notificationService;
    private final WechatSubscribeMessageService wechatSubscribeMessageService;

    public ReviewTaskService(ReviewTaskSubmissionMapper reviewTaskSubmissionMapper,
                             TaskConfigMapper taskConfigMapper,
                             UserTaskProgressMapper userTaskProgressMapper,
                             NotificationService notificationService,
                             WechatSubscribeMessageService wechatSubscribeMessageService) {
        this.reviewTaskSubmissionMapper = reviewTaskSubmissionMapper;
        this.taskConfigMapper = taskConfigMapper;
        this.userTaskProgressMapper = userTaskProgressMapper;
        this.notificationService = notificationService;
        this.wechatSubscribeMessageService = wechatSubscribeMessageService;
    }

    public ReviewTaskSubmission getLatestSubmission(Long userId, String taskCode) {
        return reviewTaskSubmissionMapper.findLatestByUserAndTask(userId, taskCode);
    }

    public List<ReviewTaskSubmission> listLatest(int limit) {
        return reviewTaskSubmissionMapper.listLatest(limit);
    }

    @Transactional
    public ReviewTaskSubmission submit(Long userId, String taskCode, String submissionText, String proofImages) {
        TaskConfig config = taskConfigMapper.findByCode(taskCode);
        if (config == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        if (!isReviewTask(config)) {
            throw new IllegalArgumentException("该任务不是审核型任务");
        }
        if (!StringUtils.hasText(proofImages)) {
            throw new IllegalArgumentException("请至少上传一张凭证图片");
        }

        ReviewTaskSubmission latest = reviewTaskSubmissionMapper.findLatestByUserAndTask(userId, taskCode);
        if (latest != null && latest.getStatus() != null && latest.getStatus() == 0) {
            throw new IllegalStateException("已有待审核记录，请勿重复提交");
        }

        ReviewTaskSubmission submission = new ReviewTaskSubmission();
        submission.setUserId(userId);
        submission.setTaskCode(taskCode);
        submission.setSubmissionText(submissionText);
        submission.setProofImages(proofImages);
        submission.setStatus(0);
        reviewTaskSubmissionMapper.insert(submission);
        return submission;
    }

    @Transactional
    public ReviewTaskSubmission review(Long submissionId, Integer status, Long reviewedBy, String reviewRemark) {
        ReviewTaskSubmission submission = reviewTaskSubmissionMapper.findById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("提交记录不存在");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("审核状态非法");
        }
        reviewTaskSubmissionMapper.updateReviewStatus(submissionId, status, reviewedBy, reviewRemark);

        if (status == 1) {
            TaskConfig config = taskConfigMapper.findByCode(submission.getTaskCode());
            if (config != null) {
                markPendingClaim(submission.getUserId(), config);
            }
        } else if (status == 2) {
            String remark = StringUtils.hasText(reviewRemark) ? reviewRemark : "未通过";
            notificationService.createReviewTaskResultNotification(submission.getUserId(), submission.getTaskCode(), "驳回", remark);
            wechatSubscribeMessageService.sendReviewTaskResult(submission.getUserId(), submission.getTaskCode(), "驳回", remark);
        }
        return reviewTaskSubmissionMapper.findById(submissionId);
    }

    private boolean isReviewTask(TaskConfig config) {
        String extra = config.getExtraConfig();
        return extra != null && extra.contains("REVIEW_TASK");
    }

    private void markPendingClaim(Long userId, TaskConfig config) {
        UserTaskProgress progress = userTaskProgressMapper.findByUserAndTaskCodeAndPeriod(
                userId,
                config.getTaskCode(),
                null
        );
        if (progress == null) {
            progress = new UserTaskProgress();
            progress.setUserId(userId);
            progress.setTaskId(config.getId());
            progress.setTaskCode(config.getTaskCode());
            progress.setCurrentCount(1);
            progress.setTargetCount(1);
            progress.setStatus(1);
            progress.setPeriodStart(null);
            userTaskProgressMapper.insert(progress);
        }
        if (progress.getStatus() == null || progress.getStatus() != 2) {
            userTaskProgressMapper.updateProgress(progress.getId(), 1, 1, java.time.LocalDateTime.now());
        }
    }
}
