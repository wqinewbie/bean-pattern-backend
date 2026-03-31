package com.beanpattern.mapper;

import com.beanpattern.entity.CreatorPatternEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CreatorPatternMapper {

    @Select("SELECT id, user_id AS userId, title, description, cover_url AS coverUrl, " +
            "pattern_url AS patternUrl, grid_size AS gridSize, difficulty, price_coins AS priceCoins, " +
            "download_count AS downloadCount, like_count AS likeCount, status, " +
            "category, tags, created_at AS createdAt " +
            "FROM bp_creator_pattern WHERE status = 1 " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<CreatorPatternEntity> listPublic(@Param("limit") int limit);

    @Select("SELECT id, user_id AS userId, title, description, cover_url AS coverUrl, " +
            "pattern_url AS patternUrl, grid_size AS gridSize, difficulty, price_coins AS priceCoins, " +
            "download_count AS downloadCount, like_count AS likeCount, status, " +
            "reject_reason AS rejectReason, category, tags, created_at AS createdAt " +
            "FROM bp_creator_pattern WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<CreatorPatternEntity> listByUser(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, title, description, cover_url AS coverUrl, " +
            "pattern_url AS patternUrl, grid_size AS gridSize, difficulty, price_coins AS priceCoins, " +
            "download_count AS downloadCount, like_count AS likeCount, status, " +
            "reject_reason AS rejectReason, category, tags, created_at AS createdAt " +
            "FROM bp_creator_pattern ORDER BY created_at DESC LIMIT 1000")
    List<CreatorPatternEntity> listAll();

    @Select("SELECT id, user_id AS userId, title, description, cover_url AS coverUrl, " +
            "pattern_url AS patternUrl, grid_size AS gridSize, difficulty, price_coins AS priceCoins, " +
            "download_count AS downloadCount, like_count AS likeCount, status, " +
            "category, tags, created_at AS createdAt " +
            "FROM bp_creator_pattern WHERE id = #{id}")
    CreatorPatternEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO bp_creator_pattern(user_id, title, description, cover_url, pattern_url, " +
            "grid_size, difficulty, price_coins, category, tags, status) " +
            "VALUES(#{userId}, #{title}, #{description}, #{coverUrl}, #{patternUrl}, " +
            "#{gridSize}, #{difficulty}, #{priceCoins}, #{category}, #{tags}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CreatorPatternEntity pattern);

    @Update("UPDATE bp_creator_pattern SET download_count = download_count + 1 WHERE id = #{id}")
    int incrementDownload(@Param("id") Long id);

    @Update("UPDATE bp_creator_pattern SET status=#{status}, reject_reason=#{rejectReason}, updated_at=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status, @Param("rejectReason") String rejectReason);
}
