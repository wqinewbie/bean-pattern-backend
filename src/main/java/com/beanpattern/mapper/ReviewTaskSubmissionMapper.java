package com.beanpattern.mapper;

import com.beanpattern.entity.ReviewTaskSubmission;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 审核型任务提交记录 Mapper。
 */
@Mapper
public interface ReviewTaskSubmissionMapper {

    String BASE_COLUMNS = "id, user_id AS userId, task_code AS taskCode, " +
            "submission_text AS submissionText, proof_images AS proofImages, status, " +
            "review_remark AS reviewRemark, reviewed_at AS reviewedAt, created_at AS createdAt, updated_at AS updatedAt";

    @Select("SELECT " + BASE_COLUMNS + " " +
            "FROM review_task_submission WHERE user_id = #{userId} AND task_code = #{taskCode} " +
            "ORDER BY created_at DESC LIMIT 1")
    ReviewTaskSubmission findLatestByUserAndTask(@Param("userId") Long userId, @Param("taskCode") String taskCode);

    @Select("SELECT " + BASE_COLUMNS + " " +
            "FROM review_task_submission WHERE id = #{id}")
    ReviewTaskSubmission findById(@Param("id") Long id);

    @Select("SELECT " + BASE_COLUMNS + " " +
            "FROM review_task_submission ORDER BY created_at DESC LIMIT #{limit}")
    List<ReviewTaskSubmission> listLatest(@Param("limit") int limit);

    @Insert("INSERT INTO review_task_submission(user_id, task_code, submission_text, proof_images, status) " +
            "VALUES(#{userId}, #{taskCode}, #{submissionText}, #{proofImages}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReviewTaskSubmission submission);

    @Update("UPDATE review_task_submission SET status = #{status}, review_remark = #{reviewRemark}, " +
            "reviewed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateReviewStatus(@Param("id") Long id,
                           @Param("status") Integer status,
                           @Param("reviewRemark") String reviewRemark);
}
