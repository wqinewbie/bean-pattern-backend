# AI生成Mock方案 - 快速上手指南

## 📋 已创建的文件

### 后端代码
```
bean-pattern-backend/
├── src/main/java/com/beanpattern/
│   ├── config/
│   │   └── AsyncConfig.java              # 异步任务配置
│   ├── controller/
│   │   └── AiTaskController.java         # AI任务控制器
│   ├── entity/
│   │   └── AiGenerateTask.java           # 任务实体
│   ├── mapper/
│   │   └── AiGenerateTaskMapper.java     # 数据库Mapper
│   └── service/
│       └── AiTaskService.java            # 任务服务（核心逻辑）
├── src/main/resources/db/migration/
│   └── V1__Create_ai_generate_task_table.sql  # 数据库表
└── test-ai-mock.sh                       # 测试脚本
```

---

## 🚀 快速启动（5步）

### 第1步：准备测试图片

上传3张测试图片到COS，获取URL，然后修改 `AiTaskService.java`：

```java
// 第31行，替换为你的COS图片地址
private static final String[] TEST_IMAGES = {
    "https://your-bucket.cos.ap-guangzhou.myqcloud.com/test/cat.png",
    "https://your-bucket.cos.ap-guangzhou.myqcloud.com/test/dog.png",
    "https://your-bucket.cos.ap-guangzhou.myqcloud.com/test/flower.png"
};
```

**或者使用随机图片服务（临时测试）：**
```java
private static final String[] TEST_IMAGES = {
    "https://picsum.photos/512/512?random=1",
    "https://picsum.photos/512/512?random=2",
    "https://picsum.photos/512/512?random=3"
};
```

---

### 第2步：执行数据库迁移

```bash
# 方式1：使用Flyway（如果已配置）
# 启动应用时会自动执行

# 方式2：手动执行SQL
mysql -u root -p bean_pattern < src/main/resources/db/migration/V1__Create_ai_generate_task_table.sql
```

---

### 第3步：启动服务

```bash
cd bean-pattern-backend
mvn clean package
java -jar target/bean-pattern-backend.jar
```

---

### 第4步：测试接口

**方式1：使用测试脚本**
```bash
chmod +x test-ai-mock.sh
./test-ai-mock.sh
```

**方式2：手动测试**
```bash
# 1. 创建任务
curl -X POST http://localhost:8080/api/ai/generate \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: test-session" \
  -d '{
    "prompt": "一只可爱的小猫",
    "style": "宠物毛发",
    "size": 64,
    "brand": "MARD",
    "colorCount": 48
  }'

# 返回：
# {
#   "code": 0,
#   "data": {
#     "taskId": "AI1715234567890123456",
#     "status": "PENDING",
#     "estimatedTime": 30
#   }
# }

# 2. 等待3秒后查询状态
sleep 3
curl http://localhost:8080/api/ai/task/AI1715234567890123456

# 返回：
# {
#   "code": 0,
#   "data": {
#     "taskId": "AI1715234567890123456",
#     "status": "SUCCESS",
#     "aiImageUrl": "https://your-bucket.cos.ap-guangzhou.myqcloud.com/test/cat.png",
#     "completedAt": "2026-05-09T12:34:56"
#   }
# }
```

---

### 第5步：小程序联调

小程序代码无需修改，直接测试：

1. 打开小程序AI生成页面
2. 选择风格、尺寸等参数
3. 点击生成
4. 等待2-3秒
5. 自动跳转到结果页

---

## 📊 接口说明

### 1. 创建任务

**接口：** `POST /api/ai/generate`

**请求头：**
```
Content-Type: application/json
X-Session-Id: {sessionId}
```

**请求体：**
```json
{
  "prompt": "一只可爱的小猫",
  "style": "宠物毛发",
  "size": 64,
  "brand": "MARD",
  "colorCount": 48
}
```

**响应：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "AI1715234567890123456",
    "status": "PENDING",
    "estimatedTime": 30
  }
}
```

---

### 2. 查询任务状态

**接口：** `GET /api/ai/task/{taskId}`

**响应（进行中）：**
```json
{
  "code": 0,
  "data": {
    "taskId": "AI1715234567890123456",
    "status": "PENDING"
  }
}
```

**响应（成功）：**
```json
{
  "code": 0,
  "data": {
    "taskId": "AI1715234567890123456",
    "status": "SUCCESS",
    "aiImageUrl": "https://your-bucket.cos.ap-guangzhou.myqcloud.com/test/cat.png",
    "completedAt": "2026-05-09T12:34:56"
  }
}
```

**响应（失败）：**
```json
{
  "code": 0,
  "data": {
    "taskId": "AI1715234567890123456",
    "status": "FAILED",
    "errorMessage": "生成失败原因",
    "completedAt": "2026-05-09T12:34:56"
  }
}
```

---

## 🔄 升级为正式版

### 只需修改一处代码

**文件：** `AiTaskService.java`

**修改 `AiTaskController.java` 第29行：**

```java
// 临时版本 ❌
String taskId = aiTaskService.createMockTask(request, userId);

// 正式版本 ✅
String taskId = aiTaskService.createTask(request, userId);
```

**然后在 `AiTaskService.java` 中启用注释的代码（第120-150行）：**

```java
// 取消注释这段代码
public String createTask(AiGenerateRequest request, Long userId) {
    // ... 创建任务 ...
    
    // 发送到消息队列
    sendToQueue(taskId, request);
    
    return taskId;
}

private void sendToQueue(String taskId, AiGenerateRequest request) {
    // ... 发送消息到RabbitMQ ...
}
```

---

## ⚠️ 注意事项

1. **测试图片URL必须可访问**
   - 确保COS图片是公开访问或已配置正确的权限
   - 建议使用512x512的正方形图片

2. **数据库表必须创建**
   - 执行SQL脚本创建 `ai_generate_task` 表

3. **异步配置已启用**
   - `@EnableAsync` 已在 `AsyncConfig.java` 中配置
   - 线程池大小：核心5个，最大10个

4. **Session处理**
   - 当前 `getCurrentUserId()` 返回固定值1L
   - 正式环境需要从session或token获取真实userId

---

## 🐛 常见问题

### Q1: 任务一直是PENDING状态？

**原因：** 异步任务没有执行

**解决：**
1. 检查 `AsyncConfig.java` 是否被Spring扫描到
2. 检查 `@EnableAsync` 注解是否生效
3. 查看控制台日志是否有 `[Mock] 开始处理任务` 输出

---

### Q2: 找不到 `AiGenerateTaskMapper`？

**原因：** MyBatis没有扫描到Mapper

**解决：**
在 `Application.java` 添加：
```java
@MapperScan("com.beanpattern.mapper")
```

---

### Q3: 图片URL返回404？

**原因：** 测试图片不存在

**解决：**
1. 上传真实图片到COS
2. 或使用随机图片服务：`https://picsum.photos/512/512?random=1`

---

## ✅ 测试清单

- [ ] 数据库表已创建
- [ ] 测试图片URL已配置
- [ ] 服务启动成功
- [ ] 创建任务接口正常
- [ ] 查询状态接口正常
- [ ] 2-3秒后状态变为SUCCESS
- [ ] 返回的图片URL可访问
- [ ] 小程序联调成功

---

## 📞 技术支持

如有问题，请检查：
1. 控制台日志输出
2. 数据库 `ai_generate_task` 表数据
3. 测试脚本输出

---

**文档版本：** V1.0  
**最后更新：** 2026-05-09  
**相关文档：** doc/AI生成临时Mock方案.md
