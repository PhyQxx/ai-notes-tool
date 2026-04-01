# AI笔记工具 - 高级功能后端API需求

## 文档说明

本文档列出了前端高级功能所需的REST API接口规范，后端开发人员请按照此规范实现对应的接口。

---

## 1. 版本管理接口

### 1.1 获取版本列表

**接口地址**: `GET /api/notes/{id}/versions`

**请求参数**:
- `id` (路径参数): 笔记ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "noteId": 100,
      "versionNo": 1,
      "title": "笔记标题",
      "content": "笔记内容",
      "remark": "首次创建",
      "createdBy": 1,
      "createdAt": "2026-04-01T12:00:00"
    }
  ],
  "timestamp": 1722556800000
}
```

### 1.2 获取版本详情

**接口地址**: `GET /api/notes/{id}/versions/{versionId}`

**请求参数**:
- `id` (路径参数): 笔记ID
- `versionId` (路径参数): 版本ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "noteId": 100,
    "versionNo": 1,
    "title": "笔记标题",
    "content": "笔记内容",
    "remark": "首次创建",
    "createdBy": 1,
    "createdAt": "2026-04-01T12:00:00"
  },
  "timestamp": 1722556800000
}
```

### 1.3 创建版本快照

**接口地址**: `POST /api/notes/{id}/versions`

**请求参数**:
- `id` (路径参数): 笔记ID
- `remark` (请求体, 可选): 版本备注

**请求体示例**:
```json
{
  "remark": "重要修改"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "版本创建成功",
  "data": null,
  "timestamp": 1722556800000
}
```

### 1.4 恢复版本

**接口地址**: `POST /api/notes/{id}/versions/{versionId}/restore`

**请求参数**:
- `id` (路径参数): 笔记ID
- `versionId` (路径参数): 版本ID

**响应示例**:
```json
{
  "code": 200,
  "message": "版本恢复成功",
  "data": null,
  "timestamp": 1722556800000
}
```

### 1.5 删除版本

**接口地址**: `DELETE /api/notes/{id}/versions/{versionId}`

**请求参数**:
- `id` (路径参数): 笔记ID
- `versionId` (路径参数): 版本ID

**响应示例**:
```json
{
  "code": 200,
  "message": "版本删除成功",
  "data": null,
  "timestamp": 1722556800000
}
```

---

## 2. 导出功能接口

### 2.1 导出为Markdown

**接口地址**: `POST /api/notes/{id}/export/md`

**请求参数**:
- `id` (路径参数): 笔记ID

**响应类型**: `application/octet-stream` 或 `text/markdown`

**响应**: 文件流

**响应头**:
```
Content-Disposition: attachment; filename="笔记标题.md"
Content-Type: text/markdown
```

### 2.2 导出为PDF

**接口地址**: `POST /api/notes/{id}/export/pdf`

**请求参数**:
- `id` (路径参数): 笔记ID

**响应类型**: `application/pdf`

**响应**: 文件流

**响应头**:
```
Content-Disposition: attachment; filename="笔记标题.pdf"
Content-Type: application/pdf
```

### 2.3 导出为Word

**接口地址**: `POST /api/notes/{id}/export/word`

**请求参数**:
- `id` (路径参数): 笔记ID

**响应类型**: `application/vnd.openxmlformats-officedocument.wordprocessingml.document`

**响应**: 文件流

**响应头**:
```
Content-Disposition: attachment; filename="笔记标题.docx"
Content-Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document
```

---

## 3. 图片上传接口

### 3.1 上传图片

**接口地址**: `POST /api/upload/image`

**请求类型**: `multipart/form-data`

**请求参数**:
- `file`: 图片文件

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://example.com/uploads/image-20260401-120000.jpg",
    "fileName": "image-20260401-120000.jpg",
    "fileSize": 102400
  },
  "timestamp": 1722556800000
}
```

**注意**:
- 支持的图片格式: jpg, jpeg, png, gif, webp
- 文件大小限制: 建议10MB以内
- 返回的URL应该是可访问的完整URL

---

## 4. 通用响应格式

所有API接口都应遵循以下统一响应格式：

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1722556800000
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误描述",
  "data": null,
  "timestamp": 1722556800000
}
```

### 状态码说明
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 权限不足
- `404`: 资源不存在
- `500`: 服务器内部错误

---

## 5. 认证说明

### JWT Token认证

所有需要认证的接口都应在请求头中携带JWT Token：

```
Authorization: Bearer <token>
```

### Token刷新

如果Token过期，应返回401状态码，前端会自动调用刷新接口获取新Token。

---

## 6. 数据类型说明

### 版本类型 (NoteVersion)

```typescript
interface NoteVersion {
  id: number;           // 版本ID
  noteId: number;       // 笔记ID
  versionNo: number;    // 版本号（从1开始递增）
  title: string;        // 标题
  content: string;      // 内容
  remark: string;       // 版本备注
  createdBy: number;    // 创建人ID
  createdAt: string;    // 创建时间（ISO 8601格式）
}
```

---

## 7. 实现建议

### 7.1 版本管理
- 使用数据库表 `t_note_version` 存储版本历史
- 版本号自动递增，从1开始
- 建议限制每个笔记最多保存50个版本
- 超出限制时自动删除最旧的版本

### 7.2 导出功能
- Markdown导出：直接返回Markdown格式文本
- PDF导出：使用 wkhtmltopdf 或 jsPDF 等库生成PDF
- Word导出：使用 Apache POI 或 docx 等库生成Word文档
- 建议使用异步任务处理大文件导出

### 7.3 图片上传
- 使用对象存储服务（如MinIO、阿里云OSS）
- 生成唯一文件名，避免文件名冲突
- 建议压缩图片，节省存储空间
- 记录文件上传日志，便于审计

### 7.4 性能优化
- 版本列表查询使用分页（每页20条）
- 导出功能使用流式响应，避免内存溢出
- 图片上传使用CDN加速
- 添加Redis缓存，提升查询性能

---

## 8. 测试建议

### 8.1 版本管理测试用例
- [ ] 创建版本快照
- [ ] 获取版本列表（空列表、多个版本）
- [ ] 恢复到指定版本
- [ ] 删除版本
- [ ] 版本号自动递增
- [ ] 版本备注功能

### 8.2 导出功能测试用例
- [ ] 导出Markdown文件
- [ ] 导出PDF文件
- [ ] 导出Word文件
- [ ] 文件名编码（中文文件名）
- [ ] 大文件导出（超过10MB）
- [ ] 空内容导出

### 8.3 图片上传测试用例
- [ ] 上传常见图片格式（jpg, png, gif）
- [ ] 上传大文件（超过限制）
- [ ] 上传非图片文件
- [ ] 上传重复文件名
- [ ] URL访问测试

---

## 9. 安全建议

### 9.1 认证授权
- 所有接口都需要JWT Token认证
- 验证用户是否有权限访问指定笔记
- 版本恢复和删除需要笔记所有者权限

### 9.2 文件上传
- 验证文件类型（MIME Type）
- 限制文件大小
- 扫描病毒（可选）
- 防止路径遍历攻击

### 9.3 导出功能
- 验证笔记所有权
- 限制导出频率（防止滥用）
- 记录导出日志

---

## 10. 兼容性说明

### 10.1 数据库
- MySQL 8.0+
- 建议使用utf8mb4字符集

### 10.2 对象存储
- MinIO
- 阿里云OSS
- 腾讯云COS
- AWS S3

### 10.3 文件格式
- PDF: ISO 32000-1:2008
- Word: Office Open XML (.docx)
- Markdown: CommonMark规范

---

## 联系方式

如有问题，请联系：
- 前端开发: Dev
- 项目负责人: PD

---

**文档版本**: v1.0
**创建日期**: 2026-04-01
**最后更新**: 2026-04-01
