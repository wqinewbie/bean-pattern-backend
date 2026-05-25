package com.beanpattern.service.task;

import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.ReviewTaskSubmission;
import com.beanpattern.entity.TaskCenterItem;
import com.beanpattern.entity.TaskConfig;
import com.beanpattern.entity.UserTaskProgress;
import com.beanpattern.mapper.UserTaskProgressMapper;
import com.beanpattern.service.GiftPackageService;
import com.beanpattern.service.ReviewTaskService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 社交发帖等审核型任务的任务中心聚合实现。
 */
@Component
public class ReviewTaskHandler implements TaskHandler {

    private final ReviewTaskService reviewTaskService;
    private final GiftPackageService giftPackageService;
    private final UserTaskProgressMapper userTaskProgressMapper;
    private final ObjectMapper objectMapper;

    public ReviewTaskHandler(ReviewTaskService reviewTaskService,
                             GiftPackageService giftPackageService,
                             UserTaskProgressMapper userTaskProgressMapper) {
        this.reviewTaskService = reviewTaskService;
        this.giftPackageService = giftPackageService;
        this.userTaskProgressMapper = userTaskProgressMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(TaskConfig config) {
        String handlerType = readExtraText(config, "handlerType", "");
        return "REVIEW_TASK".equals(handlerType)
                || "social_post_task".equals(config.getTaskCode())
                || "social_post".equals(config.getTaskCode());
    }

    @Override
    public TaskCenterItem buildTaskItem(Long userId, TaskConfig config) {
        ReviewTaskSubmission latest = reviewTaskService.getLatestSubmission(userId, config.getTaskCode());
        UserTaskProgress progress = userTaskProgressMapper.findByUserAndTaskCodeAndPeriod(userId, config.getTaskCode(), null);
        boolean repeatable = "UNLIMITED".equals(config.getTaskType());
        int status = 0;
        String progressText = "提交内容后进入人工审核";
        boolean canClaim = false;
        boolean done = false;

        if (latest != null && latest.getStatus() != null) {
            status = latest.getStatus();
            if (status == 0) {
                progressText = "已提交，待审核";
            } else if (status == 1) {
                int progressStatus = progress != null && progress.getStatus() != null ? progress.getStatus() : 2;
                if (progressStatus == 1) {
                    progressText = "审核通过，待领取奖励";
                    status = 1;
                    canClaim = true;
                } else if (progressStatus == 2) {
                    progressText = repeatable ? "审核通过，奖励已领取，可继续提交" : "审核通过，奖励已领取";
                    status = repeatable ? 0 : 2;
                    done = !repeatable;
                } else {
                    progressText = "审核通过，待领取奖励";
                    status = 1;
                    canClaim = true;
                }
            } else if (status == 2) {
                progressText = StringUtils.hasText(latest.getReviewRemark())
                        ? "审核驳回：" + latest.getReviewRemark()
                        : "审核未通过，请重新提交";
                status = 0;
            }
        }

        String giftPackageCode = readExtraText(config, "giftPackageCode", "");
        GiftPackage giftPackage = StringUtils.hasText(giftPackageCode)
                ? giftPackageService.getByCode(giftPackageCode)
                : null;
        GiftPackageRewardHelper.RewardInfo rewardInfo = GiftPackageRewardHelper.parseGiftPackageReward(giftPackage);

        return TaskCenterItem.builder()
                .taskId(config.getId())
                .taskCode(config.getTaskCode())
                .taskName(config.getTaskName())
                .taskType(config.getTaskType())
                .description(config.getDescription())
                .rewardType(rewardInfo.getDisplayType())
                .rewardValue(rewardInfo.getDisplayValue())
                .rewardItems(rewardInfo.getItems())
                .icon(config.getIcon())
                .sortOrder(config.getSortOrder())
                .handlerType("REVIEW_TASK")
                .bizCategory("REVIEW_TASK")
                .progressText(progressText)
                .status(status)
                .currentCount(latest != null ? 1 : 0)
                .targetCount(1)
                .progressId(progress != null ? progress.getId() : null)
                .done(done)
                .canClaim(canClaim)
                .build();
    }

    private String readExtraText(TaskConfig config, String field, String defaultValue) {
        JsonNode value = readExtraNode(config, field);
        return value == null || value.isNull() ? defaultValue : value.asText(defaultValue);
    }

    private JsonNode readExtraNode(TaskConfig config, String field) {
        if (config == null || !StringUtils.hasText(config.getExtraConfig())) return null;
        try {
            JsonNode root = objectMapper.readTree(config.getExtraConfig());
            return root.get(field);
        } catch (Exception e) {
            return null;
        }
    }
}
