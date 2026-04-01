# AI笔记工具 - 产品需求文档 (PRD)

## 文档信息
- **项目名称**：AI笔记工具 (AI Notes)
- **版本**：v1.0
- **创建日期**：2026-04-01
- **文档状态**：草稿
- **作者**：PD团队

---

## 1. 项目概述

### 1.1 项目背景
随着人工智能技术的发展，智能笔记工具成为提升个人和团队工作效率的重要工具。本项目旨在开发一个集成AI智能体的现代化笔记应用，为用户提供智能化的笔记管理、内容生成和知识整理能力。

### 1.2 项目目标
- 构建一个功能完整、用户体验良好的Web端笔记工具
- 集成国内主流AI服务（DeepSeek、GLM等），提供智能化辅助
- 支持个人使用和小团队协作场景
- 实现Markdown和富文本双模式编辑
- 提供完整的版本管理和导出功能

### 1.3 目标用户
- **个人用户**：知识工作者、学生、研究人员，需要整理和记录知识
- **小团队**：3-20人的团队，需要共享笔记和协作编辑

---

## 2. 用户画像

### 2.1 个人用户 - 知识工作者
- **年龄**：25-45岁
- **职业**：程序员、产品经理、设计师、咨询师
- **需求**：
  - 快速记录想法和灵感
  - 整理技术文档和学习笔记
  - AI辅助生成和优化内容
  - 多设备同步访问

### 2.2 小团队用户
- **团队规模**：3-20人
- **典型场景**：产品研发团队、设计团队、项目管理团队
- **需求**：
  - 共享知识库
  - 协作编辑笔记
  - 权限管理
  - 团队知识沉淀

---

## 3. 功能需求

### 3.1 用户模块

#### 3.1.1 用户注册/登录
- 邮箱注册/登录
- 密码找回
- 第三方登录（可选：微信、GitHub）
- JWT Token认证

#### 3.1.2 个人信息管理
- 头像上传
- 昵称修改
- 密码修改

### 3.2 笔记管理

#### 3.2.1 笔记CRUD
- 创建笔记（空白笔记、模板创建）
- 编辑笔记（Markdown/富文本模式）
- 删除笔记（软删除，支持恢复）
- 查看笔记详情

#### 3.2.2 分类管理
- 文件夹分类
- 标签管理
- 多级文件夹支持

#### 3.2.3 搜索功能
- 全文搜索（标题、内容）
- 按标签筛选
- 按时间筛选
- 搜索历史

#### 3.2.4 笔记操作
- 收藏笔记
- 置顶笔记
- 复制/移动笔记
- 批量操作

### 3.3 编辑器功能

#### 3.3.1 Markdown编辑器
- 语法高亮
- 实时预览
- 快捷键支持
- 代码块支持
- 表格支持
- 图片插入（支持拖拽上传）
- 数学公式（LaTeX）

#### 3.3.2 富文本编辑器
- 所见即所得编辑
- 格式工具栏（加粗、斜体、颜色、对齐等）
- 插入图片/视频/链接
- 插入表格
- 撤销/重做

#### 3.3.3 编辑器通用功能
- 实时自动保存
- 离线编辑缓存
- 全屏模式
- 字体大小调整
- 夜间模式

### 3.4 AI智能体功能

#### 3.4.1 AI对话
- 与AI助手实时对话
- 上下文保持
- 对话历史记录

#### 3.4.2 AI辅助生成
- 基于提示词生成内容
- AI续写功能
- AI扩写功能
- AI润色优化

#### 3.4.3 AI智能处理
- 笔记摘要生成
- 关键词提取
- 内容改写
- 语法检查
- 格式化整理

#### 3.4.4 AI配置
- 多AI提供商支持（DeepSeek、GLM）
- AI模型选择
- API密钥管理
- 使用量统计

### 3.5 版本管理

#### 3.5.1 版本记录
- 自动保存历史版本
- 版本列表展示
- 版本对比功能
- 版本恢复功能

#### 3.5.2 版本操作
- 手动创建版本快照
- 添加版本备注
- 删除旧版本

### 3.6 文件管理

#### 3.6.1 文件上传
- 图片上传
- 文档上传
- 视频上传
- 文件大小限制（配置）

#### 3.6.2 文件管理
- 文件列表
- 文件预览
- 文件删除
- 存储空间统计

### 3.7 导出功能

#### 3.7.1 导出格式
- Markdown (.md)
- PDF
- Word (.docx)
- HTML

#### 3.7.2 导出选项
- 包含/排除图片
- 包含/排除附件
- 自定义样式

### 3.8 团队协作

#### 3.8.1 空间管理
- 创建团队空间
- 邀请成员
- 移除成员
- 成员角色管理

#### 3.8.2 权限管理
- 所有者
- 管理员
- 编辑者
- 查看者
- 自定义权限

#### 3.8.3 协作编辑
- 实时协作（可选，v2.0）
- 评论功能
- @提醒

### 3.9 系统设置

#### 3.9.1 用户设置
- 界面语言
- 主题切换（亮/暗）
- 字体大小
- 编辑器默认模式

#### 3.9.2 通知设置
- 邮件通知
- AI生成完成通知
- 团队协作通知

---

## 4. 非功能需求

### 4.1 性能要求
- 页面加载时间 < 2秒
- 编辑器响应时间 < 100ms
- 搜索响应时间 < 500ms
- AI生成时间 < 5秒（常规内容）

### 4.2 安全要求
- 密码加密存储（BCrypt）
- HTTPS加密传输
- JWT Token认证
- API请求频率限制
- SQL注入防护
- XSS防护

### 4.3 可用性要求
- 系统可用性 > 99%
- 数据备份：每日自动备份
- 灾难恢复：RTO < 1小时

### 4.4 兼容性要求
- 浏览器：Chrome 80+、Firefox 75+、Safari 13+、Edge 80+
- 移动端：响应式设计，支持主流移动浏览器

### 4.5 可扩展性要求
- 支持水平扩展
- 支持多AI提供商扩展
- 支持插件机制（可选，v2.0）

---

## 5. 技术架构

### 5.1 技术栈

#### 前端
- **框架**：Vue 3 + TypeScript
- **UI组件库**：Element Plus / Ant Design Vue
- **Markdown编辑器**：Vditor / ByteMD
- **富文本编辑器**：Tiptap / Quill
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP客户端**：Axios
- **构建工具**：Vite

#### 后端
- **框架**：Spring Boot 3.x + Java 17
- **数据库**：MySQL 8.0
- **ORM框架**：MyBatis Plus / JPA
- **缓存**：Redis
- **文件存储**：MinIO / 阿里云OSS
- **消息队列**：RabbitMQ（可选，用于异步任务）
- **API文档**：Swagger / Knife4j

#### AI集成
- **SDK**：各AI官方SDK
- **DeepSeek**：https://platform.deepseek.com/
- **智谱GLM**：https://open.bigmodel.cn/

### 5.2 系统架构图

```
┌─────────────────────────────────────────────────────┐
│                     客户端                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │  浏览器   │  │  移动端   │  │  桌面端   │          │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘          │
└───────┼────────────┼────────────┼──────────────────┘
        │            │            │
        └────────────┴────────────┘
                     │ HTTPS
        ┌────────────▼────────────┐
        │     Nginx 反向代理       │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │    Spring Boot 应用     │
        │  ┌──────────────────┐   │
        │  │   控制器层       │   │
        │  ├──────────────────┤   │
        │  │   业务逻辑层     │   │
        │  ├──────────────────┤   │
        │  │   数据访问层     │   │
        │  └──────────────────┘   │
        └────────────┬────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
   ┌────▼───┐  ┌────▼───┐  ┌────▼────┐
   │ MySQL  │  │ Redis  │  │ MinIO   │
   └────────┘  └────────┘  └─────────┘
                     │
        ┌────────────▼────────────┐
        │   外部服务              │
        │  ┌──────┐  ┌──────────┐ │
        │  │DeepSeek│ │   GLM    │ │
        │  └──────┘  └──────────┘ │
        └─────────────────────────┘
```

---

## 6. 数据库设计

### 6.1 核心数据表

#### 用户表 (t_user)
```sql
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    avatar VARCHAR(500) COMMENT '头像URL',
    nickname VARCHAR(50) COMMENT '昵称',
    status TINYINT DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) COMMENT '用户表';
```

#### 笔记表 (t_note)
```sql
CREATE TABLE t_note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    space_id BIGINT COMMENT '空间ID(团队协作)',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content LONGTEXT COMMENT '内容',
    content_type VARCHAR(20) DEFAULT 'markdown' COMMENT '内容类型 markdown/richtext',
    folder_id BIGINT COMMENT '文件夹ID',
    tags VARCHAR(500) COMMENT '标签(逗号分隔)',
    is_favorite TINYINT DEFAULT 0 COMMENT '是否收藏',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    status TINYINT DEFAULT 1 COMMENT '状态 1:正常 0:删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_space_id (space_id),
    INDEX idx_folder_id (folder_id),
    INDEX idx_status (status)
) COMMENT '笔记表';
```

#### 文件夹表 (t_folder)
```sql
CREATE TABLE t_folder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    space_id BIGINT COMMENT '空间ID',
    name VARCHAR(100) NOT NULL COMMENT '文件夹名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父文件夹ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) COMMENT '文件夹表';
```

#### 版本历史表 (t_note_version)
```sql
CREATE TABLE t_note_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    version_no INT NOT NULL COMMENT '版本号',
    title VARCHAR(200) COMMENT '标题',
    content LONGTEXT COMMENT '内容',
    remark VARCHAR(500) COMMENT '版本备注',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_note_id (note_id),
    INDEX idx_version_no (note_id, version_no)
) COMMENT '笔记版本历史表';
```

#### 附件表 (t_attachment)
```sql
CREATE TABLE t_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    note_id BIGINT COMMENT '关联笔记ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    file_type VARCHAR(50) COMMENT '文件类型',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_note_id (note_id)
) COMMENT '附件表';
```

#### AI对话表 (t_ai_conversation)
```sql
CREATE TABLE t_ai_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    note_id BIGINT COMMENT '关联笔记ID',
    ai_provider VARCHAR(50) NOT NULL COMMENT 'AI提供商 deepseek/glm',
    ai_model VARCHAR(100) NOT NULL COMMENT 'AI模型',
    messages JSON COMMENT '对话消息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_note_id (note_id)
) COMMENT 'AI对话表';
```

#### 团队空间表 (t_space)
```sql
CREATE TABLE t_space (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '空间名称',
    description VARCHAR(500) COMMENT '描述',
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    member_count INT DEFAULT 1 COMMENT '成员数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_owner_id (owner_id)
) COMMENT '团队空间表';
```

#### 空间成员表 (t_space_member)
```sql
CREATE TABLE t_space_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    space_id BIGINT NOT NULL COMMENT '空间ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) DEFAULT 'member' COMMENT '角色 owner/admin/member/viewer',
    status TINYINT DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_space_user (space_id, user_id),
    INDEX idx_user_id (user_id)
) COMMENT '空间成员表';
```

---

## 7. API设计

### 7.1 用户认证接口

```
POST   /api/auth/register    用户注册
POST   /api/auth/login       用户登录
POST   /api/auth/logout      用户登出
POST   /api/auth/refresh     刷新Token
GET    /api/auth/profile     获取个人信息
PUT    /api/auth/profile     更新个人信息
```

### 7.2 笔记接口

```
GET    /api/notes              获取笔记列表
POST   /api/notes              创建笔记
GET    /api/notes/{id}         获取笔记详情
PUT    /api/notes/{id}         更新笔记
DELETE /api/notes/{id}         删除笔记
POST   /api/notes/{id}/favorite 收藏/取消收藏
POST   /api/notes/{id}/top     置顶/取消置顶
GET    /api/notes/search       搜索笔记
```

### 7.3 文件夹接口

```
GET    /api/folders            获取文件夹列表
POST   /api/folders            创建文件夹
PUT    /api/folders/{id}       更新文件夹
DELETE /api/folders/{id}       删除文件夹
```

### 7.4 版本管理接口

```
GET    /api/notes/{id}/versions        获取版本列表
GET    /api/notes/{id}/versions/{vid}  获取版本详情
POST   /api/notes/{id}/versions        创建版本快照
POST   /api/notes/{id}/versions/{vid}/restore 恢复版本
```

### 7.5 AI接口

```
POST   /api/ai/chat              AI对话
POST   /api/ai/generate          AI生成内容
POST   /api/ai/summary           AI摘要
POST   /api/ai/optimize          AI优化
GET    /api/ai/conversations     获取对话历史
GET    /api/ai/config            获取AI配置
PUT    /api/ai/config            更新AI配置
```

### 7.6 文件上传接口

```
POST   /api/upload/image         上传图片
POST   /api/upload/file          上传文件
GET    /api/files                获取文件列表
DELETE /api/files/{id}           删除文件
```

### 7.7 导出接口

```
POST   /api/notes/{id}/export/md      导出Markdown
POST   /api/notes/{id}/export/pdf     导出PDF
POST   /api/notes/{id}/export/word    导出Word
```

### 7.8 团队协作接口

```
GET    /api/spaces              获取空间列表
POST   /api/spaces              创建空间
PUT    /api/spaces/{id}         更新空间
DELETE /api/spaces/{id}         删除空间
GET    /api/spaces/{id}/members 获取成员列表
POST   /api/spaces/{id}/members 邀请成员
DELETE /api/spaces/{id}/members/{userId} 移除成员
```

---

## 8. 开发计划

### 8.1 阶段划分

#### 第一阶段：MVP版本 (4周)
**目标**：实现核心笔记功能

**功能范围**：
- 用户注册/登录
- 笔记CRUD
- 文件夹管理
- Markdown编辑器
- 基础搜索
- 用户个人信息管理

#### 第二阶段：AI集成 (3周)
**目标**：集成AI智能体

**功能范围**：
- AI对话功能
- AI内容生成
- AI摘要和优化
- AI配置管理
- 多AI提供商支持（DeepSeek、GLM）

#### 第三阶段：高级功能 (4周)
**目标**：增强功能

**功能范围**：
- 富文本编辑器
- 版本管理
- 文件上传和附件
- 导出功能（PDF/Word）
- 搜索优化

#### 第四阶段：团队协作 (3周)
**目标**：支持小团队协作

**功能范围**：
- 团队空间
- 成员邀请和管理
- 权限管理
- 评论功能
- @提醒

#### 第五阶段：优化上线 (2周)
**目标**：性能优化和部署

**功能范围**：
- 性能优化
- 安全加固
- 压力测试
- 文档完善
- 云服务器部署

### 8.2 详细时间线

| 阶段 | 周次 | 任务 | 负责人 |
|------|------|------|--------|
| **第一阶段** | | | |
| | Week 1 | 需求评审、技术选型、项目初始化 | Dev |
| | Week 1-2 | 用户认证模块 | Dev |
| | Week 2-3 | 笔记CRUD、文件夹管理 | Dev |
| | Week 3-4 | Markdown编辑器集成 | Dev |
| | Week 4 | 基础搜索、单元测试 | Dev + QA |
| **第二阶段** | | | |
| | Week 5-6 | AI SDK集成 | Dev |
| | Week 5-6 | AI对话和生成功能 | Dev |
| | Week 6-7 | AI配置管理、多提供商支持 | Dev |
| | Week 7 | AI功能测试 | QA |
| **第三阶段** | | | |
| | Week 8-9 | 富文本编辑器集成 | Dev |
| | Week 9-10 | 版本管理功能 | Dev |
| | Week 10 | 文件上传、附件管理 | Dev |
| | Week 10-11 | 导出功能 | Dev |
| | Week 11 | 搜索优化 | Dev |
| **第四阶段** | | | |
| | Week 12-13 | 团队空间、成员管理 | Dev |
| | Week 13-14 | 权限管理、评论功能 | Dev |
| | Week 14 | 协作功能测试 | QA |
| **第五阶段** | | | |
| | Week 15 | 性能优化、安全加固 | Dev |
| | Week 15 | 压力测试 | QA |
| | Week 16 | 文档编写、云服务器部署 | Ops |
| | Week 16 | 上线验收、发布 | PM + QA + Ops |

### 8.3 里程碑

| 里程碑 | 时间 | 交付物 |
|--------|------|--------|
| M1: MVP完成 | Week 4 | 可运行的MVP版本 |
| M2: AI集成完成 | Week 7 | AI功能可用版本 |
| M3: 高级功能完成 | Week 11 | 功能完整版本 |
| M4: 协作功能完成 | Week 14 | 团队协作版本 |
| M5: 产品上线 | Week 16 | 正式发布版本 |

---

## 9. 风险评估

### 9.1 技术风险
- **风险**：AI服务稳定性
- **影响**：高
- **缓解措施**：支持多AI提供商，实现降级策略

### 9.2 性能风险
- **风险**：大量笔记数据导致查询变慢
- **影响**：中
- **缓解措施**：数据库索引优化、Redis缓存、分页加载

### 9.3 安全风险
- **风险**：用户数据泄露
- **影响**：高
- **缓解措施**：HTTPS传输、数据加密、权限控制

### 9.4 成本风险
- **风险**：AI服务调用成本过高
- **影响**：中
- **缓解措施**：实现用量统计、设置使用限制

---

## 10. 验收标准

### 10.1 功能验收
- ✅ 所有核心功能正常运行
- ✅ AI响应时间 < 5秒
- ✅ 文件上传成功率 > 99%
- ✅ 搜索准确率 > 90%

### 10.2 性能验收
- ✅ 页面加载时间 < 2秒
- ✅ 并发100用户响应时间 < 3秒
- ✅ API错误率 < 0.1%

### 10.3 安全验收
- ✅ 通过安全扫描
- ✅ 无SQL注入漏洞
- ✅ 无XSS漏洞
- ✅ 数据加密存储

---

## 11. 附录

### 11.1 参考资料
- Vue 3 官方文档：https://vuejs.org/
- Spring Boot 官方文档：https://spring.io/projects/spring-boot
- DeepSeek API文档：https://platform.deepseek.com/api-docs/
- 智谱GLM API文档：https://open.bigmodel.cn/dev/api

### 11.2 术语表
- **MVP**：Minimum Viable Product，最小可行产品
- **PRD**：Product Requirements Document，产品需求文档
- **AI**：Artificial Intelligence，人工智能
- **SDK**：Software Development Kit，软件开发工具包

---

**文档结束**
