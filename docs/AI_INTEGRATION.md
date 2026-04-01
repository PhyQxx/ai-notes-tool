# AI集成模块开发文档

## 概述

本文档描述了AI笔记工具中AI集成后端模块的完整实现。该模块集成了DeepSeek和智谱GLM两个AI服务提供商，提供了AI对话、内容生成、配置管理等功能。

## 完成的工作

### 1. Maven依赖添加

在`pom.xml`中添加了以下依赖：
- OkHttp 4.12.0 - HTTP客户端
- OkHttp SSE 4.12.0 - 服务端事件支持
- FastJSON2 2.0.47 - JSON处理库

### 2. 实体类 (Entity)

#### AIConversation.java
- 对应数据库表：`t_ai_conversation`
- 字段：id, userId, noteId, aiProvider, aiModel, messages(JSON), createdAt, updatedAt
- 包含内部类`AiMessage`用于表示单条消息

### 3. 数据传输对象 (DTO)

#### 请求DTO
- `AIConfigUpdateRequest.java` - AI配置更新请求
- `AIChatRequest.java` - AI对话请求
- `AIGenerateRequest.java` - AI内容生成请求

#### 响应DTO
- `AIChatResponse.java` - AI对话响应
- `AIConfigResponse.java` - AI配置响应

### 4. AI服务抽象层

#### AIProvider.java
- AI服务提供者接口
- 方法定义：chat(), chatStream(), getName(), getSupportedModels(), getDefaultModel()

#### DeepSeekProvider.java
- DeepSeek AI服务提供者实现
- 支持：deepseek-chat, deepseek-coder模型
- 使用OkHttp调用DeepSeek API

#### GLMProvider.java
- 智谱GLM AI服务提供者实现
- 支持：glm-4, glm-4-flash, glm-3-turbo模型
- 使用OkHttp调用智谱GLM API

#### AIProviderFactory.java
- AI服务提供者工厂类
- 根据提供商名称获取对应的Provider实例
- 支持动态设置API密钥

### 5. 数据访问层 (Mapper)

#### AIConversationMapper.java
- 继承MyBatis Plus的BaseMapper
- 提供基本的CRUD操作

### 6. 业务服务层 (Service)

#### AIService.java
- AI服务接口
- 定义了所有AI相关的业务方法

#### AIServiceImpl.java
- AI服务实现类
- 实现功能：
  - `chat()` - AI对话（同步）
  - `chatStream()` - AI流式对话（预留接口）
  - `generate()` - AI内容生成（支持：摘要、优化、扩写、改写、续写）
  - `getConversations()` - 获取对话列表（分页）
  - `getConversationDetail()` - 获取对话详情
  - `deleteConversation()` - 删除对话
  - `getConfig()` - 获取用户AI配置
  - `updateConfig()` - 更新用户AI配置
  - `getProviders()` - 获取可用提供商列表

### 7. 控制器层 (Controller)

#### AIController.java
- AI智能体控制器
- RESTful API接口：
  - `POST /api/ai/chat` - AI对话
  - `POST /api/ai/chat/stream` - AI流式对话（SSE）
  - `POST /api/ai/generate` - AI内容生成
  - `GET /api/ai/conversations` - 获取对话列表
  - `GET /api/ai/conversations/{id}` - 获取对话详情
  - `DELETE /api/ai/conversations/{id}` - 删除对话
  - `GET /api/ai/config` - 获取AI配置
  - `PUT /api/ai/config` - 更新AI配置
  - `GET /api/ai/providers` - 获取可用提供商列表

### 8. 配置类

#### AIConfig.java
- AI配置属性类
- 使用@ConfigurationProperties注解
- 配置项：
  - `ai.deepseek.base-url` - DeepSeek API基础URL
  - `ai.deepseek.api-key` - DeepSeek API密钥
  - `ai.deepseek.default-model` - 默认模型
  - `ai.deepseek.models` - 支持的模型列表
  - `ai.glm.base-url` - GLM API基础URL
  - `ai.glm.api-key` - GLM API密钥
  - `ai.glm.default-model` - 默认模型
  - `ai.glm.models` - 支持的模型列表

### 9. 配置文件更新

在`application.yml`中添加了AI配置：
```yaml
ai:
  deepseek:
    base-url: https://api.deepseek.com/v1
    api-key: ${DEEPSEEK_API_KEY:}
    default-model: deepseek-chat
    models:
      - deepseek-chat
      - deepseek-coder
  glm:
    base-url: https://open.bigmodel.cn/api/paas/v4
    api-key: ${GLM_API_KEY:}
    default-model: glm-4-flash
    models:
      - glm-4
      - glm-4-flash
      - glm-3-turbo
```

## 技术特点

### 1. 使用Lombok简化代码
- 所有实体类使用@Data注解
- Service使用@RequiredArgsConstructor进行依赖注入
- Controller使用@Slf4j进行日志记录

### 2. 完善的异常处理
- 使用自定义BusinessException处理业务异常
- 统一异常响应格式

### 3. 安全性考虑
- API Key存储在Redis中，不记录日志
- 用户配置隔离，每个用户独立的配置
- 用户权限验证（对话详情、删除操作）

### 4. 对话历史管理
- 对话消息存储在数据库JSON字段中
- 限制历史消息数量（最近20条）
- 支持基于笔记的对话

### 5. 配置管理
- 使用Redis缓存用户配置
- 支持用户自定义API密钥
- 配置TTL为1小时

### 6. HTTP客户端配置
- 连接超时：30秒
- 读取超时：60秒
- 写入超时：60秒

## 数据库设计

### t_ai_conversation表
已在`docs/sql/init.sql`中定义，字段：
- `id` - 主键
- `user_id` - 用户ID（外键）
- `note_id` - 关联笔记ID（外键）
- `ai_provider` - AI提供商
- `ai_model` - AI模型
- `messages` - 对话消息（JSON格式）
- `created_at` - 创建时间
- `updated_at` - 更新时间

## API使用示例

### 1. AI对话
```bash
POST /api/ai/chat
Content-Type: application/json
Authorization: Bearer <token>

{
  "provider": "deepseek",
  "model": "deepseek-chat",
  "message": "你好",
  "noteId": 123,
  "conversationId": 456
}
```

### 2. AI内容生成
```bash
POST /api/ai/generate
Content-Type: application/json
Authorization: Bearer <token>

{
  "noteId": 123,
  "provider": "glm",
  "model": "glm-4-flash",
  "type": "summarize",
  "prompt": "自定义提示词（可选）"
}
```

### 3. 获取AI配置
```bash
GET /api/ai/config
Authorization: Bearer <token>
```

### 4. 更新AI配置
```bash
PUT /api/ai/config
Content-Type: application/json
Authorization: Bearer <token>

{
  "provider": "deepseek",
  "model": "deepseek-chat",
  "deepseekApiKey": "your-api-key"
}
```

## 环境变量配置

在运行应用前，需要配置以下环境变量：

```bash
export DEEPSEEK_API_KEY=your-deepseek-api-key
export GLM_API_KEY=your-glm-api-key
```

或者直接在`application.yml`中配置API密钥（不推荐用于生产环境）。

## 后续扩展计划

### 1. 流式对话实现
- 当前chatStream()方法抛出异常，后续可使用SseEmitter实现
- 需要处理SSE连接管理和消息推送

### 2. AI功能增强
- 增加更多AI生成类型（如：翻译、语法检查等）
- 支持自定义提示词模板
- 支持多轮对话的上下文优化

### 3. 性能优化
- 添加对话缓存
- 实现AI调用的异步处理
- 添加请求限流

### 4. 监控和日志
- AI调用成功率统计
- API调用量监控
- 错误日志记录和分析

## 注意事项

1. **API密钥安全**
   - 生产环境务必使用环境变量配置API密钥
   - 不要将API密钥提交到版本控制
   - 用户配置的API密钥需要加密存储

2. **错误处理**
   - AI服务调用失败时，返回友好的错误信息
   - 记录详细的错误日志用于排查问题

3. **对话历史**
   - 对话历史会占用大量数据库空间，建议定期清理
   - 考虑添加对话归档功能

4. **成本控制**
   - AI调用会产生费用，建议添加调用次数限制
   - 实现用量统计功能

## 总结

AI集成模块已完整实现，包括：
- ✅ Maven依赖添加
- ✅ 实体类创建
- ✅ DTO类创建
- ✅ AI服务抽象层实现
- ✅ 业务服务层实现
- ✅ 控制器层实现
- ✅ 配置类创建
- ✅ 配置文件更新
- ✅ 完善的异常处理
- ✅ 日志记录

所有代码遵循项目规范，使用Lombok、MyBatis Plus、Spring Boot等技术栈，确保代码质量和可维护性。
