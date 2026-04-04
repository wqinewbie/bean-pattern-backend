package com.beanpattern.mapper;

import com.beanpattern.entity.AdminEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminMapper {

    @Select("SELECT id, username, password, nick_name AS nickName, role, status, last_login_at AS lastLoginAt, created_at AS createdAt FROM bp_admin WHERE username = #{username}")
    AdminEntity findByUsername(@Param("username") String username);

    @Select("SELECT id, username, nick_name AS nickName, role, status, last_login_at AS lastLoginAt, created_at AS createdAt FROM bp_admin ORDER BY created_at DESC")
    List<AdminEntity> listAll();

    @Insert("INSERT INTO bp_admin(username, password, nick_name, role, status) VALUES(#{username}, #{password}, #{nickName}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdminEntity admin);

    @Update("UPDATE bp_admin SET last_login_at = NOW() WHERE id = #{id}")
    int updateLastLogin(@Param("id") Long id);

    @Update("UPDATE bp_admin SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Select("SELECT id, username, password, nick_name AS nickName, role, status FROM bp_admin WHERE id = #{id}")
    AdminEntity findById(@Param("id") Long id);

    @Delete("DELETE FROM bp_admin WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
