package com.beanpattern.mapper;

import com.beanpattern.entity.GiftPackage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GiftPackageMapper {

    @Select("SELECT id, package_code AS packageCode, name, description, items_json AS itemsJson, status, sort_order AS sortOrder, created_at AS createdAt, updated_at AS updatedAt FROM bp_gift_package ORDER BY sort_order ASC, id DESC")
    List<GiftPackage> findAll();

    @Select("SELECT id, package_code AS packageCode, name, description, items_json AS itemsJson, status, sort_order AS sortOrder, created_at AS createdAt, updated_at AS updatedAt FROM bp_gift_package WHERE status = 1 ORDER BY sort_order ASC, id DESC")
    List<GiftPackage> findAllActive();

    @Select("SELECT id, package_code AS packageCode, name, description, items_json AS itemsJson, status, sort_order AS sortOrder, created_at AS createdAt, updated_at AS updatedAt FROM bp_gift_package WHERE id = #{id}")
    GiftPackage findById(@Param("id") Long id);

    @Select("SELECT id, package_code AS packageCode, name, description, items_json AS itemsJson, status, sort_order AS sortOrder, created_at AS createdAt, updated_at AS updatedAt FROM bp_gift_package WHERE package_code = #{packageCode}")
    GiftPackage findByCode(@Param("packageCode") String packageCode);

    @Insert("INSERT INTO bp_gift_package(package_code, name, description, items_json, status, sort_order) VALUES(#{packageCode}, #{name}, #{description}, #{itemsJson}, #{status}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GiftPackage giftPackage);

    @Update("UPDATE bp_gift_package SET package_code=#{packageCode}, name=#{name}, description=#{description}, items_json=#{itemsJson}, status=#{status}, sort_order=#{sortOrder}, updated_at=NOW() WHERE id=#{id}")
    int update(GiftPackage giftPackage);

    @Update("UPDATE bp_gift_package SET status = CASE WHEN status=1 THEN 0 ELSE 1 END, updated_at=NOW() WHERE id=#{id}")
    int toggleStatus(@Param("id") Long id);
}
