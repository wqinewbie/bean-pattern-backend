package com.beanpattern.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BeadColorMapper {

    /**
     * 查询某品牌某色数套装下的所有色码（含 HEX/RGB）
     * 链路：bead_brand -> bead_brand_kit -> palette_ids -> bead_palette -> bead_palette_color -> bead_color
     */
    @Select("""
        SELECT DISTINCT c.code AS id, c.code AS name, c.r, c.g, c.b
        FROM bead_brand b
        JOIN bead_brand_kit k ON k.brand_id = b.id AND k.color_count = #{colorCount}
        JOIN bead_palette p ON FIND_IN_SET(p.name, k.palette_ids) > 0
        JOIN bead_palette_color pc ON pc.palette_id = p.id
        JOIN bead_color c ON c.id = pc.color_id
        WHERE b.name = #{brand}
        ORDER BY c.code
        """)
    List<Map<String, Object>> queryColorsByBrandAndCount(
            @Param("brand") String brand,
            @Param("colorCount") int colorCount);

    /** 查询某品牌全量颜色（所有套装并集） */
    @Select("""
        SELECT DISTINCT c.code AS id, c.code AS name, c.r, c.g, c.b
        FROM bead_brand b
        JOIN bead_brand_kit k ON k.brand_id = b.id
        JOIN bead_palette p ON FIND_IN_SET(p.name, k.palette_ids) > 0
        JOIN bead_palette_color pc ON pc.palette_id = p.id
        JOIN bead_color c ON c.id = pc.color_id
        WHERE b.name = #{brand}
        ORDER BY c.code
        """)
    List<Map<String, Object>> queryAllColorsByBrand(@Param("brand") String brand);

    /** 查询某品牌所有可用套装的色数列表 */
    @Select("""
        SELECT k.color_count
        FROM bead_brand b
        JOIN bead_brand_kit k ON k.brand_id = b.id
        WHERE b.name = #{brand}
        ORDER BY k.color_count
        """)
    List<Integer> queryKitsByBrand(@Param("brand") String brand);

    /** 查询所有品牌名称 */
    @Select("SELECT name FROM bead_brand ORDER BY id")
    List<String> queryAllBrands();

    /** 检查 bead_color 表是否存在且有数据 */
    @Select("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bead_color'")
    int checkBeadColorTableExists();

    @Select("SELECT COUNT(*) FROM bead_color")
    int countBeadColors();
}
