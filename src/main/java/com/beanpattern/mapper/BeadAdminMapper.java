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
}
