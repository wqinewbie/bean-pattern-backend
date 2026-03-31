package com.beanpattern.mapper;

import com.beanpattern.entity.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户表 Mapper（bp_user）
 */
@Mapper
public interface UserMapper {

    @Select("SELECT id, open_id AS openId, union_id AS unionId, nick_name AS nickName, " +
            "avatar_url AS avatarUrl, phone, gender, vip_level AS vipLevel, " +
            "vip_expire_at AS vipExpireAt, magic_coins AS magicCoins, ai_quota AS aiQuota, " +
            "status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user WHERE open_id = #{openId}")
    UserEntity findByOpenId(@Param("openId") String openId);

    @Select("SELECT id, open_id AS openId, union_id AS unionId, nick_name AS nickName, " +
            "avatar_url AS avatarUrl, phone, gender, vip_level AS vipLevel, " +
            "vip_expire_at AS vipExpireAt, magic_coins AS magicCoins, ai_quota AS aiQuota, " +
            "status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user WHERE id = #{id}")
    UserEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO bp_user(open_id) VALUES(#{openId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserEntity user);

    @Update("UPDATE bp_user SET updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int touch(@Param("id") Long id);

    @Update("UPDATE bp_user SET nick_name = #{nickName}, avatar_url = #{avatarUrl}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateProfile(@Param("id") Long id,
                      @Param("nickName") String nickName,
                      @Param("avatarUrl") String avatarUrl);

    @Update("UPDATE bp_user SET phone = #{phone}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updatePhone(@Param("id") Long id, @Param("phone") String phone);

    @Update("UPDATE bp_user SET magic_coins = magic_coins + #{delta}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int addCoins(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE bp_user SET ai_quota = ai_quota + #{delta}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int addAiQuota(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE bp_user SET vip_level = #{vipLevel}, vip_expire_at = #{vipExpireAt}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateVip(@Param("id") Long id,
                  @Param("vipLevel") int vipLevel,
                  @Param("vipExpireAt") java.time.LocalDateTime vipExpireAt);

    @Select("SELECT COUNT(*) FROM bp_user")
    int count();

    @Select("SELECT id, open_id AS openId, nick_name AS nickName, avatar_url AS avatarUrl, " +
            "phone, vip_level AS vipLevel, magic_coins AS magicCoins, ai_quota AS aiQuota, " +
            "status, created_at AS createdAt FROM bp_user ORDER BY created_at DESC LIMIT 10000")
    java.util.List<UserEntity> listAll();

    @Update("UPDATE bp_user SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);
}
