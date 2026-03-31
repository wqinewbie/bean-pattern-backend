# Bean Pattern Backend

拼豆魔法屋后端 API 服务，基于 Spring Boot 开发。

## 功能模块

- 用户管理（注册、登录、信息管理）
- 图纸管理（上传、审核、下架）
- Banner 管理（轮播图配置）
- 拼豆品牌、色盘、色号管理
- 订单管理（充值订单）
- VIP 套餐管理
- 反馈管理
- 提现申请处理
- 管理员后台

## 技术栈

- Java 11+
- Spring Boot 2.x
- MyBatis
- MySQL 5.7+
- Maven

## 快速开始

### 环境要求

- JDK 11 或更高版本
- Maven 3.6+
- MySQL 5.7+

### 安装依赖

```bash
mvn clean install
```

### 配置数据库

1. 创建数据库：
```sql
CREATE DATABASE bean_pattern CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入初始化脚本：
```bash
mysql -u root -p bean_pattern < bead_color_init.sql
```

3. 修改 `application-dev.yaml` 中的数据库配置

### 启动项目

```bash
./mvnw spring-boot:run
```

服务将在 `http://localhost:8081` 启动

## API 文档

### 认证

所有需要认证的接口需在请求头中添加：
```
Authorization: Bearer {token}
```

### 主要端点

- `POST /api/admin/login` - 管理员登录
- `GET /api/admin/users` - 用户列表
- `GET /api/admin/patterns` - 图纸列表
- `GET /api/admin/banners` - Banner 列表
- `GET /api/admin/bead/brands` - 拼豆品牌列表
- `GET /api/admin/bead/palettes` - 色盘列表
- `GET /api/admin/bead/colors` - 色号列表

详见 `AdminController` 类

## 项目结构

```
src/main/java/com/beanpattern/
├── controller/      # 控制层
├── service/         # 业务逻辑层
├── mapper/          # 数据访问层
├── entity/          # 实体类
├── model/           # 数据模型
└── config/          # 配置类
```

## 许可证

MIT
