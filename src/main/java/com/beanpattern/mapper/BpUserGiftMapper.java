package com.beanpattern.mapper;

import com.beanpattern.entity.BpUserGift;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BpUserGiftMapper {

    String BASE_COLUMNS = "id, user_id AS userId, gift_id AS giftId, gift_type AS giftType, " +
            "gift_name AS giftName, gift_value AS giftValue, gift_config AS giftConfig, " +
            "source, source_id AS sourceId, status, expire_at AS expireAt, used_at AS usedAt, " +
            "order_no AS orderNo, created_at AS createdAt, updated_at AS updatedAt";

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_user_gift WHERE id = #{id}")
    BpUserGift findById(@Param("id") Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_user_gift WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<BpUserGift> findByUserId(@Param("userId") Long userId);

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_user_gift WHERE user_id = #{userId} AND status = #{status} ORDER BY created_at DESC")
    List<BpUserGift> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM bp_user_gift WHERE user_id = #{userId} AND source LIKE CONCAT(#{sourcePrefix}, '%')")
    int countByUserIdAndSourcePrefix(@Param("userId") Long userId, @Param("sourcePrefix") String sourcePrefix);

    @Select("SELECT source FROM bp_user_gift WHERE user_id = #{userId} AND source LIKE CONCAT(#{sourcePrefix}, '%') ORDER BY created_at DESC LIMIT 1")
    String findLastSourceByPrefix(@Param("userId") Long userId, @Param("sourcePrefix") String sourcePrefix);

    @Select("SELECT COUNT(*) FROM bp_user_gift WHERE source = #{source}")
    int countBySource(@Param("source") String source);

    @Select("SELECT COALESCE(SUM(al.reward_value), 0) FROM bp_user_activity_log al " +
            "WHERE al.action_type = 'CLAIM' AND al.activity_code LIKE 'checkin%'")
    int sumCheckinRewardValue();

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_user_gift WHERE user_id = #{userId} AND status = 'UNUSED' " +
            "AND (expire_at IS NULL OR expire_at > NOW()) ORDER BY created_at DESC")
    List<BpUserGift> findAvailableByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO bp_user_gift(user_id, gift_id, gift_type, gift_name, gift_value, gift_config, " +
            "source, source_id, status, expire_at, order_no) " +
            "VALUES(#{userId}, #{giftId}, #{giftType}, #{giftName}, #{giftValue}, #{giftConfig}, " +
            "#{source}, #{sourceId}, #{status}, #{expireAt}, #{orderNo})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpUserGift gift);

    @Update("UPDATE bp_user_gift SET status = 'USED', used_at = NOW(), order_no = #{orderNo}, " +
            "updated_at = NOW() WHERE id = #{id} AND status = 'UNUSED'")
    int use(@Param("id") Long id, @Param("orderNo") String orderNo);

    @Update("UPDATE bp_user_gift SET status = 'EXPIRED', updated_at = NOW() WHERE id = #{id}")
    int expire(@Param("id") Long id);

    @Update("UPDATE bp_user_gift SET status = 'EXPIRED', updated_at = NOW() " +
            "WHERE status = 'UNUSED' AND expire_at IS NOT NULL AND expire_at < NOW()")
    int expireAll();

    @Delete("DELETE FROM bp_user_gift WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
