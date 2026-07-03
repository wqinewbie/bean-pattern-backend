package com.beanpattern.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BeadColorMapper {

    /**
     * 查询某品牌某色数套装下的所有色码（含 HEX/RGB）。
     * 优先使用品牌颜色覆盖表，套装与色号使用直接关联表。
     */
    @Select("""
        SELECT
               COALESCE(NULLIF(bco.display_name, ''), c.display_name, c.code) AS id,
               COALESCE(NULLIF(bco.display_name, ''), c.display_name, c.code) AS name,
               COALESCE(bco.r, c.r) AS r,
               COALESCE(bco.g, c.g) AS g,
               COALESCE(bco.b, c.b) AS b
        FROM bead_brand b
        JOIN bead_brand_kit k ON k.brand_id = b.id AND k.color_count = #{colorCount}
        JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
        JOIN bead_color c ON c.id = bkc.color_id
        LEFT JOIN bead_brand_color_override bco ON bco.brand_id = b.id AND bco.color_id = c.id
        WHERE b.name = #{brand}
        ORDER BY bkc.sort_order, c.code
        """)
    List<Map<String, Object>> queryColorsByBrandAndCount(
            @Param("brand") String brand,
            @Param("colorCount") int colorCount);

    /** 查询某品牌全量颜色（优先品牌专属映射；补充套装并集） */
    @Select("""
        SELECT
               COALESCE(NULLIF(bco.display_name, ''), c.display_name, c.code) AS id,
               COALESCE(NULLIF(bco.display_name, ''), c.display_name, c.code) AS name,
               COALESCE(bco.r, c.r) AS r,
               COALESCE(bco.g, c.g) AS g,
               COALESCE(bco.b, c.b) AS b
        FROM bead_brand b
        JOIN bead_brand_kit k ON k.brand_id = b.id
        JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
        JOIN bead_color c ON c.id = bkc.color_id
        LEFT JOIN bead_brand_color_override bco ON bco.brand_id = b.id AND bco.color_id = c.id
        WHERE b.name = #{brand}
        GROUP BY id, name, r, g, b
        ORDER BY id
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
