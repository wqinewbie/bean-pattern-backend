package com.beanpattern.mapper;

import com.beanpattern.entity.ShareVisitor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分享访客 Mapper：share_visitor
 */
@Mapper
public interface ShareVisitorMapper {

    @Select("SELECT id, share_record_id AS shareRecordId, share_user_id AS shareUserId, " +
            "visitor_openid AS visitorOpenid, is_new_user AS isNewUser, " +
            "visit_at AS visitAt, created_at AS createdAt " +
            "FROM bp_share_visitor WHERE share_record_id = #{shareRecordId}")
    List<ShareVisitor> findByShareRecordId(@Param("shareRecordId") Long shareRecordId);

    @Select("SELECT id, share_record_id AS shareRecordId, share_user_id AS shareUserId, " +
            "visitor_openid AS visitorOpenid, is_new_user AS isNewUser, " +
            "visit_at AS visitAt, created_at AS createdAt " +
            "FROM bp_share_visitor WHERE share_record_id = #{shareRecordId} AND visitor_openid = #{visitorOpenid}")
    ShareVisitor findByRecordAndVisitor(@Param("shareRecordId") Long shareRecordId, @Param("visitorOpenid") String visitorOpenid);

    @Select("SELECT DISTINCT visitor_openid AS visitorOpenid " +
            "FROM bp_share_visitor WHERE share_user_id = #{shareUserId} AND is_new_user = 1")
    List<String> findDistinctNewVisitorOpenIdsByShareUserId(@Param("shareUserId") Long shareUserId);

    @Insert("INSERT INTO bp_share_visitor(share_record_id, share_user_id, visitor_openid, is_new_user, visit_at) " +
            "VALUES(#{shareRecordId}, #{shareUserId}, #{visitorOpenid}, #{isNewUser}, #{visitAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShareVisitor visitor);
}
