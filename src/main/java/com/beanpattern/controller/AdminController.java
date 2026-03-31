package com.beanpattern.controller;

import com.beanpattern.config.PasswordEncoder;
import com.beanpattern.entity.*;
import com.beanpattern.mapper.*;
import com.beanpattern.model.ApiResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理后台专用接口 /api/admin/**
 * 通过请求头 Authorization: Bearer {token} 验证身份
 * token = base64(username:timestamp:sign)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final BannerMapper bannerMapper;
    private final FeedbackMapper feedbackMapper;
    private final CreatorPatternMapper creatorPatternMapper;
    private final ImageTaskMapper imageTaskMapper;
    private final OrderMapper orderMapper;
    private final RechargePlanMapper rechargePlanMapper;
    private final BeadAdminMapper beadAdminMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminMapper adminMapper, UserMapper userMapper,
                           BannerMapper bannerMapper, FeedbackMapper feedbackMapper,
                           CreatorPatternMapper creatorPatternMapper,
                           ImageTaskMapper imageTaskMapper, OrderMapper orderMapper,
                           RechargePlanMapper rechargePlanMapper,
                           BeadAdminMapper beadAdminMapper,
                           PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.bannerMapper = bannerMapper;
        this.feedbackMapper = feedbackMapper;
        this.creatorPatternMapper = creatorPatternMapper;
        this.imageTaskMapper = imageTaskMapper;
        this.orderMapper = orderMapper;
        this.rechargePlanMapper = rechargePlanMapper;
        this.beadAdminMapper = beadAdminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── 看板 ───────────────────────────────────────────

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", userMapper.count());
        data.put("totalTasks", imageTaskMapper.count());
        java.math.BigDecimal income = orderMapper.todayIncome();
        data.put("todayIncome", income != null ? income : java.math.BigDecimal.ZERO);
        long pending = feedbackMapper.listAll().stream()
                .filter(f -> f.getStatus() != null && f.getStatus() == 0)
                .count();
        data.put("pendingFeedback", pending);
        return ApiResponse.ok(data);
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "").trim();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return ApiResponse.fail("账号和密码不能为空");
        }
        AdminEntity admin = adminMapper.findByUsername(username);
        if (admin == null || admin.getStatus() == 0) {
            return ApiResponse.fail("账号不存在或已禁用");
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return ApiResponse.fail("密码错误");
        }
        // 登录成功后，自动将旧 MD5 密码升级为 BCrypt
        if (passwordEncoder.isLegacyMd5(admin.getPassword())) {
            adminMapper.updatePassword(admin.getId(), passwordEncoder.encode(password));
        }
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

    // ─── 用户管理 ────────────────────────────────────────

    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> users(
            @RequestParam(defaultValue = "1") int page,
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
        int from = (page - 1) * pageSize;
        var paged = filtered.stream().skip(from).limit(pageSize).map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("nickName", u.getNickName() != null ? u.getNickName() : "");
            m.put("phone", u.getPhone() != null ? u.getPhone() : "");
            m.put("magicCoins", u.getMagicCoins() != null ? u.getMagicCoins() : 0);
            m.put("aiQuota", u.getAiQuota() != null ? u.getAiQuota() : 0);
            m.put("vipLevel", u.getVipLevel() != null ? u.getVipLevel() : 0);
            m.put("status", u.getStatus() != null ? u.getStatus() : 1);
            m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());
        return ApiResponse.ok(Map.of("list", paged, "total", total));
    }

    @PostMapping("/users/{id}/toggle-status")
    public ApiResponse<String> toggleUserStatus(@PathVariable Long id) {
        var user = userMapper.findById(id);
        if (user == null) return ApiResponse.fail("用户不存在");
        userMapper.updateStatus(id, user.getStatus() == 1 ? 0 : 1);
        return ApiResponse.ok("ok");
    }

    // ─── 图纸管理 ────────────────────────────────────────

    @GetMapping("/patterns")
    public ApiResponse<Map<String, Object>> patterns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String status) {
        var all = creatorPatternMapper.listAll();
        var filtered = all.stream().filter(p ->
                !StringUtils.hasText(status) || String.valueOf(p.getStatus()).equals(status)
        ).collect(Collectors.toList());
        int total = filtered.size();
        var paged = filtered.stream().skip((long)(page-1)*pageSize).limit(pageSize)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("title", p.getTitle());
                    m.put("coverUrl", p.getCoverUrl() != null ? p.getCoverUrl() : "");
                    m.put("category", p.getCategory() != null ? p.getCategory() : "");
                    m.put("priceCoins", p.getPriceCoins());
                    m.put("downloadCount", p.getDownloadCount());
                    m.put("status", p.getStatus());
                    m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
                    return m;
                }).collect(Collectors.toList());
        return ApiResponse.ok(Map.of("list", paged, "total", total));
    }

    @PostMapping("/patterns/{id}/approve")
    public ApiResponse<String> approvePattern(@PathVariable Long id) {
        creatorPatternMapper.updateStatus(id, 1, null);
        return ApiResponse.ok("ok");
    }

    @PostMapping("/patterns/{id}/reject")
    public ApiResponse<String> rejectPattern(@PathVariable Long id, @RequestBody Map<String, String> body) {
        creatorPatternMapper.updateStatus(id, 3, body.getOrDefault("reason", ""));
        return ApiResponse.ok("ok");
    }

    @PostMapping("/patterns/{id}/toggle-online")
    public ApiResponse<String> togglePattern(@PathVariable Long id) {
        var p = creatorPatternMapper.findById(id);
        if (p == null) return ApiResponse.fail("图纸不存在");
        creatorPatternMapper.updateStatus(id, p.getStatus() == 1 ? 2 : 1, null);
        return ApiResponse.ok("ok");
    }

    // ─── Banner管理 ──────────────────────────────────────

    @GetMapping("/banners")
    public ApiResponse<List<Map<String, Object>>> adminBanners() {
        return ApiResponse.ok(bannerMapper.listAll().stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("title", b.getTitle());
            m.put("subTitle", b.getSubTitle() != null ? b.getSubTitle() : "");
            m.put("imageUrl", b.getImageUrl() != null ? b.getImageUrl() : "");
            m.put("tagText", b.getTagText() != null ? b.getTagText() : "");
            m.put("linkType", b.getLinkType() != null ? b.getLinkType() : "NONE");
            m.put("linkValue", b.getLinkValue() != null ? b.getLinkValue() : "");
            m.put("sortOrder", b.getSortOrder());
            m.put("status", b.getStatus());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/banners")
    public ApiResponse<String> createBanner(@RequestBody Map<String, Object> body) {
        BannerEntity b = new BannerEntity();
        b.setTitle((String) body.getOrDefault("title", ""));
        b.setSubTitle((String) body.getOrDefault("subTitle", ""));
        b.setImageUrl((String) body.getOrDefault("imageUrl", ""));
        b.setTagText((String) body.getOrDefault("tagText", ""));
        b.setLinkType((String) body.getOrDefault("linkType", "NONE"));
        b.setLinkValue((String) body.getOrDefault("linkValue", ""));
        b.setSortOrder(body.get("sortOrder") instanceof Number n ? n.intValue() : 0);
        b.setStatus(1);
        bannerMapper.insert(b);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/banners/{id}")
    public ApiResponse<String> updateBanner(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        bannerMapper.update(id,
                (String) body.getOrDefault("title", ""),
                (String) body.getOrDefault("subTitle", ""),
                (String) body.getOrDefault("imageUrl", ""),
                (String) body.getOrDefault("tagText", ""),
                body.get("sortOrder") instanceof Number n ? n.intValue() : 0,
                (String) body.getOrDefault("linkType", "NONE"),
                (String) body.getOrDefault("linkValue", ""));
        return ApiResponse.ok("ok");
    }

    @PostMapping("/banners/{id}/toggle")
    public ApiResponse<String> toggleBanner(@PathVariable Long id) {
        bannerMapper.toggleStatus(id);
        return ApiResponse.ok("ok");
    }

    // ─── 拼豆品牌 / 色盘 / 色号管理 ─────────────────────────

    @GetMapping("/bead/brands")
    public ApiResponse<List<Map<String, Object>>> adminBeadBrands() {
        return ApiResponse.ok(beadAdminMapper.listBrands());
    }

    @PostMapping("/bead/brands")
    public ApiResponse<String> createBeadBrand(@RequestBody Map<String, Object> body) {
        String name = ((String) body.getOrDefault("name", "")).trim();
        if (!StringUtils.hasText(name)) return ApiResponse.fail("品牌名不能为空");
        if (beadAdminMapper.countBrandByName(name) > 0) return ApiResponse.fail("品牌已存在");
        beadAdminMapper.insertBrand(name);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/bead/brands/{id}")
    public ApiResponse<String> updateBeadBrand(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = ((String) body.getOrDefault("name", "")).trim();
        if (!StringUtils.hasText(name)) return ApiResponse.fail("品牌名不能为空");
        beadAdminMapper.updateBrand(id, name);
        return ApiResponse.ok("ok");
    }

    @DeleteMapping("/bead/brands/{id}")
    public ApiResponse<String> deleteBeadBrand(@PathVariable Long id) {
        beadAdminMapper.deleteBrand(id);
        return ApiResponse.ok("ok");
    }

    @GetMapping("/bead/palettes")
    public ApiResponse<List<Map<String, Object>>> adminBeadPalettes() {
        return ApiResponse.ok(beadAdminMapper.listPalettes());
    }

    @PostMapping("/bead/palettes")
    public ApiResponse<String> createBeadPalette(@RequestBody Map<String, Object> body) {
        String name = ((String) body.getOrDefault("name", "")).trim();
        String remark = ((String) body.getOrDefault("remark", "")).trim();
        if (!StringUtils.hasText(name)) return ApiResponse.fail("色盘名不能为空");
        if (beadAdminMapper.countPaletteByName(name) > 0) return ApiResponse.fail("色盘已存在");
        beadAdminMapper.insertPalette(name, remark);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/bead/palettes/{id}")
    public ApiResponse<String> updateBeadPalette(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = ((String) body.getOrDefault("name", "")).trim();
        String remark = ((String) body.getOrDefault("remark", "")).trim();
        if (!StringUtils.hasText(name)) return ApiResponse.fail("色盘名不能为空");
        beadAdminMapper.updatePalette(id, name, remark);
        return ApiResponse.ok("ok");
    }

    @DeleteMapping("/bead/palettes/{id}")
    public ApiResponse<String> deleteBeadPalette(@PathVariable Long id) {
        beadAdminMapper.deletePalette(id);
        return ApiResponse.ok("ok");
    }

    @GetMapping("/bead/colors")
    public ApiResponse<List<Map<String, Object>>> adminBeadColors(@RequestParam(defaultValue = "") String q) {
        return ApiResponse.ok(beadAdminMapper.listColors(q));
    }

    @PostMapping("/bead/colors")
    public ApiResponse<String> createBeadColor(@RequestBody Map<String, Object> body) {
        String code = ((String) body.getOrDefault("code", "")).trim();
        String hex = ((String) body.getOrDefault("hex", "")).trim().toUpperCase();
        int r = body.get("r") instanceof Number n ? n.intValue() : 0;
        int g = body.get("g") instanceof Number n ? n.intValue() : 0;
        int b = body.get("b") instanceof Number n ? n.intValue() : 0;
        if (!StringUtils.hasText(code)) return ApiResponse.fail("色号不能为空");
        if (!StringUtils.hasText(hex)) return ApiResponse.fail("HEX不能为空");
        if (beadAdminMapper.countColorByCode(code) > 0) return ApiResponse.fail("色号已存在");
        beadAdminMapper.insertColor(code, hex, r, g, b);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/bead/colors/{id}")
    public ApiResponse<String> updateBeadColor(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String code = ((String) body.getOrDefault("code", "")).trim();
        String hex = ((String) body.getOrDefault("hex", "")).trim().toUpperCase();
        int r = body.get("r") instanceof Number n ? n.intValue() : 0;
        int g = body.get("g") instanceof Number n ? n.intValue() : 0;
        int b = body.get("b") instanceof Number n ? n.intValue() : 0;
        if (!StringUtils.hasText(code)) return ApiResponse.fail("色号不能为空");
        if (!StringUtils.hasText(hex)) return ApiResponse.fail("HEX不能为空");
        beadAdminMapper.updateColor(id, code, hex, r, g, b);
        return ApiResponse.ok("ok");
    }

    @DeleteMapping("/bead/colors/{id}")
    public ApiResponse<String> deleteBeadColor(@PathVariable Long id) {
        beadAdminMapper.deleteColor(id);
        return ApiResponse.ok("ok");
    }

    // ─── 反馈管理 ────────────────────────────────────────

    @GetMapping("/feedback")
    public ApiResponse<List<Map<String, Object>>> adminFeedback() {
        return ApiResponse.ok(feedbackMapper.listAll().stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("user", f.getUserId() != null ? "用户#" + f.getUserId() : "匿名");
            m.put("content", f.getContent());
            m.put("category", f.getCategory());
            m.put("status", f.getStatus());
            m.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/feedback/{id}/close")
    public ApiResponse<String> closeFeedback(@PathVariable Long id) {
        feedbackMapper.updateStatus(id, 3);
        return ApiResponse.ok("ok");
    }

    // ─── 管理员管理 ──────────────────────────────────────

    @GetMapping("/admins")
    public ApiResponse<List<Map<String, Object>>> listAdmins() {
        return ApiResponse.ok(adminMapper.listAll().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("username", a.getUsername());
            m.put("nickName", a.getNickName() != null ? a.getNickName() : "");
            m.put("role", a.getRole());
            m.put("status", a.getStatus());
            m.put("lastLoginAt", a.getLastLoginAt() != null ? a.getLastLoginAt().toString() : "");
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/admins")
    public ApiResponse<String> createAdmin(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "").trim();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) return ApiResponse.fail("账号和密码不能为空");
        if (adminMapper.findByUsername(username) != null) return ApiResponse.fail("账号已存在");
        AdminEntity a = new AdminEntity();
        a.setUsername(username);
        a.setPassword(passwordEncoder.encode(password));
        a.setNickName(body.getOrDefault("nickName", username));
        adminMapper.insert(a);
        return ApiResponse.ok("ok");
    }

    // ─── 订单管理 ────────────────────────────────────────

    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> orders(
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
            // 查用户昵称
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
        return ApiResponse.ok(Map.of("list", list, "total", total));
    }

    // ─── VIP套餐管理 ─────────────────────────────────────

    @GetMapping("/vip-plans")
    public ApiResponse<List<Map<String, Object>>> vipPlans() {
        var list = rechargePlanMapper.listAll().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("description", p.getDescription());
            m.put("coins", p.getCoins());
            m.put("aiQuota", p.getAiQuota());
            m.put("price", p.getPrice());
            m.put("originalPrice", p.getOriginalPrice());
            m.put("isVip", p.getIsVip());
            m.put("vipDays", p.getVipDays());
            m.put("tag", p.getTag());
            m.put("sortOrder", p.getSortOrder());
            m.put("status", p.getStatus());
            return m;
        }).collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @PostMapping("/vip-plans")
    public ApiResponse<String> createVipPlan(@RequestBody Map<String, Object> body) {
        RechargePlanEntity plan = new RechargePlanEntity();
        plan.setName((String) body.getOrDefault("name", ""));
        plan.setDescription((String) body.getOrDefault("description", ""));
        plan.setCoins(body.get("coins") instanceof Number n ? n.intValue() : 0);
        plan.setAiQuota(body.get("aiQuota") instanceof Number n ? n.intValue() : 0);
        plan.setPrice(body.get("price") instanceof Number n ? new java.math.BigDecimal(n.toString()) : java.math.BigDecimal.ZERO);
        plan.setOriginalPrice(body.get("originalPrice") instanceof Number n ? new java.math.BigDecimal(n.toString()) : java.math.BigDecimal.ZERO);
        plan.setIsVip(body.get("isVip") instanceof Number n ? n.intValue() : 0);
        plan.setVipDays(body.get("vipDays") instanceof Number n ? n.intValue() : 0);
        plan.setTag((String) body.getOrDefault("tag", ""));
        plan.setSortOrder(body.get("sortOrder") instanceof Number n ? n.intValue() : 99);
        rechargePlanMapper.insert(plan);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/vip-plans/{id}")
    public ApiResponse<String> updateVipPlan(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RechargePlanEntity plan = new RechargePlanEntity();
        plan.setId(id);
        plan.setName((String) body.getOrDefault("name", ""));
        plan.setDescription((String) body.getOrDefault("description", ""));
        plan.setCoins(body.get("coins") instanceof Number n ? n.intValue() : 0);
        plan.setAiQuota(body.get("aiQuota") instanceof Number n ? n.intValue() : 0);
        plan.setPrice(body.get("price") instanceof Number n ? new java.math.BigDecimal(n.toString()) : java.math.BigDecimal.ZERO);
        plan.setOriginalPrice(body.get("originalPrice") instanceof Number n ? new java.math.BigDecimal(n.toString()) : java.math.BigDecimal.ZERO);
        plan.setIsVip(body.get("isVip") instanceof Number n ? n.intValue() : 0);
        plan.setVipDays(body.get("vipDays") instanceof Number n ? n.intValue() : 0);
        plan.setTag((String) body.getOrDefault("tag", ""));
        plan.setSortOrder(body.get("sortOrder") instanceof Number n ? n.intValue() : 99);
        rechargePlanMapper.update(plan);
        return ApiResponse.ok("ok");
    }

    @PostMapping("/vip-plans/{id}/toggle")
    public ApiResponse<String> toggleVipPlan(@PathVariable Long id) {
        rechargePlanMapper.toggleStatus(id);
        return ApiResponse.ok("ok");
    }

    // ─── 提现管理 ────────────────────────────────────────

    @GetMapping("/withdraws")
    public ApiResponse<List<Map<String, Object>>> withdraws() {
        // 暂返回空列表，对接bp_withdraw表后替换
        return ApiResponse.ok(List.of());
    }

    @PostMapping("/withdraws/{id}/approve")
    public ApiResponse<String> approveWithdraw(@PathVariable Long id) {
        return ApiResponse.ok("ok");
    }

    @PostMapping("/withdraws/{id}/reject")
    public ApiResponse<String> rejectWithdraw(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok("ok");
    }
}
