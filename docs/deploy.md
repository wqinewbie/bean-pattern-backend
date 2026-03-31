# 生产环境部署说明

本文面向把 `bean-pattern-backend` 部署到 Linux 服务器（或同类环境）的场景，包含：进程启动、Nginx 反向代理、HTTPS、以及微信小程序「合法域名」与上传/下载注意事项。

---

## 1. 部署前准备

### 1.1 运行环境

- **JDK 17+**（与 `pom.xml` 中 `java.version` 一致即可）
- **MySQL**：已创建业务库（如 `bean_pattern`），账号具备读写权限
- **Redis**：若业务后续会用到会话/缓存再启用；当前示例代码不强依赖 Redis 业务逻辑，但 classpath 中有依赖时需保证 Redis 可连，或按需在配置中调整
- **域名与证书**：对外提供 HTTPS 时使用（Let's Encrypt 或云厂商证书均可）

### 1.2 构建产物

在项目根目录执行打包：

```bash
./mvnw -DskipTests package
```

生成：`target/bean-pattern-backend-0.0.1-SNAPSHOT.jar`

---

## 2. 生产环境变量

生产使用 `application-prod.yaml`，敏感信息通过环境变量注入（勿把密码写进仓库）。

必填示例：

| 变量名 | 说明 |
|--------|------|
| `DB_URL` | JDBC URL，如 `jdbc:mysql://127.0.0.1:3306/bean_pattern?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai` |
| `DB_USERNAME` | 数据库用户名 |
| `DB_PASSWORD` | 数据库密码 |
| `REDIS_HOST` | Redis 主机 |
| `REDIS_PORT` | Redis 端口，默认 `6379` |
| `REDIS_PASSWORD` | Redis 密码，无则留空 |
| `APP_BASE_URL` | 对外访问后端的 **HTTPS** 根地址，用于拼接上传文件 URL，如 `https://api.example.com` |
| `WECHAT_APP_ID` | 小程序 AppID |
| `WECHAT_APP_SECRET` | 小程序 AppSecret |

可选：

| 变量名 | 说明 |
|--------|------|
| `AI_API_URL` | AI 处理接口地址 |
| `AI_API_KEY` | AI 接口密钥 |

启动示例（Linux，`systemd` 或手工均可）：

```bash
export DB_URL="jdbc:mysql://..."
export DB_USERNAME="..."
export DB_PASSWORD="..."
export REDIS_HOST="127.0.0.1"
export REDIS_PORT="6379"
export REDIS_PASSWORD=""
export APP_BASE_URL="https://api.example.com"
export WECHAT_APP_ID="..."
export WECHAT_APP_SECRET="..."
export AI_API_URL="..."
export AI_API_KEY="..."

java -jar target/bean-pattern-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Windows PowerShell 可参考项目根目录 `README.md` 中的变量设置方式。

---

## 3. Nginx 反向代理（推荐）

小程序正式版要求 **HTTPS**，且域名需在公众平台备案到「服务器域名」。常见做法：Nginx 终止 TLS，反向代理到本机 `8080`。

### 3.1 示例配置（HTTP → 后端，仅内网或临时）

```nginx
server {
    listen 80;
    server_name api.example.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 上传较大图片时可适当调大
        client_max_body_size 20m;
    }
}
```

### 3.2 示例配置（HTTPS）

```nginx
server {
    listen 443 ssl http2;
    server_name api.example.com;

    ssl_certificate     /etc/nginx/ssl/api.example.com.fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/api.example.com.key;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;

        client_max_body_size 20m;
    }
}
```

修改配置后：

```bash
sudo nginx -t && sudo systemctl reload nginx
```

**重要：** 设置 `APP_BASE_URL` 为对外的 HTTPS 地址（如 `https://api.example.com`），否则小程序拿到的图片链接仍是 `http://localhost:8080`，正式环境会无法访问。

---

## 4. 微信小程序「合法域名」

登录 [微信公众平台](https://mp.weixin.qq.com/) → **开发** → **开发管理** → **开发设置** → **服务器域名**。

按本项目实际请求补充：

| 域名类型 | 说明 |
|----------|------|
| **request 合法域名** | 小程序 `wx.request` 访问的后端 API 域名，如 `https://api.example.com` |
| **uploadFile 合法域名** | 若 `wx.uploadFile` 直接上传到该域名，需把同一 API 域名配进去 |
| **downloadFile 合法域名** | 若结果图 URL 指向本域名或其它 CDN 域名，需把对应 **HTTPS 域名** 配进去 |

注意：

- 仅支持 **HTTPS**，且需备案符合微信要求。
- 本地调试可在开发者工具勾选「不校验合法域名」；**体验版/正式版必须配置正确**。

---

## 5. 小程序端配置

生产环境将 `miniprogram/utils/config.js` 中的 `API_BASE_URL` 改为你的 HTTPS API 地址，例如：

```javascript
const API_BASE_URL = "https://api.example.com";
```

重新上传小程序代码并提交审核前，务必在真机上验证：登录、上传、处理、下载全流程。

---

## 6. 常见问题

### 6.1 图片 URL 打不开

- 检查 `APP_BASE_URL` 是否与浏览器/微信最终访问的域名一致。
- 检查 Nginx `client_max_body_size` 是否过小导致上传失败。
- 检查服务器防火墙是否放行 `443` / 反代端口。

### 6.2 HTTPS 证书错误

- 证书链不完整时，部分客户端会拦截；使用完整链（fullchain）配置 `ssl_certificate`。

### 6.3 AI 接口超时

- 可在 Spring Boot 中单独配置 HTTP 客户端超时（当前示例使用 `HttpClient`，未设长超时；长耗时任务可改为异步 + 轮询或消息队列，视业务再扩展）。

---

## 7. 与 README 的关系

- 日常开发、接口说明：见项目根目录 [README.md](../README.md)。
- 生产部署、域名与 Nginx：**以本文为准**。
