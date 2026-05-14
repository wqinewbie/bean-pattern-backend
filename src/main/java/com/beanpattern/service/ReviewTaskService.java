package com.beanpattern.service;

import com.beanpattern.entity.ReviewTaskSubmission;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserGift;
import com.beanpattern.mapper.ReviewTaskSubmissionMapper;
import com.beanpattern.mapper.TaskConfigMapper;
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
    private final TaskRewardService taskRewardService;

    public ReviewTaskService(ReviewTaskSubmissionMapper reviewTaskSubmissionMapper,
                             TaskConfigMapper taskConfigMapper,
                             TaskRewardService taskRewardService) {
        this.reviewTaskSubmissionMapper = reviewTaskSubmissionMapper;
        this.taskConfigMapper = taskConfigMapper;
        this.taskRewardService = taskRewardService;
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
    public ReviewTaskSubmission review(Long submissionId, Integer status, String reviewRemark) {
        ReviewTaskSubmission submission = reviewTaskSubmissionMapper.findById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("提交记录不存在");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("审核状态非法");
        }
        reviewTaskSubmissionMapper.updateReviewStatus(submissionId, status, reviewRemark);

        if (status == 1) {
            TaskConfig config = taskConfigMapper.findByCode(submission.getTaskCode());
            if (config != null) {
                taskRewardService.grantTaskPackage(submission.getUserId(), config, "REVIEW_TASK");
            }
        }
        return reviewTaskSubmissionMapper.findById(submissionId);
    }

    private boolean isReviewTask(TaskConfig config) {
        String extra = config.getExtraConfig();
        return extra != null && extra.contains("REVIEW_TASK");
    }
}
