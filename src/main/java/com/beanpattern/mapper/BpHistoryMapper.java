package com.beanpattern.mapper;

import com.beanpattern.entity.BpHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BpHistoryMapper {

    @Insert("""
            INSERT INTO bp_history (user_id, source_type, brand, color_count, name, grid_size,
                                   rgb_data, grid_data, color_palette, box_id, source_url, expires_at)
            VALUES (#{userId}, #{sourceType}, #{brand}, #{colorCount}, #{name}, #{gridSize},
                    #{rgbData}, #{gridData}, #{colorPalette}, #{boxId}, #{sourceUrl}, #{expiresAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpHistory history);

    @Delete("DELETE FROM bp_history WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM bp_history WHERE id=#{id}")
    BpHistory findById(@Param("id") Long id);

    @Select("SELECT * FROM bp_history WHERE user_id=#{userId} ORDER BY created_at DESC")
    List<BpHistory> listByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM bp_history WHERE user_id=#{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<BpHistory> listByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM bp_history WHERE user_id=#{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Update("UPDATE bp_history SET box_id=#{boxId} WHERE id=#{id}")
    int linkBoxId(@Param("id") Long id, @Param("boxId") Long boxId);

    @Select("SELECT * FROM bp_history WHERE expires_at < NOW()")
    List<BpHistory> listExpired();

    @Delete("DELETE FROM bp_history WHERE expires_at < NOW()")
    int deleteExpired();
}
