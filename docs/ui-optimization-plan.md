# AI Notes Tool - UI 优化提升方案

> 基于 Vue 3 + Element Plus 2.13.6 + TypeScript 实际代码分析
> 分析日期：2026-04-04

---

## 一、当前 UI 现状分析

### 1.1 技术架构

- **布局**: `DefaultLayout.vue` 使用 Element Plus `el-container` + `el-aside` + `el-header` + `el-main`
- **主题**: 仅 `variables.scss` 定义了 Element Plus 默认变量，暗色模式通过 `html.dark` 类覆盖
- **样式**: 全局样式在 `global.scss`，组件样式均为 `scoped`
- **页面**: 12 个视图 + 20+ 组件

### 1.2 现有问题总结

| 问题类型 | 具体表现 | 严重程度 |
|---------|---------|---------|
| **品牌感弱** | 主色使用 Element Plus 默认蓝 `#409eff`，无品牌辨识度 | 🔴 |
| **间距不统一** | 各页面 margin/padding 硬编码，无设计令牌 | 🟡 |
| **暗色模式粗糙** | 仅覆盖了基础颜色变量，Canvas 等自定义绘制未适配 | 🔴 |
| **缺少动效** | 几乎无过渡动画，页面切换生硬 | 🟡 |
| **移动端适配差** | 仅有 `@media (max-width: 768px)` 对 aside 的基础处理 | 🔴 |
| **信息密度** | 编辑页 header 按钮过多（8+ 按钮），信息层级不清 | 🟡 |
| **登录页单调** | 渐变背景+白色卡片，缺少品牌元素和动态效果 | 🟡 |
| **字体层级** | 直接使用 Element Plus 默认字号，无自定义字体层级 | 🟡 |
| **组件风格** | 卡片、按钮圆角不一致（el-card 8px, el-button 6px） | 🟢 |

---

## 二、整体设计规范

### 2.1 色彩系统

在 `variables.scss` 中新增自定义设计令牌：

```scss
:root {
  // ===== 品牌色 =====
  --nt-primary: #5B7FFF;           // 主品牌蓝（更柔和、有辨识度）
  --nt-primary-light: #8CA4FF;
  --nt-primary-dark: #3D5FCC;
  --nt-primary-bg: #EEF2FF;        // 主色背景
  --nt-primary-bg-hover: #DDE5FF;

  // ===== 辅助色 =====
  --nt-success: #34D399;
  --nt-success-bg: #ECFDF5;
  --nt-warning: #FBBF24;
  --nt-warning-bg: #FFFBEB;
  --nt-danger: #F87171;
  --nt-danger-bg: #FEF2F2;
  --nt-info: #94A3B8;
  --nt-info-bg: #F8FAFC;

  // ===== 中性色 =====
  --nt-gray-50: #F8FAFC;
  --nt-gray-100: #F1F5F9;
  --nt-gray-200: #E2E8F0;
  --nt-gray-300: #CBD5E1;
  --nt-gray-400: #94A3B8;
  --nt-gray-500: #64748B;
  --nt-gray-600: #475569;
  --nt-gray-700: #334155;
  --nt-gray-800: #1E293B;
  --nt-gray-900: #0F172A;

  // ===== 语义色 =====
  --nt-text-primary: var(--nt-gray-900);
  --nt-text-regular: var(--nt-gray-600);
  --nt-text-secondary: var(--nt-gray-400);
  --nt-text-placeholder: var(--nt-gray-300);
  --nt-text-inverse: #FFFFFF;

  --nt-bg-page: var(--nt-gray-50);
  --nt-bg-card: #FFFFFF;
  --nt-bg-sidebar: #FFFFFF;
  --nt-bg-header: #FFFFFF;
  --nt-bg-hover: var(--nt-gray-100);
  --nt-bg-active: var(--nt-primary-bg);

  --nt-border-default: var(--nt-gray-200);
  --nt-border-light: var(--nt-gray-100);
  --nt-border-focus: var(--nt-primary);
}

// 暗色模式
html.dark {
  --nt-primary: #8CA4FF;
  --nt-primary-light: #A8BBFF;
  --nt-primary-dark: #5B7FFF;
  --nt-primary-bg: rgba(91, 127, 255, 0.12);
  --nt-primary-bg-hover: rgba(91, 127, 255, 0.2);

  --nt-success: #34D399;
  --nt-success-bg: rgba(52, 211, 153, 0.12);
  --nt-warning: #FBBF24;
  --nt-warning-bg: rgba(251, 191, 36, 0.12);
  --nt-danger: #F87171;
  --nt-danger-bg: rgba(248, 113, 113, 0.12);

  --nt-gray-50: #0F172A;
  --nt-gray-100: #1E293B;
  --nt-gray-200: #334155;
  --nt-gray-300: #475569;
  --nt-gray-400: #64748B;
  --nt-gray-500: #94A3B8;
  --nt-gray-600: #CBD5E1;
  --nt-gray-700: #E2E8F0;
  --nt-gray-800: #F1F5F9;
  --nt-gray-900: #F8FAFC;

  --nt-text-primary: var(--nt-gray-900);
  --nt-text-regular: var(--nt-gray-600);
  --nt-text-secondary: var(--nt-gray-400);
  --nt-text-placeholder: var(--nt-gray-300);

  --nt-bg-page: #0A0E1A;
  --nt-bg-card: #141B2D;
  --nt-bg-sidebar: #0F1525;
  --nt-bg-header: #0F1525;
  --nt-bg-hover: var(--nt-gray-100);
  --nt-bg-active: var(--nt-primary-bg);

  --nt-border-default: var(--nt-gray-200);
  --nt-border-light: var(--nt-gray-100);
}
```

同时覆盖 Element Plus 主题变量使其与新品牌色一致：

```scss
:root {
  --el-color-primary: var(--nt-primary);
  --el-color-primary-light-3: var(--nt-primary-light);
  --el-color-primary-light-5: #B8CAFF;
  --el-color-primary-light-7: #D6DEFF;
  --el-color-primary-light-8: var(--nt-primary-bg);
  --el-color-primary-light-9: var(--nt-primary-bg);
  --el-color-primary-dark-2: var(--nt-primary-dark);
  --el-bg-color: var(--nt-bg-card);
  --el-bg-color-page: var(--nt-bg-page);
  --el-text-color-primary: var(--nt-text-primary);
  --el-text-color-regular: var(--nt-text-regular);
  --el-text-color-secondary: var(--nt-text-secondary);
  --el-text-color-placeholder: var(--nt-text-placeholder);
  --el-border-color: var(--nt-border-default);
  --el-border-color-light: var(--nt-border-light);
}
```

### 2.2 字体层级

```scss
:root {
  // 字号
  --nt-font-size-h1: 28px;
  --nt-font-size-h2: 22px;
  --nt-font-size-h3: 18px;
  --nt-font-size-h4: 16px;
  --nt-font-size-body: 14px;
  --nt-font-size-caption: 12px;
  --nt-font-size-small: 11px;

  // 字重
  --nt-font-weight-normal: 400;
  --nt-font-weight-medium: 500;
  --nt-font-weight-semibold: 600;
  --nt-font-weight-bold: 700;

  // 行高
  --nt-line-height-tight: 1.25;
  --nt-line-height-normal: 1.5;
  --nt-line-height-relaxed: 1.75;
}
```

### 2.3 间距系统（4px 基础网格）

```scss
:root {
  --nt-space-1: 4px;
  --nt-space-2: 8px;
  --nt-space-3: 12px;
  --nt-space-4: 16px;
  --nt-space-5: 20px;
  --nt-space-6: 24px;
  --nt-space-8: 32px;
  --nt-space-10: 40px;
  --nt-space-12: 48px;
  --nt-space-16: 64px;
}
```

### 2.4 圆角规范

```scss
:root {
  --nt-radius-none: 0;
  --nt-radius-sm: 4px;      // 小元素：tag、badge
  --nt-radius-md: 8px;      // 卡片、按钮、输入框
  --nt-radius-lg: 12px;     // 弹窗、面板
  --nt-radius-xl: 16px;     // 大卡片、模态框
  --nt-radius-full: 9999px; // 头像、药丸
}
```

### 2.5 阴影层级

```scss
:root {
  --nt-shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.05);
  --nt-shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.08), 0 1px 2px rgba(0, 0, 0, 0.04);
  --nt-shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.08), 0 2px 4px -1px rgba(0, 0, 0, 0.04);
  --nt-shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -2px rgba(0, 0, 0, 0.03);
  --nt-shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.03);
}

html.dark {
  --nt-shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.2);
  --nt-shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.3), 0 1px 2px rgba(0, 0, 0, 0.2);
  --nt-shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.3), 0 2px 4px -1px rgba(0, 0, 0, 0.2);
  --nt-shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.3), 0 4px 6px -2px rgba(0, 0, 0, 0.2);
  --nt-shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.4), 0 10px 10px -5px rgba(0, 0, 0, 0.3);
}
```

### 2.6 动效规范

```scss
:root {
  --nt-duration-instant: 100ms;
  --nt-duration-fast: 200ms;
  --nt-duration-normal: 300ms;
  --nt-duration-slow: 500ms;

  --nt-ease-default: cubic-bezier(0.4, 0, 0.2, 1);
  --nt-ease-in: cubic-bezier(0.4, 0, 1, 1);
  --nt-ease-out: cubic-bezier(0, 0, 0.2, 1);
  --nt-ease-bounce: cubic-bezier(0.34, 1.56, 0.64, 1);
}
```

### 2.7 Element Plus 全局覆盖

在 `global.scss` 中添加：

```scss
// 统一圆角
.el-card { border-radius: var(--nt-radius-lg); }
.el-button { border-radius: var(--nt-radius-md); }
.el-input .el-input__wrapper { border-radius: var(--nt-radius-md); }
.el-dialog { border-radius: var(--nt-radius-xl); }
.el-drawer { border-radius: var(--nt-radius-xl); }
.el-tag { border-radius: var(--nt-radius-sm); }
.el-select .el-input__wrapper { border-radius: var(--nt-radius-md); }
.el-message-box { border-radius: var(--nt-radius-xl); }
.el-dropdown-menu { border-radius: var(--nt-radius-lg); }

// 统一过渡
.el-button,
.el-input__wrapper,
.el-card,
.el-tag {
  transition: all var(--nt-duration-fast) var(--nt-ease-default);
}
```

---

## 三、各页面优化方案

### 3.1 登录页 (`LoginView.vue`)

**当前问题：**
- 渐变背景 `linear-gradient(135deg, #667eea 0%, #764ba2 100%)` 与品牌色不一致
- 卡片无品牌 Logo 图标，辨识度低
- 无动效，视觉静态
- 暗色模式未适配（登录页背景永远紫色渐变）

**优化建议：**

```scss
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  // 改用品牌色渐变
  background: linear-gradient(135deg, var(--nt-primary) 0%, var(--nt-primary-dark) 100%);
  position: relative;
  overflow: hidden;

  // 背景装饰：浮动光斑
  &::before {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    top: -100px;
    right: -100px;
    animation: float 8s ease-in-out infinite;
  }
  &::after {
    content: '';
    position: absolute;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.05);
    bottom: -80px;
    left: -80px;
    animation: float 10s ease-in-out infinite reverse;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

.login-card {
  width: 420px;
  border-radius: var(--nt-radius-xl);
  box-shadow: var(--nt-shadow-xl);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  animation: cardEnter var(--nt-duration-slow) var(--nt-ease-out);

  .card-header {
    text-align: center;
    padding: 8px 0;

    // 新增品牌 Logo
    .logo-icon {
      width: 56px;
      height: 56px;
      border-radius: var(--nt-radius-lg);
      background: var(--nt-primary-bg);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;
      font-size: 28px;
    }

    h2 {
      font-size: var(--nt-font-size-h1);
      font-weight: var(--nt-font-weight-bold);
      color: var(--nt-text-primary);
    }

    p {
      font-size: var(--nt-font-size-body);
      color: var(--nt-text-secondary);
      margin-top: var(--nt-space-1);
    }
  }
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
```

**暗色模式登录页：** 不做特殊处理（保持渐变背景），卡片使用 `var(--nt-bg-card)` 背景色。

**模板改动：** 在 `.card-header` 中添加 Logo 图标元素。

---

### 3.2 全局导航布局 (`DefaultLayout.vue`)

**当前问题：**
- 侧边栏菜单项过多（8 个），视觉层级不清晰
- Logo 区无图标，仅文字
- 搜索框宽度固定 300px，不够灵活
- Header 右侧按钮紧凑，无分组
- 移动端侧边栏仅做了 `translateX` 基础处理

**优化建议：**

```scss
.default-layout {
  .sidebar {
    background-color: var(--nt-bg-sidebar);
    border-right: 1px solid var(--nt-border-default);
    transition: width var(--nt-duration-normal) var(--nt-ease-default);

    .logo {
      height: 64px; // 从 60px 调整
      display: flex;
      align-items: center;
      gap: var(--nt-space-3);
      padding: 0 var(--nt-space-4);
      border-bottom: 1px solid var(--nt-border-light);

      // 折叠时居中，展开时左对齐
      justify-content: flex-start;

      .logo-icon {
        width: 36px;
        height: 36px;
        min-width: 36px;
        border-radius: var(--nt-radius-md);
        background: var(--nt-primary-bg);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        color: var(--nt-primary);
        font-weight: var(--nt-font-weight-bold);
      }

      .logo-text {
        font-size: var(--nt-font-size-h4);
        font-weight: var(--nt-font-weight-bold);
        color: var(--nt-primary);
        white-space: nowrap;
        overflow: hidden;
      }
    }

    .new-note-btn {
      margin: var(--nt-space-4);
      width: calc(100% - var(--nt-space-8));
      border-radius: var(--nt-radius-md);
      font-weight: var(--nt-font-weight-medium);
      height: 40px;
    }

    // 菜单分区增加间距
    .menu-section {
      padding: var(--nt-space-2) var(--nt-space-3);
      border-top: 1px solid var(--nt-border-light);

      // 菜单分组标题
      .menu-group-label {
        font-size: var(--nt-font-size-caption);
        color: var(--nt-text-secondary);
        text-transform: uppercase;
        letter-spacing: 0.5px;
        padding: var(--nt-space-3) var(--nt-space-3) var(--nt-space-1);
        font-weight: var(--nt-font-weight-medium);
      }

      .menu-item {
        display: flex;
        align-items: center;
        gap: var(--nt-space-3);
        padding: var(--nt-space-2) var(--nt-space-3);
        border-radius: var(--nt-radius-md);
        cursor: pointer;
        transition: all var(--nt-duration-fast) var(--nt-ease-default);
        color: var(--nt-text-regular);
        font-size: var(--nt-font-size-body);
        position: relative;

        &:hover {
          background-color: var(--nt-bg-hover);
          color: var(--nt-text-primary);
        }

        &.active {
          background-color: var(--nt-bg-active);
          color: var(--nt-primary);
          font-weight: var(--nt-font-weight-medium);

          // 左侧激活指示条
          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 3px;
            height: 20px;
            border-radius: 2px;
            background: var(--nt-primary);
          }
        }
      }
    }
  }

  .header {
    height: 64px; // 固定高度
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 var(--nt-space-6);
    border-bottom: 1px solid var(--nt-border-light);
    background-color: var(--nt-bg-header);
    backdrop-filter: blur(8px);

    .header-left {
      flex: 1;
      max-width: 400px;

      .el-input {
        border-radius: var(--nt-radius-full);
        background: var(--nt-bg-hover);
        border: none;

        .el-input__wrapper {
          box-shadow: none;
          border-radius: var(--nt-radius-full);
          background: var(--nt-bg-hover);
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: var(--nt-space-2);

      // 按钮组添加分隔线
      .header-divider {
        width: 1px;
        height: 20px;
        background: var(--nt-border-default);
        margin: 0 var(--nt-space-2);
      }
    }
  }
}
```

**模板改动建议：**
- Logo 区添加图标 + 文字结构
- 菜单项分组（「导航」和「管理」两个 group）
- Header 右侧按钮分组间加分隔线

---

### 3.3 笔记列表页 (`NotesView.vue`)

**当前问题：**
- 工具栏元素太多（搜索框 + 范围 + 搜索按钮 + 文件夹 + 排序 + 新建 + 模板），一行放不下时换行不美观
- 卡片 hover 效果仅有 `translateY(-4px)`，缺少阴影变化
- 批量操作栏颜色生硬
- 标签筛选面板和列表之间缺少视觉分隔

**优化建议：**

```scss
.notes-view {
  .toolbar {
    // 改为粘性工具栏
    position: sticky;
    top: 0;
    z-index: 10;
    background: var(--nt-bg-page);
    padding: var(--nt-space-4) 0;
    margin-bottom: 0;

    // 分为两行：筛选条件 + 操作按钮
    flex-direction: column;
    align-items: stretch;

    .toolbar-top {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--nt-space-3);

      .filter-group {
        display: flex;
        align-items: center;
        gap: var(--nt-space-2);
        flex-wrap: wrap;
      }

      .action-group {
        display: flex;
        align-items: center;
        gap: var(--nt-space-2);
      }
    }
  }

  .notes-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: var(--nt-space-5);

    .note-card {
      cursor: pointer;
      border-radius: var(--nt-radius-lg);
      border: 1px solid var(--nt-border-light);
      background: var(--nt-bg-card);
      transition: all var(--nt-duration-fast) var(--nt-ease-default);

      &:hover {
        transform: translateY(-2px);
        box-shadow: var(--nt-shadow-md);
        border-color: var(--nt-border-focus);
      }

      // 减少动效（无障碍）
      @media (prefers-reduced-motion: reduce) {
        &:hover { transform: none; }
      }
    }
  }

  .batch-bar {
    padding: var(--nt-space-3) var(--nt-space-4);
    background: var(--nt-primary-bg);
    border: 1px solid rgba(var(--nt-primary-rgb, 91, 127, 255), 0.2);
    border-radius: var(--nt-radius-md);
    margin-bottom: var(--nt-space-4);
  }
}
```

---

### 3.4 笔记编辑页 (`NoteEditView.vue`)

**当前问题：**
- 编辑器头部按钮过多（收藏、置顶、版本、评论、反向链接、导出、分享、AI助手、保存 = 9 个按钮），视觉混乱
- 标题输入框固定 400px，不够灵活
- 标签栏和编辑器模式切换位置不自然
- `editorHeight: calc(100vh - 180px)` 硬编码

**优化建议：**

```scss
.note-edit-view {
  .editor-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: var(--nt-space-3) var(--nt-space-6);
    border-bottom: 1px solid var(--nt-border-light);
    background-color: var(--nt-bg-header);
    height: 56px;

    .header-left {
      min-width: 200px;
    }

    .header-center {
      flex: 1;
      max-width: 600px;

      .el-input {
        .el-input__wrapper {
          box-shadow: none;
          border-bottom: 2px solid transparent;
          border-radius: 0;
          transition: border-color var(--nt-duration-fast);

          &:focus-within {
            border-bottom-color: var(--nt-primary);
          }
        }

        // 标题字号
        :deep(.el-input__inner) {
          font-size: var(--nt-font-size-h3);
          font-weight: var(--nt-font-weight-semibold);
          text-align: center;
        }
      }
    }

    .header-right {
      display: flex;
      gap: var(--nt-space-1);

      // 将次要按钮收入「更多」下拉
      .primary-actions {
        display: flex;
        gap: var(--nt-space-2);
      }
    }
  }

  .editor-toolbar {
    display: flex;
    align-items: center;
    gap: var(--nt-space-2);
    padding: var(--nt-space-2) var(--nt-space-6);
    border-bottom: 1px solid var(--nt-border-light);
    background-color: var(--nt-bg-card);
    min-height: 44px;
  }

  .editor-container {
    flex: 1;
    overflow: hidden;
    // 使用 flex-1 自适应而非 calc
  }
}
```

**模板改动建议：**
- 将「版本历史」「反向链接」「导出」收入 `el-dropdown` 的「更多操作」菜单
- 标题输入框改为 `flex: 1; max-width: 600px`
- 保存按钮固定在 header 右侧，使用 `type="primary"` + 醒目样式
- 编辑器模式切换（Markdown/富文本）移到工具栏左侧

---

### 3.5 AI 助手面板 (`AIAssistant.vue` + `AIChatView.vue`)

**当前问题：**
- Drawer 面板内对话列表 + 主内容区并排，600px 宽度内拥挤
- 无对话气泡样式区分（用户 vs AI）
- 快捷操作按钮堆叠

**优化建议：**

```scss
// AI 消息气泡样式
.assistant-content {
  .message-list {
    padding: var(--nt-space-4);

    .message-item {
      display: flex;
      gap: var(--nt-space-3);
      margin-bottom: var(--nt-space-4);

      &.user {
        flex-direction: row-reverse;

        .message-bubble {
          background: var(--nt-primary);
          color: white;
          border-radius: var(--nt-radius-lg) var(--nt-radius-lg) var(--nt-radius-sm) var(--nt-radius-lg);
        }
      }

      &.assistant {
        .message-bubble {
          background: var(--nt-bg-hover);
          color: var(--nt-text-primary);
          border-radius: var(--nt-radius-lg) var(--nt-radius-lg) var(--nt-radius-lg) var(--nt-radius-sm);
        }
      }

      .message-avatar {
        width: 32px;
        height: 32px;
        border-radius: var(--nt-radius-full);
        flex-shrink: 0;
      }

      .message-bubble {
        padding: var(--nt-space-3) var(--nt-space-4);
        max-width: 85%;
        line-height: var(--nt-line-height-relaxed);
        font-size: var(--nt-font-size-body);

        // Markdown 内容样式
        :deep(pre) {
          background: var(--nt-gray-100);
          border-radius: var(--nt-radius-md);
          padding: var(--nt-space-3);
          overflow-x: auto;
        }

        :deep(code) {
          font-size: 13px;
        }
      }
    }
  }
}
```

**Drawer 尺寸调整：** 从 `600px` 改为 `45%`（最小 500px），或改为右侧滑出面板而非 Drawer。

---

### 3.6 知识图谱页 (`GraphView.vue`)

**当前问题：**
- Canvas 绘制颜色硬编码 `#409eff`、`#67c23a`、`#303133`，暗色模式下不可读
- 节点样式单一（蓝色圆形），无标签信息
- 缺少缩放和平移功能
- 空状态提示不够美观

**优化建议：**

**JS 代码改动：**
```typescript
// 获取当前主题色
function getGraphColors() {
  const isDark = document.documentElement.classList.contains('dark');
  return {
    node: isDark ? '#8CA4FF' : '#5B7FFF',
    nodeStroke: isDark ? '#141B2D' : '#FFFFFF',
    text: isDark ? '#E2E8F0' : '#1E293B',
    linkEdge: isDark ? '#8CA4FF' : '#5B7FFF',
    tagEdge: isDark ? '#34D399' : '#10B981',
    background: isDark ? '#0A0E1A' : '#F8FAFC',
    hover: isDark ? '#A8BBFF' : '#3D5FCC',
  };
}
```

**Canvas 样式改进：**
- 节点根据标签数量显示不同颜色（使用标签颜色映射）
- 节点 hover 时显示 tooltip（标题 + 标签列表）
- 添加鼠标滚轮缩放 + 拖拽平移（当前仅支持拖拽单个节点）
- 连线使用曲线（贝塞尔）而非直线

**CSS 补充：**
```scss
.graph-view {
  .graph-header {
    padding: var(--nt-space-4) 0;
    border-bottom: 1px solid var(--nt-border-light);
    margin-bottom: var(--nt-space-4);
  }

  .graph-container {
    border: 1px solid var(--nt-border-light);
    border-radius: var(--nt-radius-lg);
    background: var(--nt-bg-card);
  }

  .graph-empty {
    flex-direction: column;
    gap: var(--nt-space-3);

    // 使用图标 + 文字而非纯文字
    .empty-icon {
      font-size: 48px;
      color: var(--nt-text-placeholder);
    }
  }
}
```

---

### 3.7 设置页 (`SettingsView.vue`)

**当前问题：**
- 最大宽度 `800px` 偏窄，表单浪费空间
- Tab 标签页无图标，视觉单调
- AI 设置 Tab 的 API Key 输入框缺少安全提示

**优化建议：**

```scss
.settings-view {
  max-width: 960px;
  margin: 0 auto;

  .settings-card {
    border-radius: var(--nt-radius-xl);
    border: none;
    box-shadow: var(--nt-shadow-sm);

    .card-header {
      padding: var(--nt-space-6) var(--nt-space-6) var(--nt-space-4);
      border-bottom: 1px solid var(--nt-border-light);

      h3 {
        font-size: var(--nt-font-size-h2);
        font-weight: var(--nt-font-weight-semibold);
      }
    }
  }
}
```

**模板改动：**
- Tab 标签添加图标（`<User />`、`<Lock />`、`<Brush />`、`<MagicStick />`）
- AI 设置增加「连接状态」指示器
- 表单 `label-width` 从 100px/120px 统一为 140px

---

### 3.8 首页 (`HomeView.vue`)

**当前问题：**
- 欢迎区文字和统计卡片之间缺少视觉过渡
- 统计卡片使用 `el-col :span="8"` 在小屏下不自适应
- 「猜你喜欢」推荐区的标题使用了 emoji `💡`，风格不统一

**优化建议：**

```scss
.home-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--nt-space-6) var(--nt-space-8);

  .welcome-section {
    margin-bottom: var(--nt-space-8);

    h2 {
      font-size: var(--nt-font-size-h1);
      font-weight: var(--nt-font-weight-bold);
    }

    p {
      font-size: var(--nt-font-size-h4);
      color: var(--nt-text-secondary);
    }

    // 新增：快捷操作按钮组
    .quick-actions {
      display: flex;
      gap: var(--nt-space-3);
      margin-top: var(--nt-space-5);
    }
  }

  .stats-row {
    margin-bottom: var(--nt-space-8);

    // 改为响应式网格
    .stat-card {
      border-radius: var(--nt-radius-lg);
      border: 1px solid var(--nt-border-light);
      transition: all var(--nt-duration-fast) var(--nt-ease-default);

      &:hover {
        box-shadow: var(--nt-shadow-md);
        transform: translateY(-2px);
      }
    }
  }
}
```

**模板改动：**
- 统计卡片改用 CSS Grid 替代 `el-row/el-col`
- 欢迎区添加快捷操作（新建笔记、搜索、AI 助手）
- 移除 emoji，使用 Element Plus 图标

---

### 3.9 全局搜索 Command Palette (`DefaultLayout.vue`)

**当前问题：**
- `el-dialog` 宽度 560px，样式与全局设计不一致
- 搜索结果列表无键盘导航指示
- 无搜索历史记录

**优化建议：**

```scss
// 覆盖 Command Palette 对话框样式
.command-palette {
  .el-dialog {
    border-radius: var(--nt-radius-xl);
    overflow: hidden;
    box-shadow: var(--nt-shadow-xl);
    border: 1px solid var(--nt-border-default);
  }

  .el-dialog__header {
    display: none; // 无标题
  }

  .el-dialog__body {
    padding: var(--nt-space-3);
  }

  .command-results {
    margin-top: var(--nt-space-2);
    max-height: 400px;
    overflow-y: auto;

    .command-item {
      padding: var(--nt-space-3) var(--nt-space-4);
      border-radius: var(--nt-radius-md);
      cursor: pointer;
      transition: background-color var(--nt-duration-instant);

      &:hover,
      &.active {
        background-color: var(--nt-bg-hover);
      }

      .command-item-title {
        font-size: var(--nt-font-size-body);
        font-weight: var(--nt-font-weight-medium);
        color: var(--nt-text-primary);
      }

      .command-item-preview {
        font-size: var(--nt-font-size-caption);
        color: var(--nt-text-secondary);
      }
    }
  }
}
```

**功能改进建议：**
- 添加键盘上下选择 + Enter 确认
- 搜索结果高亮匹配关键词

---

### 3.10 评论/分享组件

**CommentPanel.vue 当前问题：**
- 评论面板使用 `el-drawer direction="btt"`（底部抽屉），宽度不合适
- 评论输入区与列表混在一起，视觉层次不清

**优化建议：**
```scss
// 改为右侧 Drawer
.comment-panel {
  .comment-item {
    padding: var(--nt-space-4) 0;
    border-bottom: 1px solid var(--nt-border-light);

    &:last-child { border-bottom: none; }

    .comment-header {
      display: flex;
      align-items: center;
      gap: var(--nt-space-2);
      margin-bottom: var(--nt-space-2);
    }

    .comment-body {
      padding-left: 40px; // avatar + gap 的偏移
      font-size: var(--nt-font-size-body);
      line-height: var(--nt-line-height-relaxed);
      color: var(--nt-text-regular);
    }

    .comment-time {
      font-size: var(--nt-font-size-caption);
      color: var(--nt-text-placeholder);
    }
  }
}
```

**分享弹窗：** 当前实现合理，仅调整样式：
- 分享链接区域添加复制成功的视觉反馈（绿色勾号动画）
- 已创建分享列表使用卡片化布局

---

## 四、响应式适配建议

### 4.1 断点定义

```scss
// 新增到 variables.scss
$breakpoints: (
  sm: 640px,
  md: 768px,
  lg: 1024px,
  xl: 1280px,
  xxl: 1536px,
);

@mixin sm { @media (max-width: 640px) { @content; } }
@mixin md { @media (max-width: 768px) { @content; } }
@mixin lg { @media (max-width: 1024px) { @content; } }
@mixin xl { @media (max-width: 1280px) { @content; } }
```

### 4.2 侧边栏折叠策略

```scss
@include lg {
  .default-layout {
    .sidebar {
      // lg 以下默认折叠，hover 展开
      width: 64px !important;

      .logo-text,
      .new-note-btn span,
      .folder-section,
      .menu-item span,
      .menu-group-label {
        display: none;
      }
    }
  }
}

@include md {
  .default-layout {
    .sidebar {
      // md 以下隐藏侧边栏，使用汉堡菜单触发
      position: fixed;
      z-index: 1000;
      transform: translateX(-100%);
      transition: transform var(--nt-duration-normal) var(--nt-ease-default);

      &.is-open {
        transform: translateX(0);
      }
    }

    // 添加遮罩层
    .sidebar-overlay {
      display: block;
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      z-index: 999;
    }
  }
}
```

### 4.3 编辑器适配

```scss
@include md {
  .note-edit-view {
    .editor-header {
      flex-wrap: wrap;
      height: auto;
      padding: var(--nt-space-3);

      .header-left { display: none; }
      .header-center { order: 1; max-width: 100%; }
      .header-right { order: 2; width: 100%; justify-content: flex-end; }
    }

    // AI 助手 Drawer 改为全屏
    .el-drawer {
      width: 100% !important;
    }
  }
}
```

### 4.4 移动端布局

```scss
@include sm {
  // 首页统计卡片改为单列
  .home-view .stats-row {
    .el-col { width: 100%; margin-bottom: var(--nt-space-3); }
  }

  // 笔记列表改为单列
  .notes-view .notes-list {
    grid-template-columns: 1fr;
  }

  // Command Palette 全屏
  .command-palette .el-dialog {
    width: 100% !important;
    margin: 0;
    border-radius: 0;
    max-height: 100vh;
  }

  // 设置页全宽
  .settings-view { max-width: 100%; }
}
```

---

## 五、全局动效补充

### 5.1 页面切换动画

在 `App.vue` 中为 `router-view` 添加过渡：

```vue
<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--nt-duration-normal) var(--nt-ease-default);
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
```

### 5.2 列表项入场动画

```scss
// 笔记列表卡片入场
.notes-list .note-card {
  animation: slideUp var(--nt-duration-normal) var(--nt-ease-out) both;

  @for $i from 1 through 20 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 30}ms;
    }
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

### 5.3 骨架屏优化

当前使用 `el-skeleton`，建议在笔记列表中增加骨架屏的过渡效果：

```scss
.skeleton-list {
  animation: skeletonPulse 1.5s ease-in-out infinite;
}

@keyframes skeletonPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
```

---

## 六、优先级排序

| 优先级 | 改动项 | 预计工时 | 影响范围 |
|-------|-------|---------|---------|
| **P0** | 品牌色 + 设计令牌系统 | 0.5d | 全局 |
| **P0** | 暗色模式适配（Canvas、硬编码颜色） | 0.5d | GraphView |
| **P1** | Element Plus 全局覆盖（圆角、阴影） | 0.5d | 全局 |
| **P1** | 侧边栏视觉优化 + 菜单分组 | 0.5d | DefaultLayout |
| **P1** | 编辑页头部按钮整理 | 0.5d | NoteEditView |
| **P1** | 响应式断点 + 移动端适配 | 1d | 全局 |
| **P2** | 登录页视觉升级 | 0.5d | LoginView |
| **P2** | AI 助手消息气泡样式 | 0.5d | AIAssistant |
| **P2** | 页面切换 + 列表入场动画 | 0.5d | App + NotesView |
| **P3** | Command Palette 增强 | 0.5d | DefaultLayout |
| **P3** | 设置页 + 首页细节优化 | 0.5d | Settings + Home |
| **P3** | 评论/分享组件优化 | 0.5d | Comment + Share |

**总计预估：约 6 个工作日**

---

## 七、实施建议

1. **Phase 1（P0）**: 建立设计令牌系统 → 全局生效，后续改动基于此
2. **Phase 2（P1）**: 核心交互优化（导航、编辑器、响应式）
3. **Phase 3（P2-P3）**: 细节打磨和动效补充

每个 Phase 完成后进行视觉走查，确保与 Element Plus 设计语言一致且暗色模式完整可用。
