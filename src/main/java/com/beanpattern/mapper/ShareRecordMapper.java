package com.beanpattern.mapper;

import com.beanpattern.entity.ShareRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 分享记录 Mapper：share_record
 */
@Mapper
public interface ShareRecordMapper {

    @Select("SELECT id, user_id AS userId, share_scene AS shareScene, target_id AS targetId, " +
            "share_ticket AS shareTicket, visit_count AS visitCount, valid_visit_count AS validVisitCount, " +
            "reward_status AS rewardStatus, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_share_record WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT 200")
    List<ShareRecord> findByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, share_scene AS shareScene, target_id AS targetId, " +
            "share_ticket AS shareTicket, visit_count AS visitCount, valid_visit_count AS validVisitCount, " +
            "reward_status AS rewardStatus, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_share_record WHERE id = #{id}")
    ShareRecord findById(@Param("id") Long id);

    @Insert("INSERT INTO bp_share_record(user_id, share_scene, target_id, share_ticket, visit_count, valid_visit_count, reward_status) " +
            "VALUES(#{userId}, #{shareScene}, #{targetId}, #{shareTicket}, 0, 0, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShareRecord record);

    @Update("UPDATE bp_share_record SET visit_count = visit_count + 1 WHERE id = #{id}")
    int incrementVisitCount(@Param("id") Long id);

    @Update("UPDATE bp_share_record SET valid_visit_count = valid_visit_count + 1 WHERE id = #{id}")
    int incrementValidVisitCount(@Param("id") Long id);

    @Update("UPDATE bp_share_record SET reward_status = #{rewardStatus}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateRewardStatus(@Param("id") Long id, @Param("rewardStatus") Integer rewardStatus);

    @Delete("DELETE FROM bp_share_record WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
