# AI魔法风格配置 - 实施说明

## 📦 已创建的文件

### 后端代码（5个文件）
```
bean-pattern-backend/
├── src/main/java/com/beanpattern/
│   ├── entity/AiMagicStyle.java              ✅ 实体类
│   ├── mapper/AiMagicStyleMapper.java        ✅ Mapper
│   ├── service/AiMagicStyleService.java      ✅ Service
│   └── controller/AiMagicStyleController.java ✅ Controller
├── src/main/resources/db/migration/
│   └── V2__Create_ai_magic_style_table.sql   ✅ Flyway迁移脚本
└── ai_magic_style.sql                        ✅ 独立SQL文件（手动执行）
```

### 前端代码（1个文件）
```
bean-pattern-admin/
└── src/views/AiMagicStyle.vue                ✅ 管理页面
```

---

## 🚀 实施步骤

### 第1步：执行SQL脚本

```bash
# 方式1：使用独立SQL文件（推荐）
mysql -u root -p bean_pattern < bean-pattern-backend/ai_magic_style.sql

# 方式2：使用Flyway（如果已配置）
# 启动应用时会自动执行 V2__Create_ai_magic_style_table.sql
```

**SQL文件位置：** `bean-pattern-backend/ai_magic_style.sql`

---

### 第2步：添加管理后台路由

编辑 `bean-pattern-admin/src/router/index.js`，添加路由：

```javascript
{
  path: '/ai-magic-style',
  name: 'AiMagicStyle',
  component: () => import('../views/AiMagicStyle.vue'),
  meta: { title: 'AI魔法风格' }
}
```

---

### 第3步：添加管理后台菜单

编辑 `bean-pattern-admin/src/layouts/AdminLayout.vue`，添加菜单项：

```vue
<el-menu-item index="/ai-magic-style">
  <el-icon><MagicStick /></el-icon>
  <span>AI魔法风格</span>
</el-menu-item>
```

---

### 第4步：修改小程序代码

编辑 `miniprogram/pages/ai-generate/ai-generate.js`：

**修改 data 部分：**
```javascript
data: {
  magicStyles: [],  // 改为空数组，从后台获取
  selectedStyle: '',
  // ... 其他保持不变
},
```

**修改 onLoad 方法：**
```javascript
onLoad() {
  const layout = getSafeAreaLayout();
  // ... 其他初始化代码 ...
  
  this.loadBrandsFromServer();
  this.loadMagicStyles();  // ✅ 添加这行
},
```

**添加 loadMagicStyles 方法：**
```javascript
/**
 * 加载魔法风格列表
 */
loadMagicStyles() {
  request.get('/ai/magic-styles').then(data => {
    if (data && data.length > 0) {
      this.setData({
        magicStyles: data,
        selectedStyle: data[0].name  // 默认选中第一个
      });
    }
  }).catch(err => {
    console.error('加载魔法风格失败', err);
    // 使用默认风格（兜底）
    this.setData({
      magicStyles: [
        { name: '人物特化', icon: '👤', category: '题材', tag: '适用人物' }
      ],
      selectedStyle: '人物特化'
    });
  });
},
```

---

### 第5步：重启服务测试

```bash
# 1. 重启后端服务
cd bean-pattern-backend
mvn spring-boot:run

# 2. 测试接口
curl http://localhost:8080/api/ai/magic-styles

# 3. 访问管理后台
# http://localhost:5173/ai-magic-style

# 4. 测试小程序
# 打开小程序AI生成页面，查看风格列表
```

---

## ✅ 验证清单

- [ ] SQL脚本执行成功
- [ ] 数据库表 `ai_magic_style` 已创建
- [ ] 初始数据已插入（9条记录）
- [ ] 后端接口 `/api/ai/magic-styles` 返回数据
- [ ] 管理后台页面可访问
- [ ] 管理后台可以添加/编辑/删除风格
- [ ] 小程序可以获取风格列表
- [ ] 小程序风格选择功能正常

---

## 📊 接口测试

### 测试小程序接口

```bash
curl http://localhost:8080/api/ai/magic-styles
```

**预期返回：**
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "人物特化",
      "icon": "👤",
      "category": "题材",
      "tag": "适用人物",
      "description": "专注人物细节，适合人像照片",
      "promptTemplate": "portrait, detailed face, high quality, professional photography",
      "sortOrder": 1,
      "enabled": 1
    },
    // ... 其他风格
  ]
}
```

### 测试管理后台接口

```bash
curl http://localhost:8080/api/admin/ai-magic-style/list
```

---

## 🎯 功能说明

### 管理后台功能

1. **列表展示**
   - 显示所有风格（包括禁用的）
   - 按排序字段升序排列

2. **添加风格**
   - 填写风格名称、图标、分类等
   - 配置AI提示词模板（正式版使用）

3. **编辑风格**
   - 修改风格信息
   - 调整排序
   - 启用/禁用

4. **删除风格**
   - 删除不需要的风格

### 小程序功能

1. **动态加载**
   - 从后台获取启用的风格列表
   - 按排序字段显示

2. **兜底机制**
   - 如果接口失败，使用默认风格
   - 保证功能可用

---

## 🔄 升级为正式版

当AI服务开发完成后，可以使用 `promptTemplate` 字段：

```java
// AiTaskService.java
public String createTask(AiGenerateRequest request, Long userId) {
    // 获取风格的提示词模板
    String promptTemplate = aiMagicStyleService.getPromptTemplate(request.getStyle());
    
    // 构造完整提示词
    String fullPrompt = request.getPrompt() + ", " + promptTemplate;
    
    // 发送到消息队列
    sendToQueue(taskId, fullPrompt, request);
}
```

---

## 📝 注意事项

1. **Emoji显示**
   - 确保数据库字符集为 `utf8mb4`
   - 否则emoji可能显示为乱码

2. **排序规则**
   - 数字越小越靠前
   - 建议使用 1, 2, 3... 或 10, 20, 30...

3. **启用状态**
   - 禁用的风格不会在小程序显示
   - 但管理后台可以看到

4. **兜底机制**
   - 小程序有默认风格兜底
   - 即使接口失败也能正常使用

---

**文档版本：** V1.0  
**最后更新：** 2026-05-09  
**文档路径：** bean-pattern-backend/README_AI_MAGIC_STYLE.md
