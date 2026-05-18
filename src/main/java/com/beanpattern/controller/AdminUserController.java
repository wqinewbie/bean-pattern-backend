package com.beanpattern.controller;

import com.beanpattern.config.PasswordEncoder;
import com.beanpattern.entity.AdminEntity;
import com.beanpattern.mapper.AdminMapper;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    private static final String ROLE_ADMIN = "ADMIN";

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(AdminMapper adminMapper, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── 用户管理 ───

    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> users(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(defaultValue = "") String q,
                                                   @RequestParam(defaultValue = "") String vipLevel,
                                                   @RequestParam(defaultValue = "") String status) {
        var all = userMapper.listAll();
        var filtered = all.stream().filter(u -> {
            if (StringUtils.hasText(q)) {
                String nick = u.getNickName() != null ? u.getNickName() : "";
                String phone = u.getPhone() != null ? u.getPhone() : "";
                if (!nick.contains(q) && !phone.contains(q)) return false;
            }
            if (StringUtils.hasText(vipLevel) && !String.valueOf(u.getVipLevel()).equals(vipLevel)) return false;
            if (StringUtils.hasText(status) && !String.valueOf(u.getStatus()).equals(status)) return false;
            return true;
        }).collect(Collectors.toList());

        int total = filtered.size();
        var paged = filtered.stream().skip((long) (page - 1) * pageSize).limit(pageSize)
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("nickName", u.getNickName() != null ? u.getNickName() : "");
                    m.put("avatarUrl", u.getAvatarUrl() != null ? u.getAvatarUrl() : "");
                    m.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    m.put("vipLevel", u.getVipLevel() != null ? u.getVipLevel() : 0);
                    m.put("status", u.getStatus() != null ? u.getStatus() : 1);
                    m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
                    return m;
                }).collect(Collectors.toList());
        return ApiResponse.ok(Map.of("list", paged, "total", total));
    }

    @PostMapping("/users/{id}/toggle-status")
    public ApiResponse<Void> toggleUser(@PathVariable Long id) {
        var user = userMapper.findById(id);
        if (user == null) return ApiResponse.fail("用户不存在");
        userMapper.updateStatus(id, user.getStatus() == 1 ? 0 : 1);
        return ApiResponse.ok(null);
    }

    // ─── 管理员管理 ───

    @GetMapping("/admins")
    public ApiResponse<List<Map<String, Object>>> admins(HttpServletRequest request) {
        Long currentAdminId = (Long) request.getAttribute("adminId");
        return ApiResponse.ok(adminMapper.listAll().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("username", a.getUsername());
            m.put("nickName", a.getNickName() != null ? a.getNickName() : "");
            m.put("role", a.getRole());
            m.put("status", a.getStatus());
            m.put("lastLoginAt", a.getLastLoginAt() != null ? a.getLastLoginAt().toString() : "");
            m.put("isCurrent", currentAdminId != null && currentAdminId.equals(a.getId()));
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/admins")
    public ApiResponse<Void> createAdmin(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "").trim();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password))
            return ApiResponse.fail("账号和密码不能为空");
        if (!isSuperAdmin(request)) return ApiResponse.fail("仅超级管理员可新增管理员");
        if (adminMapper.findByUsername(username) != null) return ApiResponse.fail("账号已存在");

        AdminEntity a = new AdminEntity();
        a.setUsername(username);
        a.setPassword(passwordEncoder.encode(password));
        a.setNickName(body.getOrDefault("nickName", username));
        a.setRole(ROLE_ADMIN);
        a.setStatus(1);
        adminMapper.insert(a);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/admins/{id}")
    public ApiResponse<Void> deleteAdmin(@PathVariable Long id, HttpServletRequest request) {
        var admin = adminMapper.findById(id);
        if (admin == null) return ApiResponse.fail("管理员不存在");
        Long currentAdminId = (Long) request.getAttribute("adminId");
        if (currentAdminId != null && currentAdminId.equals(id))
            return ApiResponse.fail("不能删除当前登录账号");
        if (!isSuperAdmin(request)) return ApiResponse.fail("仅超级管理员可删除管理员");
        if (!ROLE_ADMIN.equalsIgnoreCase(admin.getRole())) return ApiResponse.fail("仅可删除普通管理员");
        if ("admin".equalsIgnoreCase(admin.getUsername())) return ApiResponse.fail("默认管理员不可删除");
        adminMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    private boolean isSuperAdmin(HttpServletRequest request) {
        String currentRole = String.valueOf(request.getAttribute("adminRole"));
        return ROLE_SUPER_ADMIN.equalsIgnoreCase(currentRole);
    }
}
