package com.beanpattern.config;

import com.beanpattern.mapper.AdminMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 管理后台接口鉴权拦截器
 * 校验 Authorization: Bearer {token} 头，token = Base64(username:timestamp)
 * 登录接口 /api/admin/login 放行
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminMapper adminMapper;

    public AdminAuthInterceptor(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();
        // 登录接口放行
        if (path.equals("/api/admin/login")) {
            return true;
        }
        // OPTIONS 预检请求放行（CORS 需要）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            reject(response, "未登录或登录已过期");
            return false;
        }
        String token = header.substring(7);
        try {
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            int colonIdx = decoded.lastIndexOf(':');
            if (colonIdx <= 0) { reject(response, "无效的Token"); return false; }
            String username = decoded.substring(0, colonIdx);
            long timestamp = Long.parseLong(decoded.substring(colonIdx + 1));
            // Token 有效期 24 小时
            if (System.currentTimeMillis() - timestamp > 86400_000L) {
                reject(response, "登录已过期，请重新登录");
                return false;
            }
            var admin = adminMapper.findByUsername(username);
            if (admin == null || admin.getStatus() == 0) {
                reject(response, "账号不存在或已禁用");
                return false;
            }
            request.setAttribute("adminUsername", username);
            request.setAttribute("adminId", admin.getId());
            request.setAttribute("adminRole", admin.getRole());
            return true;
        } catch (Exception e) {
            reject(response, "Token解析失败");
            return false;
        }
    }

    private void reject(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        // 直接拼接JSON，避免引入ObjectMapper依赖
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"code\":1,\"message\":\"" + escaped + "\",\"data\":null}");
    }
}
