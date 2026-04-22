-- AI笔记工具 - 用户AI配置表
-- 用于存储每个用户的 AI Provider 和 API Key 配置

CREATE TABLE IF NOT EXISTS `t_user_ai_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `provider` VARCHAR(32) NOT NULL DEFAULT 'deepseek' COMMENT 'AI提供商：deepseek / glm / minimax',
    `model` VARCHAR(64) NOT NULL DEFAULT 'deepseek-chat' COMMENT 'AI模型名称',
    `deepseek_api_key` VARCHAR(512) DEFAULT NULL COMMENT 'DeepSeek API密钥',
    `glm_api_key` VARCHAR(512) DEFAULT NULL COMMENT '智谱GLM API密钥',
    `minimax_api_key` VARCHAR(512) DEFAULT NULL COMMENT 'MiniMax API密钥',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户AI配置表';
