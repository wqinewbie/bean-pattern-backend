package com.beanpattern.controller;

import com.beanpattern.config.PasswordEncoder;
import com.beanpattern.entity.AdminEntity;
import com.beanpattern.mapper.AdminMapper;
import com.beanpattern.mapper.FeedbackMapper;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.ImageUploadResponse;
import com.beanpattern.model.PageResult;
import com.beanpattern.util.ImageUploadHelper;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final FeedbackMapper feedbackMapper;
    private final PasswordEncoder passwordEncoder;
    private final ImageUploadHelper imageUploadHelper;

    public AdminController(AdminMapper adminMapper, UserMapper userMapper,
                           OrderMapper orderMapper, FeedbackMapper feedbackMapper,
                           PasswordEncoder passwordEncoder, ImageUploadHelper imageUploadHelper) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.feedbackMapper = feedbackMapper;
        this.passwordEncoder = passwordEncoder;
        this.imageUploadHelper = imageUploadHelper;
    }

    // ─── 看板 ───────────────────────────────────────────

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", userMapper.count());
        data.put("totalTasks", 0);
        java.math.BigDecimal income = orderMapper.todayIncome();
        data.put("todayIncome", income != null ? income : java.math.BigDecimal.ZERO);
        long pending = feedbackMapper.listAll().stream()
                .filter(f -> f.getStatus() != null && f.getStatus() == 0)
                .count();
        data.put("pendingFeedback", pending);
        return ApiResponse.ok(data);
    }

    // ─── 登录 ───────────────────────────────────────────

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "").trim();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password))
            return ApiResponse.fail("账号和密码不能为空");

        AdminEntity admin = adminMapper.findByUsername(username);
        if (admin == null || admin.getStatus() == 0)
            return ApiResponse.fail("账号不存在或已禁用");
        if (!passwordEncoder.matches(password, admin.getPassword()))
            return ApiResponse.fail("密码错误");
        if (passwordEncoder.isLegacyMd5(admin.getPassword()))
            adminMapper.updatePassword(admin.getId(), passwordEncoder.encode(password));

        adminMapper.updateLastLogin(admin.getId());
        String token = Base64.getEncoder().encodeToString(
                (username + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        Map<String, Object> adminInfo = new LinkedHashMap<>();
        adminInfo.put("id", admin.getId());
        adminInfo.put("username", admin.getUsername());
        adminInfo.put("nickName", admin.getNickName());
        adminInfo.put("role", admin.getRole());
        data.put("admin", adminInfo);
        return ApiResponse.ok(data);
    }

    // ─── 订单管理 ────────────────────────────────────────

    @GetMapping("/orders")
    public ApiResponse<PageResult<Map<String, Object>>> orders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String status) {
        int total = orderMapper.count();
        int offset = (page - 1) * pageSize;
        var list = orderMapper.listAll(offset, pageSize).stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo() != null ? o.getOrderNo() : "");
            m.put("userId", o.getUserId());
            String userName = "";
            if (o.getUserId() != null) {
                var u = userMapper.findById(o.getUserId());
                if (u != null && u.getNickName() != null) userName = u.getNickName();
                else if (u != null) userName = "用户#" + o.getUserId();
            }
            m.put("userName", userName);
            m.put("planName", o.getPlanName() != null ? o.getPlanName() : "");
            m.put("amount", o.getAmount());
            m.put("status", o.getStatus() != null ? o.getStatus() : "");
            m.put("paidAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : "");
            m.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());
        return ApiResponse.ok(PageResult.of(list, page, pageSize, total));
    }

    // ─── 通用图片上传 ────────────────────────────────────────

    @PostMapping(value = "/image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(imageUploadHelper.uploadImage(file));
    }
}
