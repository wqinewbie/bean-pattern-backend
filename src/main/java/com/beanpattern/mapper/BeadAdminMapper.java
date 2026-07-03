package com.beanpattern.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BeadAdminMapper {

    @Select("SELECT id, name FROM bead_brand ORDER BY id")
    List<Map<String, Object>> listBrands();

    @Select("SELECT COUNT(*) FROM bead_brand WHERE name = #{name}")
    int countBrandByName(@Param("name") String name);

    @Insert("INSERT INTO bead_brand(name) VALUES(#{name})")
    int insertBrand(@Param("name") String name);

    @Update("UPDATE bead_brand SET name = #{name} WHERE id = #{id}")
    int updateBrand(@Param("id") Long id, @Param("name") String name);

    @Delete("DELETE FROM bead_brand WHERE id = #{id}")
    int deleteBrand(@Param("id") Long id);

    @Select("""
        SELECT id,
               code,
               display_name AS displayName,
               hex,
               r,
               g,
               b
        FROM bead_color
        WHERE (#{q} IS NULL OR #{q} = ''
            OR code LIKE CONCAT('%', #{q}, '%')
            OR display_name LIKE CONCAT('%', #{q}, '%')
            OR hex LIKE CONCAT('%', #{q}, '%'))
        ORDER BY code
        """)
    List<Map<String, Object>> listColors(@Param("q") String q);

    @Select("SELECT COUNT(*) FROM bead_color WHERE code = #{code}")
    int countColorByCode(@Param("code") String code);

    @Insert("INSERT INTO bead_color(code, display_name, hex, r, g, b) VALUES(#{code}, #{displayName}, #{hex}, #{r}, #{g}, #{b})")
    int insertColor(@Param("code") String code,
                    @Param("displayName") String displayName,
                    @Param("hex") String hex,
                    @Param("r") int r,
                    @Param("g") int g,
                    @Param("b") int b);

    @Update("UPDATE bead_color SET code = #{code}, display_name = #{displayName}, hex = #{hex}, r = #{r}, g = #{g}, b = #{b} WHERE id = #{id}")
    int updateColor(@Param("id") Long id,
                    @Param("code") String code,
                    @Param("displayName") String displayName,
                    @Param("hex") String hex,
                    @Param("r") int r,
                    @Param("g") int g,
                    @Param("b") int b);

    @Delete("DELETE FROM bead_color WHERE id = #{id}")
    int deleteColor(@Param("id") Long id);

    @Select("""
        SELECT k.id,
               k.brand_id AS brandId,
               b.name AS brandName,
               k.color_count AS colorCount,
               COUNT(DISTINCT bkc.color_id) AS colorTotal
        FROM bead_brand_kit k
        LEFT JOIN bead_brand b ON b.id = k.brand_id
        LEFT JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
        GROUP BY k.id, k.brand_id, b.name, k.color_count
        ORDER BY k.brand_id, k.color_count
        """)
    List<Map<String, Object>> listBrandKits();

    @Select("""
        SELECT k.id,
               k.color_count AS colorCount,
               COUNT(DISTINCT bkc.color_id) AS colorTotal
        FROM bead_brand_kit k
        LEFT JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
        WHERE k.brand_id = #{brandId}
        GROUP BY k.id, k.color_count
        ORDER BY k.color_count, k.id
        """)
    List<Map<String, Object>> listKitsByBrandId(@Param("brandId") Long brandId);

    @Select("""
        SELECT
               c.id,
               c.code,
               COALESCE(NULLIF(bco.display_name, ''), c.display_name, c.code) AS displayName,
               COALESCE(bco.hex, c.hex) AS hex,
               COALESCE(bco.r, c.r) AS r,
               COALESCE(bco.g, c.g) AS g,
               COALESCE(bco.b, c.b) AS b
        FROM bead_brand_kit k
        JOIN bead_brand b ON b.id = k.brand_id
        JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
        JOIN bead_color c ON c.id = bkc.color_id
        LEFT JOIN bead_brand_color_override bco ON bco.brand_id = b.id AND bco.color_id = c.id
        WHERE k.id = #{kitId}
        ORDER BY bkc.sort_order, c.code
        """)
    List<Map<String, Object>> listColorsByKitId(@Param("kitId") Long kitId);

    @Select("""
        SELECT
               c.id,
               c.code,
               COALESCE(NULLIF(bco.display_name, ''), c.display_name, c.code) AS displayName,
               COALESCE(bco.hex, c.hex) AS hex,
               COALESCE(bco.r, c.r) AS r,
               COALESCE(bco.g, c.g) AS g,
               COALESCE(bco.b, c.b) AS b
        FROM bead_brand_kit k
        JOIN bead_brand b ON b.id = k.brand_id
        JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
        JOIN bead_color c ON c.id = bkc.color_id
        LEFT JOIN bead_brand_color_override bco ON bco.brand_id = b.id AND bco.color_id = c.id
        WHERE k.brand_id = #{brandId} AND k.color_count = #{colorCount}
        ORDER BY bkc.sort_order, c.code
        """)
    List<Map<String, Object>> listColorsByBrandAndCount(@Param("brandId") Long brandId,
                                                        @Param("colorCount") Integer colorCount);

    @Select({"<script>",
            "SELECT id FROM bead_color WHERE code IN",
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>",
            "#{code}",
            "</foreach>",
            "</script>"})
    List<Long> listColorIdsByCodes(@Param("codes") List<String> codes);

    @Select("SELECT COUNT(*) FROM bead_brand_kit WHERE id = #{id}")
    int countKitById(@Param("id") Long id);

    @Insert({"<script>",
            "INSERT IGNORE INTO bead_brand_kit_color(kit_id, color_id, sort_order) VALUES",
            "<foreach collection='colorIds' item='colorId' index='index' separator=','>",
            "(#{kitId}, #{colorId}, #{index})",
            "</foreach>",
            "</script>"})
    int insertKitColorsBatch(@Param("kitId") Long kitId,
                             @Param("colorIds") List<Long> colorIds);

    @Delete("DELETE FROM bead_brand_kit_color WHERE kit_id = #{kitId} AND color_id = #{colorId}")
    int deleteKitColor(@Param("kitId") Long kitId, @Param("colorId") Long colorId);

    @Select("""
        SELECT COUNT(*) FROM bead_brand
        """)
    int countBrands();

    @Select("""
        SELECT COUNT(*) FROM bead_brand_kit
        """)
    int countKits();

    @Select("""
        SELECT COUNT(*) FROM bead_color
        """)
    int countColors();

    @Select("""
        SELECT COUNT(*) FROM bead_brand_kit_color
        """)
    int countKitColors();

    @Select("""
        SELECT b.name AS brandName,
               k.id AS kitId,
               k.color_count AS colorCount,
               COUNT(DISTINCT bkc.color_id) AS colorTotal
        FROM bead_brand_kit k
        JOIN bead_brand b ON b.id = k.brand_id
        LEFT JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
        GROUP BY b.name, k.id, k.color_count
        HAVING colorTotal <> colorCount
        ORDER BY b.id, k.color_count
        """)
    List<Map<String, Object>> auditKitCountMismatches();

    @Select("""
        SELECT DISTINCT b.name AS brandName,
               k.id AS kitId,
               k.color_count AS colorCount,
               c.code,
               c.hex,
               c.r,
               c.g,
               c.b
        FROM bead_brand_kit k
        JOIN bead_brand b ON b.id = k.brand_id
        JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
        JOIN bead_color c ON c.id = bkc.color_id
        WHERE c.code LIKE 'VT%'
        ORDER BY b.id, k.color_count, c.code
        """)
    List<Map<String, Object>> auditVirtualKitColors();

    @Select("""
        SELECT id,
               code,
               display_name AS displayName,
               hex,
               r,
               g,
               b
        FROM bead_color
        WHERE hex NOT REGEXP '^#[0-9A-Fa-f]{6}$'
           OR UPPER(REPLACE(hex, '#', '')) <> CONCAT(LPAD(HEX(r), 2, '0'), LPAD(HEX(g), 2, '0'), LPAD(HEX(b), 2, '0'))
           OR r < 0 OR r > 255
           OR g < 0 OR g > 255
           OR b < 0 OR b > 255
        ORDER BY code
        """)
    List<Map<String, Object>> auditBadColorValues();

}
