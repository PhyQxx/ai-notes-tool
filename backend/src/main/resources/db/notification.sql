-- 通知表 DDL
CREATE TABLE IF NOT EXISTS t_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sender_id BIGINT,
    type VARCHAR(50) NOT NULL COMMENT 'COMMENT / SHARE / MENTION / SYSTEM',
    title VARCHAR(200) NOT NULL,
    content VARCHAR(500),
    link VARCHAR(500),
    is_read TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
