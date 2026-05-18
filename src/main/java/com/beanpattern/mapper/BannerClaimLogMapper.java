package com.beanpattern.mapper;

import com.beanpattern.entity.BannerClaimLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Banner礼品领取记录 Mapper
 */
@Mapper
public interface BannerClaimLogMapper {

    /**
     * 插入领取记录
     */
    @Insert("INSERT INTO bp_banner_claim_log (user_id, banner_id, banner_code, gift_type, gift_value, claim_date, created_at) " +
            "VALUES (#{userId}, #{bannerId}, #{bannerCode}, #{giftType}, #{giftValue}, #{claimDate}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BannerClaimLog log);

    /**
     * 查询用户是否领取过某个Banner（不限日期）
     */
    @Select("SELECT COUNT(*) FROM bp_banner_claim_log WHERE user_id = #{userId} AND banner_code = #{bannerCode}")
    int countByUserAndBanner(@Param("userId") Long userId, @Param("bannerCode") String bannerCode);

    /**
     * 查询用户今天是否领取过某个Banner
     */
    @Select("SELECT COUNT(*) FROM bp_banner_claim_log " +
            "WHERE user_id = #{userId} AND banner_code = #{bannerCode} AND claim_date = #{claimDate}")
    int countByUserAndBannerAndDate(@Param("userId") Long userId,
                                     @Param("bannerCode") String bannerCode,
                                     @Param("claimDate") LocalDate claimDate);

    /**
     * 查询用户的领取记录
     */
    @Select("SELECT id, user_id AS userId, banner_id AS bannerId, banner_code AS bannerCode, " +
            "gift_type AS giftType, gift_value AS giftValue, claim_date AS claimDate, created_at AS createdAt " +
            "FROM bp_banner_claim_log WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT 200")
    List<BannerClaimLog> findByUserId(@Param("userId") Long userId);
}
