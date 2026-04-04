# 生产环境部署说明

本文面向把 `bean-pattern-backend` 部署到 Linux 服务器（或同类环境）的场景，包含：容器部署、数据库初始化、对象存储、Nginx 反向代理、HTTPS、以及微信小程序合法域名配置。

---

## 1. 仓库与部署文件

- 后端服务：`bean-pattern-backend`
- 管理后台：`bean-pattern-admin`
- Compose 文件：`bean-pattern-backend/deploy/docker-compose.yml`
- 后端生产配置：`bean-pattern-backend/src/main/resources/application-prod.yaml`
- 后台 Nginx 配置：`bean-pattern-admin/deploy/admin.nginx.conf`

说明：服务器部署时请统一使用 `deploy/docker-compose.yml`，不要再使用仓库根目录历史遗留的本地开发 compose 文件。

---

## 2. 部署前准备

### 2.1 运行环境

- Docker 26+
- Docker Compose v2+
- MySQL 8（或兼容版本）
- 可访问的对象存储（当前生产使用腾讯云 COS，按 S3 兼容参数接入）
- 已备案 HTTPS 域名（小程序正式环境必需）

### 2.2 关键经验

新环境必须 **先初始化数据库 schema，再启动 backend**，否则 `SchemaUpgrader` 会因为核心表缺失而报错。

当前 `deploy/docker-compose.yml` 已预留可选 `db-init` 服务（`tools` profile）用于初始化，但初始化 SQL 仍应视为“首次部署动作”，不要依赖业务容器在空库上自愈。

---

## 3. 生产环境变量

生产使用 `application-prod.yaml`，敏感信息通过 `.env` 或容器环境变量注入。

### 3.1 必填变量

| 变量名 | 说明 |
|--------|------|
| `DB_URL` | 生产库 JDBC URL |
| `DB_USERNAME` | 生产库用户名 |
| `DB_PASSWORD` | 生产库密码 |
| `APP_BASE_URL` | 后端对外访问根地址，建议 HTTPS 域名 |
| `S3_ENDPOINT` | COS S3 endpoint，例如 `https://cos.ap-guangzhou.myqcloud.com` |
| `S3_ACCESS_KEY` | COS SecretId |
| `S3_SECRET_KEY` | COS SecretKey |
| `S3_BUCKET` | COS Bucket 名 |
| `S3_REGION` | COS 地域，例如 `ap-guangzhou` |
| `S3_PUBLIC_BASE_URL` | 图片公开访问根地址，例如 `https://<bucket>.cos.ap-guangzhou.myqcloud.com` |
| `WECHAT_APP_ID` | 小程序 AppID |
| `WECHAT_APP_SECRET` | 小程序 AppSecret |

### 3.2 可选变量

| 变量名 | 说明 |
|--------|------|
| `REDIS_HOST` | Redis 主机；当前项目未配置生产 Redis 时可留默认，但短信验证码仅在 Redis 可用或单实例内存兜底下工作 |
| `REDIS_PORT` | Redis 端口，默认 `6379` |
| `AI_API_URL` | AI 接口地址 |
| `AI_API_KEY` | AI 接口密钥 |
| `SMS_*` | 腾讯云短信配置，启用短信发送时必填 |

### 3.3 COS 配置注意事项

当前代码里 `S3_PUBLIC_BASE_URL` 推荐直接填写 bucket 访问域名，例如：

```text
https://bean-pattern-1417861640.cos.ap-guangzhou.myqcloud.com
```

这种情况下：

```text
S3_PATH_STYLE_ACCESS=false
S3_URL_INCLUDE_BUCKET=false
```

否则生成的图片 URL 会重复拼接 bucket 名。

---

## 4. 启动方式

进入：

```bash
cd /home/wangqi/apps/bean-pattern/bean-pattern-backend/deploy
```

首次部署：

```bash
docker compose --profile tools run --rm db-init
```

日常构建与启动：

```bash
docker compose build --no-cache
docker compose up -d
docker compose ps
```

查看日志：

```bash
docker logs bean-pattern-backend --tail 200
docker logs bean-pattern-admin --tail 100
```

---

## 5. 健康检查与访问验证

后端已提供以下健康检查入口：

- `/`
- `/health`
- `/api/health`

可用于：

- 云平台健康探针
- Nginx 回源检测
- 人工快速判断容器是否成功启动

接口返回 `code=0` 且 `data.status=ok` 即表示服务进程正常。

---

## 6. Nginx 与同域部署

当前后台容器监听 `80`，其内部 Nginx 已把 `/api/` 反向代理到 `backend:8081`，因此管理后台上线时推荐直接通过同域名访问：

- 后台页面：`http(s)://admin-domain/`
- 后台接口：`http(s)://admin-domain/api/...`

这样可以避免后台额外处理跨域。

如果要给小程序提供正式服务，仍建议单独准备 HTTPS API 域名，并把：

- request 合法域名
- uploadFile 合法域名
- downloadFile 合法域名

全部配置到微信公众平台。

---

## 7. 小程序上线前配置

`miniprogram/utils/config.js` 不能保留本地内网地址，必须替换成正式 HTTPS API 域名，例如：

```javascript
const API_BASE_URL = "https://api.example.com";
```

注意：

- 小程序正式版不能使用 IP 直连
- `wx.request` / `wx.uploadFile` / `wx.downloadFile` 都依赖合法域名配置
- 若图片 URL 指向 COS，也要把 COS 域名加入下载域名白名单，或统一走你自己的 CDN 域名

---

## 8. 数据库演进建议

当前项目仍存在 `SchemaUpgrader` 自动补字段逻辑，它适合“兼容已有表结构”，不适合替代正式迁移体系。

建议后续尽快引入 Flyway：

1. 把 schema 初始化与增量变更写成版本化 SQL
2. 每次发布前先执行迁移
3. 让生产环境表结构变更可审计、可追踪、可回滚

在 Flyway 完全落地前，至少应维护 `sql/migrations/` 目录记录每次上线 SQL。

---

## 9. 日常更新流程（建议）

```bash
cd /home/wangqi/apps/bean-pattern/bean-pattern-backend
git pull origin main

cd /home/wangqi/apps/bean-pattern/bean-pattern-admin
git pull origin main

cd /home/wangqi/apps/bean-pattern/bean-pattern-backend/deploy
docker compose build
docker compose up -d
```

若涉及数据库变更：

1. 先执行迁移 SQL / Flyway
2. 再执行 `docker compose up -d`
3. 最后验证 `/api/health`、后台登录、小程序主流程
