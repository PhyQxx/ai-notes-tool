# 🐛 Bug 清单 — AI Notes Tool

**提交人**: QA  
**日期**: 2026-04-03 11:49  
**状态**: 待修复

---

## Bug #1 — P1 严重：AI对话表缺少 title 列

| 属性 | 值 |
|------|-----|
| 模块 | AI对话管理 |
| 影响接口 | `POST /api/ai/conversations`（新建对话）、`PUT /api/ai/conversations/{id}/rename`（重命名） |
| 现象 | 返回500: `Unknown column 'title' in 'field list'` |
| 根因 | M5代码新增了 AIConversation.title 字段，但数据库 t_ai_conversation 表未执行DDL变更 |
| 修复方案 | 执行SQL: `ALTER TABLE t_ai_conversation ADD COLUMN title VARCHAR(200) DEFAULT '新对话' AFTER ai_model;` |
| 优先级 | 🔴 P1 — 阻塞上线 |

---

## Bug #2 — P2 一般：搜索接口不支持中文关键词

| 属性 | 值 |
|------|-----|
| 模块 | 笔记搜索 |
| 影响接口 | `GET /api/notes/search?keyword=xxx` |
| 现象 | 中文keyword返回HTTP 400，英文keyword正常返回200 |
| 根因 | URL编码问题，Spring MVC未正确处理中文参数 |
| 修复方案 | Controller参数加编码处理，或前端使用 `encodeURIComponent()` |
| 优先级 | 🟡 P2 — 不阻塞上线 |

---

## 修复后请通知 QA 回归测试
