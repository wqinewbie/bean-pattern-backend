package com.beanpattern.controller;

import com.beanpattern.config.PasswordEncoder;
import com.beanpattern.entity.*;
import com.beanpattern.mapper.*;
import com.beanpattern.mapper.TutorialMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.ImageUploadResponse;
import com.beanpattern.service.GiftPackageService;
import com.beanpattern.service.GiftTypeConfigService;
import com.beanpattern.service.ImageStorageService;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    private static final String ROLE_ADMIN = "ADMIN";

    private static final String MSG_ADMIN_NOT_FOUND = "管理员不存在";
    private static final String MSG_ONLY_SUPER_ADMIN_CREATE = "仅超级管理员可新增管理员";
    private static final String MSG_ONLY_SUPER_ADMIN_DELETE = "仅超级管理员可删除管理员";
    private static final String MSG_CANNOT_DELETE_CURRENT = "不能删除当前登录账号";
    private static final String MSG_ONLY_DELETE_ADMIN = "仅可删除普通管理员";
    private static final String MSG_DEFAULT_ADMIN_PROTECTED = "默认管理员不可删除";

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final BannerMapper bannerMapper;
    private final FeedbackMapper feedbackMapper;
    private final CreatorPatternMapper creatorPatternMapper;
    private final OrderMapper orderMapper;
    private final RechargePlanMapper rechargePlanMapper;
    private final BeadAdminMapper beadAdminMapper;
    private final TutorialMapper tutorialMapper;
    private final TaskConfigMapper taskConfigMapper;
    private final GiftPackageService giftPackageService;
    private final GiftTypeConfigService giftTypeConfigService;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageStorageService;

    public AdminController(AdminMapper adminMapper, UserMapper userMapper,
                           BannerMapper bannerMapper, FeedbackMapper feedbackMapper,
                           CreatorPatternMapper creatorPatternMapper,
                           OrderMapper orderMapper,
                           RechargePlanMapper rechargePlanMapper,
                           BeadAdminMapper beadAdminMapper,
                           TutorialMapper tutorialMapper,
                           TaskConfigMapper taskConfigMapper,
                           GiftPackageService giftPackageService,
                           GiftTypeConfigService giftTypeConfigService,
                           PasswordEncoder passwordEncoder,
                           ImageStorageService imageStorageService) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.bannerMapper = bannerMapper;
        this.feedbackMapper = feedbackMapper;
        this.creatorPatternMapper = creatorPatternMapper;
        this.orderMapper = orderMapper;
        this.rechargePlanMapper = rechargePlanMapper;
        this.beadAdminMapper = beadAdminMapper;
        this.tutorialMapper = tutorialMapper;
        this.taskConfigMapper = taskConfigMapper;
        this.giftPackageService = giftPackageService;
        this.giftTypeConfigService = giftTypeConfigService;
        this.passwordEncoder = passwordEncoder;
        this.imageStorageService = imageStorageService;
    }

    // ─── 看板 ───────────────────────────────────────────

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", userMapper.count());
        // 旧任务表已废弃，显示0
        data.put("totalTasks", 0);
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

    @GetMapping("/user-patterns")
    public ApiResponse<Map<String, Object>> userPatterns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String taskType,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String isSaved) {
        // 旧任务表已废弃，返回空列表
        return ApiResponse.ok(Map.of("list", List.of(), "total", 0));
    }

    // ─── 礼品类型管理 ──────────────────────────────────────

    @GetMapping("/gift-types")
    public ApiResponse<List<GiftTypeConfig>> giftTypes(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ApiResponse.ok(activeOnly ? giftTypeConfigService.listActive() : giftTypeConfigService.listAll());
    }

    @PostMapping("/gift-types")
    public ApiResponse<GiftTypeConfig> createGiftType(@RequestBody GiftTypeConfig giftTypeConfig) {
        try {
            giftTypeConfig.setId(null);
            return ApiResponse.ok(giftTypeConfigService.save(giftTypeConfig));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PutMapping("/gift-types/{id}")
    public ApiResponse<GiftTypeConfig> updateGiftType(@PathVariable Long id, @RequestBody GiftTypeConfig giftTypeConfig) {
        try {
            giftTypeConfig.setId(id);
            return ApiResponse.ok(giftTypeConfigService.save(giftTypeConfig));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/gift-types/{id}/toggle")
    public ApiResponse<String> toggleGiftType(@PathVariable Long id) {
        giftTypeConfigService.toggleStatus(id);
        return ApiResponse.ok("ok");
    }

    // ─── 礼品包管理 ──────────────────────────────────────

    @GetMapping("/gift-packages")
    public ApiResponse<List<GiftPackage>> giftPackages(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ApiResponse.ok(activeOnly ? giftPackageService.listActive() : giftPackageService.listAll());
    }

    @PostMapping("/gift-packages")
    public ApiResponse<GiftPackage> createGiftPackage(@RequestBody GiftPackage giftPackage) {
        try {
            giftPackage.setId(null);
            return ApiResponse.ok(giftPackageService.save(giftPackage));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PutMapping("/gift-packages/{id}")
    public ApiResponse<GiftPackage> updateGiftPackage(@PathVariable Long id, @RequestBody GiftPackage giftPackage) {
        try {
            giftPackage.setId(id);
            return ApiResponse.ok(giftPackageService.save(giftPackage));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/gift-packages/{id}/toggle")
    public ApiResponse<String> toggleGiftPackage(@PathVariable Long id) {
        giftPackageService.toggleStatus(id);
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
            m.put("bgColor", b.getBgColor() != null ? b.getBgColor() : "");
            m.put("linkType", b.getLinkType() != null ? b.getLinkType() : "NONE");
            m.put("linkValue", b.getLinkValue() != null ? b.getLinkValue() : "");
            m.put("actionType", b.getActionType() != null ? b.getActionType() : "");
            m.put("actionConfig", b.getActionConfig() != null ? b.getActionConfig() : "");
            m.put("sortOrder", b.getSortOrder());
            m.put("status", b.getStatus());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/banners")
    public ApiResponse<String> createBanner(@RequestBody Map<String, Object> body) {
        BannerEntity b = new BannerEntity();
        b.setTitle(cleanBannerText((String) body.getOrDefault("title", "")));
        b.setSubTitle(cleanBannerText((String) body.getOrDefault("subTitle", "")));
        b.setImageUrl((String) body.getOrDefault("imageUrl", ""));
        b.setTagText(cleanBannerText((String) body.getOrDefault("tagText", "")));
        b.setBgColor((String) body.getOrDefault("bgColor", ""));
        b.setLinkType((String) body.getOrDefault("linkType", "NONE"));
        b.setLinkValue((String) body.getOrDefault("linkValue", ""));
        b.setActionType((String) body.getOrDefault("actionType", ""));
        b.setActionConfig((String) body.getOrDefault("actionConfig", ""));
        b.setSortOrder(body.get("sortOrder") instanceof Number n ? n.intValue() : 0);
        b.setStatus(1);
        bannerMapper.insert(b);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/banners/{id}")
    public ApiResponse<String> updateBanner(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        bannerMapper.update(id,
                cleanBannerText((String) body.getOrDefault("title", "")),
                cleanBannerText((String) body.getOrDefault("subTitle", "")),
                (String) body.getOrDefault("imageUrl", ""),
                cleanBannerText((String) body.getOrDefault("tagText", "")),
                (String) body.getOrDefault("bgColor", ""),
                body.get("sortOrder") instanceof Number n ? n.intValue() : 0,
                (String) body.getOrDefault("linkType", "NONE"),
                (String) body.getOrDefault("linkValue", ""),
                (String) body.getOrDefault("actionType", ""),
                (String) body.getOrDefault("actionConfig", ""));
        return ApiResponse.ok("ok");
    }

    @PostMapping("/banners/{id}/toggle")
    public ApiResponse<String> toggleBanner(@PathVariable Long id) {
        bannerMapper.toggleStatus(id);
        return ApiResponse.ok("ok");
    }

    // ─── 教程管理 ────────────────────────────────────────

    @GetMapping("/tutorials")
    public ApiResponse<List<Map<String, Object>>> adminTutorials() {
        return ApiResponse.ok(tutorialMapper.listAll().stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("title", t.getTitle());
            m.put("description", t.getDescription() != null ? t.getDescription() : "");
            m.put("videoUrl", t.getVideoUrl() != null ? t.getVideoUrl() : "");
            m.put("thumbnailUrl", t.getThumbnailUrl() != null ? t.getThumbnailUrl() : "");
            m.put("sortOrder", t.getSortOrder());
            m.put("status", t.getStatus());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/tutorials")
    public ApiResponse<String> createTutorial(@RequestBody Map<String, Object> body) {
        TutorialEntity t = new TutorialEntity();
        t.setTitle((String) body.getOrDefault("title", ""));
        t.setDescription((String) body.getOrDefault("description", ""));
        t.setVideoUrl((String) body.getOrDefault("videoUrl", ""));
        t.setThumbnailUrl((String) body.getOrDefault("thumbnailUrl", ""));
        t.setSortOrder(body.get("sortOrder") instanceof Number n ? n.intValue() : 0);
        t.setStatus(1);
        tutorialMapper.insert(t);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/tutorials/{id}")
    public ApiResponse<String> updateTutorial(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        TutorialEntity t = new TutorialEntity();
        t.setId(id);
        t.setTitle((String) body.getOrDefault("title", ""));
        t.setDescription((String) body.getOrDefault("description", ""));
        t.setVideoUrl((String) body.getOrDefault("videoUrl", ""));
        t.setThumbnailUrl((String) body.getOrDefault("thumbnailUrl", ""));
        t.setSortOrder(body.get("sortOrder") instanceof Number n ? n.intValue() : 0);
        tutorialMapper.update(t);
        return ApiResponse.ok("ok");
    }

    @PostMapping("/tutorials/{id}/toggle")
    public ApiResponse<String> toggleTutorial(@PathVariable Long id) {
        tutorialMapper.toggleStatus(id);
        return ApiResponse.ok("ok");
    }

    @PostMapping(value = "/tutorials/video-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadTutorialVideo(@RequestParam("file") MultipartFile file) {
        ImageUploadResponse upload = imageStorageService.store(file);
        return ApiResponse.ok(upload);
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

    @PostMapping("/bead/palettes/{id}/batch-add-colors")
    public ApiResponse<Map<String, Object>> batchAddPaletteColors(@PathVariable Long id,
                                                                   @RequestBody Map<String, Object> body) {
        if (beadAdminMapper.countPaletteById(id) <= 0) return ApiResponse.fail("色盘不存在");

        Object raw = body.get("codes");
        if (!(raw instanceof List<?> rawList) || rawList.isEmpty()) return ApiResponse.fail("codes 不能为空");

        List<String> codes = rawList.stream()
                .map(v -> v == null ? "" : String.valueOf(v).trim())
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (codes.isEmpty()) return ApiResponse.fail("codes 不能为空");

        List<Long> colorIds = beadAdminMapper.listColorIdsByCodes(codes);
        int added = 0;
        if (!colorIds.isEmpty()) {
            added = beadAdminMapper.insertPaletteColorsBatch(id, colorIds);
        }

        int found = colorIds.size();
        int missing = Math.max(codes.size() - found, 0);
        int ignored = Math.max(found - added, 0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inputCount", codes.size());
        result.put("foundCount", found);
        result.put("addedCount", added);
        result.put("ignoredCount", ignored);
        result.put("missingCount", missing);
        return ApiResponse.ok(result);
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
    public ApiResponse<List<Map<String, Object>>> listAdmins(jakarta.servlet.http.HttpServletRequest request) {
        Long currentAdminId = (Long) request.getAttribute("adminId");
        return ApiResponse.ok(adminMapper.listAll().stream().map(a -> toAdminVO(a, currentAdminId)).collect(Collectors.toList()));
    }

    @PostMapping("/admins")
    public ApiResponse<String> createAdmin(@RequestBody Map<String, String> body,
                                           jakarta.servlet.http.HttpServletRequest request) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "").trim();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) return ApiResponse.fail("账号和密码不能为空");

        if (!isSuperAdmin(request)) return ApiResponse.fail(MSG_ONLY_SUPER_ADMIN_CREATE);
        if (adminMapper.findByUsername(username) != null) return ApiResponse.fail("账号已存在");

        AdminEntity a = new AdminEntity();
        a.setUsername(username);
        a.setPassword(passwordEncoder.encode(password));
        a.setNickName(body.getOrDefault("nickName", username));
        a.setRole(ROLE_ADMIN);
        a.setStatus(1);
        adminMapper.insert(a);
        return ApiResponse.ok("ok");
    }

    @DeleteMapping("/admins/{id}")
    public ApiResponse<String> deleteAdmin(@PathVariable Long id,
                                           jakarta.servlet.http.HttpServletRequest request) {
        var admin = adminMapper.findById(id);
        if (admin == null) return ApiResponse.fail(MSG_ADMIN_NOT_FOUND);

        Long currentAdminId = (Long) request.getAttribute("adminId");
        if (currentAdminId != null && currentAdminId.equals(id)) {
            return ApiResponse.fail(MSG_CANNOT_DELETE_CURRENT);
        }
        if (!isSuperAdmin(request)) return ApiResponse.fail(MSG_ONLY_SUPER_ADMIN_DELETE);
        if (!ROLE_ADMIN.equalsIgnoreCase(admin.getRole())) {
            return ApiResponse.fail(MSG_ONLY_DELETE_ADMIN);
        }
        if ("admin".equalsIgnoreCase(admin.getUsername())) return ApiResponse.fail(MSG_DEFAULT_ADMIN_PROTECTED);

        adminMapper.deleteById(id);
        return ApiResponse.ok("ok");
    }

    private boolean isSuperAdmin(jakarta.servlet.http.HttpServletRequest request) {
        String currentRole = String.valueOf(request.getAttribute("adminRole"));
        return ROLE_SUPER_ADMIN.equalsIgnoreCase(currentRole);
    }

    private Map<String, Object> toAdminVO(AdminEntity a, Long currentAdminId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("username", a.getUsername());
        m.put("nickName", a.getNickName() != null ? a.getNickName() : "");
        m.put("role", a.getRole());
        m.put("status", a.getStatus());
        m.put("lastLoginAt", a.getLastLoginAt() != null ? a.getLastLoginAt().toString() : "");
        m.put("isCurrent", currentAdminId != null && currentAdminId.equals(a.getId()));
        return m;
    }

    private String cleanBannerText(String value) {
        if (value == null) {
            return "";
        }
        String sanitized = value
                .replace("\u0000", "")
                .replace("\r", "")
                .trim();
        return sanitized.length() > 255 ? sanitized.substring(0, 255) : sanitized;
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

    // ─── 任务中心管理 ─────────────────────────────────────

    @GetMapping("/tasks")
    public ApiResponse<List<TaskConfig>> getTasks() {
        List<TaskConfig> list = taskConfigMapper.findAll();
        return ApiResponse.ok(list);
    }

    @PostMapping("/tasks")
    public ApiResponse<String> createTask(@RequestBody TaskConfig task) {
        if (task.getTaskCode() == null || task.getTaskCode().trim().isEmpty()) {
            return ApiResponse.fail("任务代码不能为空");
        }
        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
            return ApiResponse.fail("任务名称不能为空");
        }
        // 检查任务代码是否已存在
        TaskConfig existing = taskConfigMapper.findByCode(task.getTaskCode());
        if (existing != null) {
            return ApiResponse.fail("任务代码已存在");
        }
        taskConfigMapper.insert(task);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/tasks/{id}")
    public ApiResponse<String> updateTask(@PathVariable Long id, @RequestBody TaskConfig task) {
        TaskConfig existing = taskConfigMapper.findById(id);
        if (existing == null) {
            return ApiResponse.fail("任务不存在");
        }
        task.setId(id);
        taskConfigMapper.update(task);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/tasks/{id}/status")
    public ApiResponse<String> toggleTaskStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        TaskConfig existing = taskConfigMapper.findById(id);
        if (existing == null) {
            return ApiResponse.fail("任务不存在");
        }
        taskConfigMapper.updateStatus(id, isActive);
        return ApiResponse.ok("ok");
    }

    @DeleteMapping("/tasks/{id}")
    public ApiResponse<String> deleteTask(@PathVariable Long id) {
        TaskConfig existing = taskConfigMapper.findById(id);
        if (existing == null) {
            return ApiResponse.fail("任务不存在");
        }
        taskConfigMapper.deleteById(id);
        return ApiResponse.ok("ok");
    }
}
