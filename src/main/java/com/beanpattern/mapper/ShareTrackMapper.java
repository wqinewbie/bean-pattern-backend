package com.beanpattern.mapper;

import com.beanpattern.entity.ShareTrack;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ShareTrackMapper {

    String BASE_COLUMNS = "id, sharer_id AS sharerId, visitor_id AS visitorId, " +
            "visitor_openid AS visitorOpenid, task_code AS taskCode, share_date AS shareDate, " +
            "is_valid AS isValid, created_at AS createdAt";

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_share_track WHERE id = #{id}")
    ShareTrack findById(@Param("id") Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_share_track WHERE sharer_id = #{sharerId} ORDER BY created_at DESC")
    List<ShareTrack> findBySharerId(@Param("sharerId") Long sharerId);

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_share_track WHERE sharer_id = #{sharerId} AND share_date = #{date}")
    List<ShareTrack> findBySharerIdAndDate(@Param("sharerId") Long sharerId, @Param("date") LocalDate date);

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_share_track WHERE visitor_id = #{visitorId}")
    List<ShareTrack> findByVisitorId(@Param("visitorId") Long visitorId);

    @Select("SELECT COUNT(*) FROM bp_share_track WHERE sharer_id = #{sharerId} AND share_date = #{date} AND is_valid = 1")
    int countValidBySharerIdAndDate(@Param("sharerId") Long sharerId, @Param("date") LocalDate date);

    @Insert("INSERT INTO bp_share_track(sharer_id, visitor_id, visitor_openid, task_code, share_date, is_valid) " +
            "VALUES(#{sharerId}, #{visitorId}, #{visitorOpenid}, #{taskCode}, #{shareDate}, #{isValid})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShareTrack track);

    @Update("UPDATE bp_share_track SET is_valid = #{isValid} WHERE id = #{id}")
    int updateValid(@Param("id") Long id, @Param("isValid") Boolean isValid);
}
