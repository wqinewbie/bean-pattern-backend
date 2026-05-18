package com.beanpattern.mapper;

import com.beanpattern.entity.UserInviteRelation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserInviteRelationMapper {

    @Select("SELECT id, inviter_user_id AS inviterUserId, invitee_user_id AS inviteeUserId, invite_code AS inviteCode, " +
            "status, first_paid_at AS firstPaidAt, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_invite_relation WHERE invitee_user_id = #{inviteeUserId} LIMIT 1")
    UserInviteRelation findByInviteeUserId(@Param("inviteeUserId") Long inviteeUserId);

    @Select("SELECT r.id, r.inviter_user_id AS inviterUserId, r.invitee_user_id AS inviteeUserId, r.invite_code AS inviteCode, " +
            "r.status, r.first_paid_at AS firstPaidAt, r.created_at AS createdAt, r.updated_at AS updatedAt, " +
            "u.nick_name AS inviteeNickName, u.avatar_url AS inviteeAvatarUrl " +
            "FROM bp_user_invite_relation r LEFT JOIN bp_user u ON u.id = r.invitee_user_id " +
            "WHERE r.inviter_user_id = #{inviterUserId} ORDER BY r.created_at DESC")
    List<UserInviteRelation> findByInviterUserId(@Param("inviterUserId") Long inviterUserId);

    @Select("SELECT id, inviter_user_id AS inviterUserId, invitee_user_id AS inviteeUserId, invite_code AS inviteCode, " +
            "status, first_paid_at AS firstPaidAt, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_invite_relation WHERE invite_code = #{inviteCode} AND invitee_user_id = #{inviteeUserId} LIMIT 1")
    UserInviteRelation findByInviteCodeAndInvitee(@Param("inviteCode") String inviteCode,
                                                  @Param("inviteeUserId") Long inviteeUserId);

    @Insert("INSERT INTO bp_user_invite_relation(inviter_user_id, invitee_user_id, invite_code, status) " +
            "VALUES(#{inviterUserId}, #{inviteeUserId}, #{inviteCode}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserInviteRelation relation);

    @Update("UPDATE bp_user_invite_relation SET status = #{status}, first_paid_at = #{firstPaidAt}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatusAndFirstPaidAt(@Param("id") Long id,
                                   @Param("status") Integer status,
                                   @Param("firstPaidAt") LocalDateTime firstPaidAt);
}
