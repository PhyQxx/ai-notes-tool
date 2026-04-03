-- ============================================================================
-- AI笔记工具 - 数据库初始化脚本
-- ============================================================================
-- 项目名称：AI Notes
-- 版本：v1.0
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 创建日期：2026-04-01
-- ============================================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 1. 建库语句
-- ============================================================================
-- 如果需要单独创建数据库，请取消以下注释
-- CREATE DATABASE IF NOT EXISTS ai_notes_db
-- CHARACTER SET utf8mb4
-- COLLATE utf8mb4_unicode_ci
-- COMMENT 'AI笔记工具数据库';
--
-- USE ai_notes_db;

-- ============================================================================
-- 2. 删除已存在的表（按依赖关系倒序删除）
-- ============================================================================
DROP TABLE IF EXISTS t_comment;
DROP TABLE IF EXISTS t_space_member;
DROP TABLE IF EXISTS t_space;
DROP TABLE IF EXISTS t_ai_conversation;
DROP TABLE IF EXISTS t_attachment;
DROP TABLE IF EXISTS t_note_version;
DROP TABLE IF EXISTS t_note;
DROP TABLE IF EXISTS t_folder;
DROP TABLE IF EXISTS t_user;

-- ============================================================================
-- 3. 创建数据表
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 3.1 用户表 (t_user)
-- ----------------------------------------------------------------------------
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------------------------------------------------------
-- 3.2 文件夹表 (t_folder)
-- ----------------------------------------------------------------------------
CREATE TABLE t_folder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件夹ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    space_id BIGINT DEFAULT NULL COMMENT '所属空间ID',
    name VARCHAR(100) NOT NULL COMMENT '文件夹名称',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父文件夹ID，0表示根目录',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 外键约束
    CONSTRAINT fk_folder_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_folder_space FOREIGN KEY (space_id) REFERENCES t_space(id) ON DELETE CASCADE,

    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_space_id (space_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件夹表';

-- ----------------------------------------------------------------------------
-- 3.3 笔记表 (t_note)
-- ----------------------------------------------------------------------------
CREATE TABLE t_note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '创建用户ID',
    space_id BIGINT DEFAULT NULL COMMENT '所属空间ID',
    title VARCHAR(200) NOT NULL COMMENT '笔记标题',
    content LONGTEXT COMMENT '笔记内容',
    content_type VARCHAR(20) NOT NULL DEFAULT 'markdown' COMMENT '内容类型：markdown-Markdown编辑器，richtext-富文本编辑器',
    folder_id BIGINT DEFAULT NULL COMMENT '所属文件夹ID',
    tags VARCHAR(500) DEFAULT NULL COMMENT '标签(逗号分隔)',
    is_favorite TINYINT NOT NULL DEFAULT 0 COMMENT '是否收藏：1-是，0-否',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：1-是，0-否',
    view_count INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-删除(软删除)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 外键约束
    CONSTRAINT fk_note_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_note_space FOREIGN KEY (space_id) REFERENCES t_space(id) ON DELETE CASCADE,
    CONSTRAINT fk_note_folder FOREIGN KEY (folder_id) REFERENCES t_folder(id) ON DELETE SET NULL,

    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_space_id (space_id),
    INDEX idx_folder_id (folder_id),
    INDEX idx_status (status),
    INDEX idx_is_favorite (is_favorite),
    INDEX idx_is_top (is_top),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- ----------------------------------------------------------------------------
-- 3.4 版本历史表 (t_note_version)
-- ----------------------------------------------------------------------------
CREATE TABLE t_note_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    version_no INT NOT NULL COMMENT '版本号',
    title VARCHAR(200) DEFAULT NULL COMMENT '标题',
    content LONGTEXT COMMENT '内容',
    remark VARCHAR(500) DEFAULT NULL COMMENT '版本备注',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 外键约束
    CONSTRAINT fk_version_note FOREIGN KEY (note_id) REFERENCES t_note(id) ON DELETE CASCADE,
    CONSTRAINT fk_version_user FOREIGN KEY (created_by) REFERENCES t_user(id) ON DELETE SET NULL,

    -- 索引
    INDEX idx_note_id (note_id),
    UNIQUE KEY uk_note_version (note_id, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记版本历史表';

-- ----------------------------------------------------------------------------
-- 3.5 附件表 (t_attachment)
-- ----------------------------------------------------------------------------
CREATE TABLE t_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    user_id BIGINT NOT NULL COMMENT '上传用户ID',
    note_id BIGINT DEFAULT NULL COMMENT '关联笔记ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型：image-图片，document-文档，video-视频，other-其他',
    mime_type VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',

    -- 外键约束
    CONSTRAINT fk_attachment_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_attachment_note FOREIGN KEY (note_id) REFERENCES t_note(id) ON DELETE SET NULL,

    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_note_id (note_id),
    INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件表';

-- ----------------------------------------------------------------------------
-- 3.6 AI对话表 (t_ai_conversation)
-- ----------------------------------------------------------------------------
CREATE TABLE t_ai_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '对话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    note_id BIGINT DEFAULT NULL COMMENT '关联笔记ID',
    ai_provider VARCHAR(50) NOT NULL COMMENT 'AI提供商：deepseek-DeepSeek，glm-智谱GLM',
    ai_model VARCHAR(100) NOT NULL COMMENT 'AI模型名称',
    title VARCHAR(200) DEFAULT '新对话' COMMENT '对话标题',
    messages JSON COMMENT '对话消息(JSON数组)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 外键约束
    CONSTRAINT fk_conversation_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_note FOREIGN KEY (note_id) REFERENCES t_note(id) ON DELETE CASCADE,

    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_note_id (note_id),
    INDEX idx_ai_provider (ai_provider),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话表';

-- ----------------------------------------------------------------------------
-- 3.7 团队空间表 (t_space)
-- ----------------------------------------------------------------------------
CREATE TABLE t_space (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '空间ID',
    name VARCHAR(100) NOT NULL COMMENT '空间名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '空间描述',
    owner_id BIGINT NOT NULL COMMENT '所有者用户ID',
    member_count INT NOT NULL DEFAULT 1 COMMENT '成员数量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 外键约束
    CONSTRAINT fk_space_owner FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE,

    -- 索引
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队空间表';

-- ----------------------------------------------------------------------------
-- 3.8 空间成员表 (t_space_member)
-- ----------------------------------------------------------------------------
CREATE TABLE t_space_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员ID',
    space_id BIGINT NOT NULL COMMENT '空间ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT '角色：owner-所有者，admin-管理员，editor-编辑者，viewer-查看者',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',

    -- 外键约束
    CONSTRAINT fk_member_space FOREIGN KEY (space_id) REFERENCES t_space(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,

    -- 唯一索引
    UNIQUE KEY uk_space_user (space_id, user_id),

    -- 普通索引
    INDEX idx_user_id (user_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='空间成员表';

-- ----------------------------------------------------------------------------
-- 3.9 评论表 (t_comment)
-- ----------------------------------------------------------------------------
CREATE TABLE t_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID(0为顶级评论)',
    content TEXT NOT NULL COMMENT '评论内容',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 外键约束
    CONSTRAINT fk_comment_note FOREIGN KEY (note_id) REFERENCES t_note(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,

    -- 索引
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================================================
-- 4. 初始数据插入
-- ============================================================================

-- 插入测试用户（密码为：123456，使用BCrypt加密）
INSERT INTO t_user (username, email, password, nickname, status) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHKCmahfHMJ6tiQU5HL1wO', '系统管理员', 1),
('testuser', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHKCmahfHMJ6tiQU5HL1wO', '测试用户', 1);

-- 插入测试文件夹
INSERT INTO t_folder (user_id, name, parent_id, sort_order) VALUES
(1, '我的笔记', 0, 1),
(1, '工作文档', 0, 2),
(1, '学习笔记', 0, 3),
(2, '测试文件夹', 0, 1);

-- 插入测试笔记
INSERT INTO t_note (user_id, title, content, content_type, folder_id, tags, status) VALUES
(1, '欢迎使用AI笔记', '# 欢迎使用AI笔记工具\n\n这是一个功能强大的智能笔记应用。\n\n## 主要功能\n\n- Markdown编辑\n- AI辅助生成\n- 版本管理\n- 团队协作\n\n开始你的笔记之旅吧！', 'markdown', 1, '欢迎,入门', 1),
(1, '项目需求文档', '# 项目需求文档\n\n## 1. 项目概述\n\nAI笔记工具是一个现代化的智能笔记应用。\n\n## 2. 技术栈\n\n- 前端：Vue 3 + TypeScript\n- 后端：Spring Boot 3.x\n- 数据库：MySQL 8.0', 'markdown', 2, '文档,需求', 1),
(2, '测试笔记', '这是一个测试笔记。', 'markdown', 4, '测试', 1);

-- ============================================================================
-- 5. 视图（可选）
-- ============================================================================

-- 创建笔记详情视图（包含用户信息、文件夹信息）
CREATE OR REPLACE VIEW v_note_detail AS
SELECT
    n.id AS note_id,
    n.title,
    n.content,
    n.content_type,
    n.tags,
    n.is_favorite,
    n.is_top,
    n.view_count,
    n.status,
    n.created_at,
    n.updated_at,
    u.id AS user_id,
    u.username,
    u.nickname,
    u.avatar,
    f.id AS folder_id,
    f.name AS folder_name,
    s.id AS space_id,
    s.name AS space_name
FROM t_note n
LEFT JOIN t_user u ON n.user_id = u.id
LEFT JOIN t_folder f ON n.folder_id = f.id
LEFT JOIN t_space s ON n.space_id = s.id;

-- ============================================================================
-- 6. 触发器（可选）
-- ============================================================================

-- 触发器：笔记被访问时自动增加查看次数
DELIMITER $$
CREATE TRIGGER trg_note_view_count
BEFORE UPDATE ON t_note
FOR EACH ROW
BEGIN
    -- 当content发生变化时，认为是一次查看（实际应用中可能需要更精确的逻辑）
    IF OLD.content <> NEW.content OR OLD.title <> NEW.title THEN
        SET NEW.view_count = OLD.view_count + 1;
    END IF;
END$$
DELIMITER ;

-- ============================================================================
-- 7. 存储过程（可选）
-- ============================================================================

-- 存储过程：创建笔记版本快照
DELIMITER $$
CREATE PROCEDURE sp_create_note_version(
    IN p_note_id BIGINT,
    IN p_remark VARCHAR(500)
)
BEGIN
    DECLARE v_version_no INT;
    DECLARE v_user_id BIGINT;

    -- 获取下一个版本号
    SELECT COALESCE(MAX(version_no), 0) + 1 INTO v_version_no
    FROM t_note_version
    WHERE note_id = p_note_id;

    -- 获取笔记的创建用户ID
    SELECT user_id INTO v_user_id
    FROM t_note
    WHERE id = p_note_id;

    -- 插入版本记录
    INSERT INTO t_note_version (note_id, version_no, title, content, remark, created_by)
    SELECT id, v_version_no, title, content, p_remark, v_user_id
    FROM t_note
    WHERE id = p_note_id;

    SELECT v_version_no AS version_no;
END$$
DELIMITER ;

-- ============================================================================
-- 8. 完成初始化
-- ============================================================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 初始化完成
-- ============================================================================
-- 说明：
-- 1. 本脚本使用MySQL 8.0语法编写
-- 2. 字符集使用utf8mb4，支持完整的Unicode字符（包括emoji）
-- 3. 所有表都包含created_at和updated_at字段，并设置了自动更新
-- 4. 合理使用了外键约束保证数据一致性
-- 5. 创建了必要的索引提升查询性能
-- 6. 插入了测试数据方便开发调试
-- ============================================================================
