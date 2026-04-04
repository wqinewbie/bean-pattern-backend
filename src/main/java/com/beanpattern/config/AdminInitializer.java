package com.beanpattern.config;

import com.beanpattern.entity.AdminEntity;
import com.beanpattern.mapper.AdminMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动后自动确保默认管理员账号存在且密码正确（BCrypt）
 */
@Component
public class AdminInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public AdminInitializer(AdminMapper adminMapper, PasswordEncoder passwordEncoder,
                            @Value("${app.admin.init-enabled:false}") boolean enabled) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!enabled) {
            log.info("[AdminInitializer] default admin initialization is disabled");
            return;
        }
        try {
            AdminEntity existing = adminMapper.findByUsername(DEFAULT_USERNAME);
            if (existing == null) {
                AdminEntity admin = new AdminEntity();
                admin.setUsername(DEFAULT_USERNAME);
                admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                admin.setNickName("超级管理员");
                admin.setRole("SUPER_ADMIN");
                admin.setStatus(1);
                adminMapper.insert(admin);
                log.info("[AdminInitializer] 默认管理员账号已创建: {}/{}", DEFAULT_USERNAME, DEFAULT_PASSWORD);
            } else {
                log.info("[AdminInitializer] 管理员账号已存在，无需初始化");
            }
        } catch (Exception e) {
            log.error("[AdminInitializer] 初始化失败: {}", e.getMessage(), e);
        }
    }
}
