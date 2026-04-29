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

    @Select("SELECT id, name, remark FROM bead_palette ORDER BY id")
    List<Map<String, Object>> listPalettes();

    @Select("SELECT COUNT(*) FROM bead_palette WHERE name = #{name}")
    int countPaletteByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM bead_palette WHERE id = #{id}")
    int countPaletteById(@Param("id") Long id);

    @Insert("INSERT INTO bead_palette(name, remark) VALUES(#{name}, #{remark})")
    int insertPalette(@Param("name") String name, @Param("remark") String remark);

    @Update("UPDATE bead_palette SET name = #{name}, remark = #{remark} WHERE id = #{id}")
    int updatePalette(@Param("id") Long id, @Param("name") String name, @Param("remark") String remark);

    @Delete("DELETE FROM bead_palette WHERE id = #{id}")
    int deletePalette(@Param("id") Long id);

    @Select("""
        SELECT id, code, hex, r, g, b
        FROM bead_color
        WHERE (#{q} IS NULL OR #{q} = '' OR code LIKE CONCAT('%', #{q}, '%'))
        ORDER BY code
        """)
    List<Map<String, Object>> listColors(@Param("q") String q);

    @Select("SELECT COUNT(*) FROM bead_color WHERE code = #{code}")
    int countColorByCode(@Param("code") String code);

    @Insert("INSERT INTO bead_color(code, hex, r, g, b) VALUES(#{code}, #{hex}, #{r}, #{g}, #{b})")
    int insertColor(@Param("code") String code,
                    @Param("hex") String hex,
                    @Param("r") int r,
                    @Param("g") int g,
                    @Param("b") int b);

    @Update("UPDATE bead_color SET code = #{code}, hex = #{hex}, r = #{r}, g = #{g}, b = #{b} WHERE id = #{id}")
    int updateColor(@Param("id") Long id,
                    @Param("code") String code,
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
               COALESCE(GROUP_CONCAT(p.name ORDER BY bkp.sort_order SEPARATOR ','), k.palette_ids) AS paletteIds
        FROM bead_brand_kit k
        LEFT JOIN bead_brand b ON b.id = k.brand_id
        LEFT JOIN bead_brand_kit_palette bkp ON bkp.kit_id = k.id
        LEFT JOIN bead_palette p ON p.id = bkp.palette_id
        GROUP BY k.id, k.brand_id, b.name, k.color_count, k.palette_ids
        ORDER BY k.brand_id, k.color_count
        """)
    List<Map<String, Object>> listBrandKits();

    @Select("SELECT id, color_count FROM bead_brand_kit WHERE brand_id = #{brandId} ORDER BY color_count, id")
    List<Map<String, Object>> listKitsByBrandId(@Param("brandId") Long brandId);

    @Select("""
        SELECT p.id, p.name
        FROM bead_brand_kit_palette bkp
        JOIN bead_palette p ON p.id = bkp.palette_id
        WHERE bkp.kit_id = #{kitId}
        ORDER BY bkp.sort_order, p.id
        """)
    List<Map<String, Object>> listPalettesByKitId(@Param("kitId") Long kitId);

    @Select("SELECT c.id, c.code, c.hex, c.r, c.g, c.b FROM bead_palette_color pc JOIN bead_color c ON c.id = pc.color_id WHERE pc.palette_id = #{paletteId} ORDER BY c.code")
    List<Map<String, Object>> listColorsByPaletteId(@Param("paletteId") Integer paletteId);

    @Select("""
        SELECT DISTINCT p.id, p.name
        FROM bead_brand_kit k
        JOIN bead_brand_kit_palette bkp ON bkp.kit_id = k.id
        JOIN bead_palette p ON p.id = bkp.palette_id
        WHERE k.brand_id = #{brandId}
        ORDER BY p.id
        """)
    List<Map<String, Object>> listPalettesByBrandId(@Param("brandId") Long brandId);

    @Select({"<script>",
            "SELECT id FROM bead_color WHERE code IN",
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>",
            "#{code}",
            "</foreach>",
            "</script>"})
    List<Long> listColorIdsByCodes(@Param("codes") List<String> codes);

    @Insert({"<script>",
            "INSERT IGNORE INTO bead_palette_color(palette_id, color_id) VALUES",
            "<foreach collection='colorIds' item='colorId' separator=','>",
            "(#{paletteId}, #{colorId})",
            "</foreach>",
            "</script>"})
    int insertPaletteColorsBatch(@Param("paletteId") Long paletteId,
                                 @Param("colorIds") List<Long> colorIds);
}
