# AI笔记工具 - 技术架构文档

## 文档信息
- **项目名称**：AI笔记工具
- **版本**：v1.0
- **创建日期**：2026-04-01
- **文档状态**：草稿

---

## 1. 技术选型

### 1.1 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 渐进式JavaScript框架 |
| TypeScript | 5.3+ | JavaScript超集，提供类型安全 |
| Vite | 5.0+ | 下一代前端构建工具 |
| Element Plus | 2.4+ | Vue 3 UI组件库 |
| Pinia | 2.1+ | Vue 3 状态管理 |
| Vue Router | 4.2+ | Vue官方路由管理器 |
| Axios | 1.6+ | HTTP客户端 |
| Vditor | 3.9+ | Markdown编辑器 |
| Tiptap | 2.1+ | 富文本编辑器 |

### 1.2 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | LTS版本 |
| Spring Boot | 3.2+ | Java应用框架 |
| MyBatis Plus | 3.5+ | MyBatis增强工具 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存数据库 |
| MinIO | RELEASE.2023+ | 对象存储 |
| JWT | 0.12+ | JSON Web Token |
| Swagger/Knife4j | 4.0+ | API文档 |

### 1.3 AI服务集成

| 服务 | SDK版本 | 说明 |
|------|---------|------|
| DeepSeek | Latest | 深度求索AI服务 |
| 智谱GLM | Latest | 清华KEG AI服务 |

### 1.4 部署技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Nginx | 1.24+ | 反向代理/静态资源服务器 |
| Docker | 24.0+ | 容器化部署 |
| Docker Compose | 2.20+ | 多容器编排 |

---

## 2. 系统架构

### 2.1 总体架构

```
┌─────────────────────────────────────────────────────────────┐
│                         用户层                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Web浏览器   │  │    移动端     │  │    桌面端     │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼─────────────────┼─────────────────┼───────────────┘
          │                 │                 │
          └─────────────────┴─────────────────┘
                            │ HTTPS
        ┌───────────────────▼───────────────────┐
        │            Nginx (反向代理)             │
        │  - 静态资源服务                         │
        │  - SSL终端                             │
        │  - 负载均衡                             │
        │  - Gzip压缩                            │
        └───────────────────┬───────────────────┘
                            │
        ┌───────────────────▼───────────────────┐
        │         前端应用 (Vue 3 SPA)            │
        │  ┌─────────────────────────────────┐  │
        │  │  视图层 (Views)                  │  │
        │  ├─────────────────────────────────┤  │
        │  │  组件层 (Components)             │  │
        │  ├─────────────────────────────────┤  │
        │  │  状态管理 (Pinia)                │  │
        │  ├─────────────────────────────────┤  │
        │  │  路由 (Vue Router)               │  │
        │  ├─────────────────────────────────┤  │
        │  │  编辑器 (Markdown/RichText)      │  │
        │  └─────────────────────────────────┘  │
        └───────────────────┬───────────────────┘
                            │ REST API
        ┌───────────────────▼───────────────────┐
        │      后端应用 (Spring Boot)            │
        │  ┌─────────────────────────────────┐  │
        │  │  Web层 (Controller)             │  │
        │  ├─────────────────────────────────┤  │
        │  │  业务层 (Service)                │  │
        │  ├─────────────────────────────────┤  │
        │  │  数据访问层 (DAO/Repository)     │  │
        │  ├─────────────────────────────────┤  │
        │  │  AI集成层 (AI Service)           │  │
        │  ├─────────────────────────────────┤  │
        │  │  工具层 (Utils)                 │  │
        │  └─────────────────────────────────┘  │
        └───────────────────┬───────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   ┌────▼────┐        ┌────▼────┐        ┌────▼────┐
   │  MySQL  │        │  Redis  │        │  MinIO  │
   │ (持久化) │        │  (缓存) │        │ (文件)  │
   └─────────┘        └─────────┘        └─────────┘
                            │
        ┌───────────────────▼───────────────────┐
        │           外部AI服务                    │
        │  ┌──────────────┐  ┌──────────────┐  │
        │  │  DeepSeek    │  │    GLM       │  │
        │  │  API         │  │    API       │  │
        │  └──────────────┘  └──────────────┘  │
        └───────────────────────────────────────┘
```

### 2.2 前端架构

```
src/
├── api/                 # API接口
│   ├── auth.ts
│   ├── note.ts
│   ├── ai.ts
│   └── ...
├── assets/              # 静态资源
│   ├── styles/
│   └── images/
├── components/          # 公共组件
│   ├── editor/
│   │   ├── MarkdownEditor.vue
│   │   └── RichTextEditor.vue
│   ├── common/
│   └── business/
├── composables/         # 组合式函数
│   ├── useAuth.ts
│   ├── useNote.ts
│   └── useAI.ts
├── layouts/             # 布局组件
│   ├── DefaultLayout.vue
│   └── EditorLayout.vue
├── router/              # 路由配置
│   └── index.ts
├── stores/              # Pinia状态管理
│   ├── auth.ts
│   ├── note.ts
│   ├── editor.ts
│   └── ai.ts
├── types/               # TypeScript类型定义
│   ├── index.ts
│   └── api.d.ts
├── utils/               # 工具函数
│   ├── request.ts       # Axios封装
│   ├── storage.ts       # 本地存储
│   └── ...
├── views/               # 页面视图
│   ├── auth/
│   ├── notes/
│   ├── editor/
│   └── settings/
├── App.vue
└── main.ts
```

### 2.3 后端架构

```
src/
├── main.java
├── config/              # 配置类
│   ├── SecurityConfig.java
│   ├── MybatisPlusConfig.java
│   ├── RedisConfig.java
│   └── MinioConfig.java
├── controller/          # 控制器层
│   ├── AuthController.java
│   ├── NoteController.java
│   ├── FolderController.java
│   ├── AIController.java
│   └── ...
├── service/             # 业务逻辑层
│   ├── impl/
│   ├── AuthService.java
│   ├── NoteService.java
│   ├── AIService.java
│   └── ...
├── mapper/              # 数据访问层
│   ├── UserMapper.java
│   ├── NoteMapper.java
│   └── ...
├── entity/              # 实体类
│   ├── User.java
│   ├── Note.java
│   └── ...
├── dto/                 # 数据传输对象
│   ├── request/
│   └── response/
├── vo/                  # 视图对象
├── common/              # 公共模块
│   ├── exception/
│   ├── enums/
│   └── constants/
├── ai/                  # AI集成
│   ├── AIProvider.java
│   ├── DeepSeekProvider.java
│   ├── GLMProvider.java
│   └── ...
├── util/                # 工具类
│   ├── JwtUtil.java
│   ├── FileUtil.java
│   └── ...
└── Application.java
```

---

## 3. 核心模块设计

### 3.1 用户认证模块

#### 3.1.1 认证流程

```
┌──────────┐    1. POST /api/auth/login    ┌──────────┐
│  客户端   │ ────────────────────────────→ │  后端    │
└──────────┘                              └──────────┘
    ↑                                            │
    │ 2. 返回JWT Token                            │
    │                                            │
    │ 3. 后续请求携带Token                        │
    │                                            │
    │ 4. 验证Token，返回用户信息                 │
    │                                            │
    │─────────────────────────────────────────→ │
    │ 5. 返回受保护资源                         │
    │←───────────────────────────────────────── │
    │                                            │
    │←─────────────────────────────────────────── │
```

#### 3.1.2 Token管理

- **Access Token**：有效期2小时
- **Refresh Token**：有效期7天
- Token存储：LocalStorage
- 自动刷新机制

### 3.2 笔记编辑模块

#### 3.2.1 编辑器架构

```
┌────────────────────────────────────────────────┐
│              EditorContainer.vue               │
│  ┌────────────────────────────────────────┐  │
│  │          MarkdownEditor.vue            │  │
│  │  ┌────────────┐    ┌────────────┐     │  │
│  │  │ 编辑区     │    │  预览区     │     │  │
│  │  └────────────┘    └────────────┘     │  │
│  └────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────┐  │
│  │         RichTextEditor.vue             │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │  工具栏 + 编辑区                │  │  │
│  │  └─────────────────────────────────┘  │  │
│  └────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────┐  │
│  │            AIAssistant.vue             │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │  对话面板 + 快捷操作            │  │  │
│  │  └─────────────────────────────────┘  │  │
│  └────────────────────────────────────────┘  │
└────────────────────────────────────────────────┘
```

#### 3.2.2 自动保存机制

```javascript
// 防抖自动保存
const debouncedSave = useDebounceFn(async (content) => {
  await api.updateNote(noteId, { content });
  showAutoSaved();
}, 3000);

// 监听内容变化
watch(() => editorContent, (newContent) => {
  debouncedSave(newContent);
});
```

### 3.3 AI集成模块

#### 3.3.1 AI服务抽象

```java
public interface AIProvider {
    /**
     * 对话
     */
    String chat(String model, List<Message> messages);

    /**
     * 生成内容
     */
    String generate(String model, String prompt);

    /**
     * 摘要
     */
    String summarize(String model, String content);

    /**
     * 优化
     */
    String optimize(String model, String content);
}
```

#### 3.3.2 AI调用流程

```
┌──────────┐    1. 发起AI请求    ┌──────────┐    2. 路由到具体Provider
│  客户端   │ ──────────────────→│  后端    │ ──────────────────→
└──────────┘                    └──────────┘                     │
    ↑                                                              │
    │ 4. 返回AI结果                                                 │
    │                                                              │
    │←───────────────────────────────────────────────────────────── │
    │                                                              │
    │ 3. 调用外部AI服务                                             │
    │                              ┌──────────────┐               │
    │←─────────────────────────────│  DeepSeek/   │               │
    │                              │  GLM API     │               │
    │→─────────────────────────────└──────────────┘               │
```

### 3.4 版本管理模块

#### 3.4.1 版本保存策略

- **自动保存**：笔记更新时自动创建版本（每10分钟或内容变化超过5%）
- **手动快照**：用户主动创建版本
- **版本保留**：保留最近50个版本
- **版本压缩**：超过30天的版本进行压缩存储

#### 3.4.2 版本对比算法

```javascript
// 使用 diff-match-patch 进行版本对比
const diff = diff_match_patch().diff_main(oldContent, newContent);
const diffHtml = diff_match_patch().diff_prettyHtml(diff);
```

---

## 4. 数据库设计

### 4.1 索引策略

| 表名 | 索引名 | 字段 | 类型 | 说明 |
|------|--------|------|------|------|
| t_user | idx_email | email | 普通索引 | 登录查询 |
| t_user | idx_username | username | 普通索引 | 用户名查询 |
| t_note | idx_user_id | user_id | 普通索引 | 用户笔记查询 |
| t_note | idx_space_id | space_id | 普通索引 | 空间笔记查询 |
| t_note | idx_folder_id | folder_id | 普通索引 | 文件夹笔记查询 |
| t_note | idx_status | status | 普通索引 | 状态过滤 |
| t_note | idx_created_at | created_at | 普通索引 | 时间排序 |
| t_note_version | idx_note_id | note_id | 普通索引 | 笔记版本查询 |
| t_note_version | idx_version | note_id, version_no | 联合索引 | 版本号查询 |
| t_attachment | idx_user_id | user_id | 普通索引 | 用户文件查询 |
| t_attachment | idx_note_id | note_id | 普通索引 | 笔记附件查询 |

### 4.2 分区策略（可选）

对于大型部署，可以考虑对以下表进行分区：

- **t_note_version**：按时间范围分区（月度/季度）
- **t_ai_conversation**：按时间范围分区（月度/季度）

---

## 5. 缓存策略

### 5.1 Redis缓存使用场景

| 场景 | Key格式 | 过期时间 | 说明 |
|------|---------|----------|------|
| 用户信息 | user:info:{userId} | 1小时 | 用户基本信息 |
| 用户权限 | user:perms:{userId} | 1小时 | 用户权限列表 |
| 笔记详情 | note:detail:{noteId} | 30分钟 | 笔记完整信息 |
| 文件夹列表 | folder:list:{userId} | 30分钟 | 用户文件夹树 |
| 搜索结果 | search:{query}:{page} | 5分钟 | 搜索结果缓存 |
| AI对话历史 | ai:chat:{conversationId} | 1小时 | AI对话上下文 |

### 5.2 缓存更新策略

- **写时更新**：数据修改时同步更新缓存
- **定时刷新**：低频更新数据定时刷新
- **缓存穿透**：对不存在的key缓存空值
- **缓存雪崩**：设置随机过期时间

---

## 6. 文件存储方案

### 6.1 MinIO配置

```
Bucket: ai-notes
├── avatars/          # 用户头像
├── notes/            # 笔记图片
├── attachments/      # 附件文件
└── exports/          # 导出文件
```

### 6.2 文件上传流程

```
┌──────────┐    1. 选择文件    ┌──────────┐    2. 获取上传URL
│  客户端   │ ──────────────→ │  后端    │ ──────────────→
└──────────┘                └──────────┘                 │
    ↑                                                      │
    │ 3. 上传文件到MinIO                                   │
    │                                                      │
    │←───────────────────────────────────────────────────── │
    │ 4. 返回上传地址                                       │
    │                                                      │
    │ 5. 上传文件                                          │
    │→────────────────────────→ MinIO                      │
    │                                                      │
    │ 6. 返回文件访问URL                                   │
    │←───────────────────────────────────────────────────── │
    │ 7. 保存文件记录到数据库                               │
    │→─────────────────────────────────────────────────────→│
```

### 6.3 文件访问控制

- **私有文件**：通过后端代理访问，验证权限
- **公开文件**：直接访问MinIO预签名URL

---

## 7. API设计规范

### 7.1 RESTful API设计原则

- 使用HTTP动词表示操作：GET（查询）、POST（创建）、PUT（更新）、DELETE（删除）
- 使用名词表示资源：/notes、/folders、/users
- 使用复数形式：/notes 而非 /note
- 使用HTTP状态码表示结果：200（成功）、201（创建）、400（错误）、401（未授权）、404（不存在）、500（服务器错误）

### 7.2 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1234567890
}
```

### 7.3 错误码定义

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 500 | 服务器错误 |
| 10001 | 用户不存在 |
| 10002 | 密码错误 |
| 10003 | Token过期 |
| 20001 | 笔记不存在 |
| 20002 | 笔记已删除 |
| 30001 | AI服务调用失败 |

---

## 8. 安全设计

### 8.1 认证与授权

- **认证方式**：JWT Token
- **Token存储**：LocalStorage（HTTP Only Cookie可选）
- **Token刷新**：自动刷新机制
- **权限控制**：基于角色的访问控制（RBAC）

### 8.2 数据安全

- **密码加密**：BCrypt（加盐哈希）
- **敏感数据加密**：AES加密
- **HTTPS传输**：SSL/TLS加密
- **SQL注入防护**：参数化查询
- **XSS防护**：输入过滤、输出转义

### 8.3 API安全

- **请求限流**：Token Bucket算法
- **IP白名单**：管理接口限制
- **CORS配置**：允许的域名列表
- **CSRF防护**：Token验证

---

## 9. 性能优化

### 9.1 前端优化

- **代码分割**：路由懒加载
- **资源压缩**：Gzip/Brotli
- **图片优化**：WebP格式、懒加载
- **缓存策略**：强缓存 + 协商缓存
- **CDN加速**：静态资源CDN

### 9.2 后端优化

- **数据库优化**：索引优化、查询优化
- **缓存优化**：Redis多级缓存
- **连接池**：数据库连接池
- **异步处理**：耗时任务异步化
- **负载均衡**：多实例部署

### 9.3 数据库优化

- **索引优化**：合理创建索引
- **查询优化**：避免N+1查询
- **分页查询**：限制返回数据量
- **读写分离**：主从复制（可选）
- **分区表**：大表分区（可选）

---

## 10. 部署架构

### 10.1 单服务器部署

```
┌────────────────────────────────────────┐
│            云服务器                      │
│  ┌──────────────────────────────────┐  │
│  │  Nginx (80/443)                  │  │
│  │  - 反向代理                       │  │
│  │  - 静态资源                       │  │
│  └──────────────────────────────────┘  │
│  ┌──────────────────────────────────┐  │
│  │  Docker Compose                  │  │
│  │  ┌────────────┐  ┌────────────┐ │  │
│  │  │ Spring Boot│  │  Vue App   │ │  │
│  │  │  (8080)    │  │  (3000)    │ │  │
│  │  └────────────┘  └────────────┘ │  │
│  │  ┌────────────┐  ┌────────────┐ │  │
│  │  │   MySQL    │  │   Redis    │ │  │
│  │  │  (3306)    │  │  (6379)    │ │  │
│  │  └────────────┘  └────────────┘ │  │
│  │  ┌────────────┐                 │  │
│  │  │   MinIO    │                 │  │
│  │  │  (9000)    │                 │  │
│  │  └────────────┘                 │  │
│  └──────────────────────────────────┘  │
└────────────────────────────────────────┘
```

### 10.2 Docker Compose配置

```yaml
version: '3.8'

services:
  # 后端应用
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - REDIS_HOST=redis
      - MINIO_HOST=minio
    depends_on:
      - mysql
      - redis
      - minio
    volumes:
      - ./logs:/app/logs

  # 前端应用
  frontend:
    build: ./frontend
    ports:
      - "3000:3000"

  # MySQL数据库
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=ai_notes
    volumes:
      - mysql-data:/var/lib/mysql

  # Redis缓存
  redis:
    image: redis:7.0-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

  # MinIO对象存储
  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      - MINIO_ROOT_USER=admin
      - MINIO_ROOT_PASSWORD=admin123
    command: server /data --console-address ":9001"
    volumes:
      - minio-data:/data

  # Nginx
  nginx:
    image: nginx:1.24
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/ssl:/etc/nginx/ssl
      - ./frontend/dist:/usr/share/nginx/html
    depends_on:
      - backend
      - frontend

volumes:
  mysql-data:
  redis-data:
  minio-data:
```

---

## 11. 监控与日志

### 11.1 日志管理

- **日志级别**：ERROR、WARN、INFO、DEBUG
- **日志格式**：JSON格式，结构化日志
- **日志存储**：文件存储 + 日志收集（可选ELK）
- **日志保留**：保留30天

### 11.2 监控指标

- **系统指标**：CPU、内存、磁盘、网络
- **应用指标**：QPS、响应时间、错误率
- **业务指标**：用户数、笔记数、AI调用量

### 11.3 告警策略

- **CPU使用率**：> 80% 告警
- **内存使用率**：> 85% 告警
- **磁盘使用率**：> 90% 告警
- **API错误率**：> 1% 告警
- **响应时间**：> 3秒 告警

---

**文档结束**
