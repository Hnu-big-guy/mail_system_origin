-- mail_system.sql
CREATE DATABASE IF NOT EXISTS mail_system DEFAULT CHARACTER SET utf8mb4;

USE mail_system;

-- 删除现有表（按依赖顺序）
DROP TABLE IF EXISTS attachments;
DROP TABLE IF EXISTS mails;
DROP TABLE IF EXISTS filter_rules;
DROP TABLE IF EXISTS system_logs;
DROP TABLE IF EXISTS users;

-- 用户表
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(120) NOT NULL,
                       nickname VARCHAR(50),
                       phone VARCHAR(20),
                       role ENUM('USER', 'ADMIN') DEFAULT 'USER',
                       status ENUM('ACTIVE', 'DISABLED', 'LOCKED') DEFAULT 'ACTIVE',
                       mailbox_size INT DEFAULT 100,
                       used_size INT DEFAULT 0,
                       last_login_time DATETIME,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_email (email),
                       INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 邮件表
CREATE TABLE mails (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       subject VARCHAR(200) NOT NULL,
                       content TEXT,
                       sender_id BIGINT NOT NULL,
                       receiver_id BIGINT NOT NULL,
                       sender_email VARCHAR(100),
                       receiver_email VARCHAR(100),
                       is_read BOOLEAN DEFAULT FALSE,
                       is_starred BOOLEAN DEFAULT FALSE,
                       is_deleted BOOLEAN DEFAULT FALSE,
                       is_draft BOOLEAN DEFAULT FALSE,
                       sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       received_at DATETIME,
                       size INT,
                       folder ENUM('INBOX', 'SENT', 'DRAFT', 'TRASH', 'SPAM') DEFAULT 'INBOX',
                       FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
                       FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
                       INDEX idx_sender (sender_id),
                       INDEX idx_receiver (receiver_id),
                       INDEX idx_folder (folder),
                       INDEX idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 附件表
CREATE TABLE attachments (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             filename VARCHAR(255) NOT NULL,
                             file_type VARCHAR(100),
                             file_size BIGINT,
                             file_path VARCHAR(500),
                             mail_id BIGINT NOT NULL,
                             uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (mail_id) REFERENCES mails(id) ON DELETE CASCADE,
                             INDEX idx_mail (mail_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统日志表
CREATE TABLE system_logs (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            type ENUM('LOGIN', 'LOGOUT', 'SEND_MAIL', 'RECEIVE_MAIL', 'SAVE_DRAFT',
                                'CREATE_USER', 'DELETE_USER', 'UPDATE_USER',
                                'SYSTEM_CONFIG', 'SECURITY', 'ERROR'),
                            module VARCHAR(100),
                            operation VARCHAR(500),
                            details VARCHAR(1000),
                            ip_address VARCHAR(50),
                            username VARCHAR(100),
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_type (type),
                            INDEX idx_username (username),
                            INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 过滤规则表
CREATE TABLE filter_rules (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              type ENUM('EMAIL', 'SUBJECT', 'CONTENT', 'IP_ADDRESS'),
                              pattern VARCHAR(200),
                              action ENUM('BLOCK', 'ALLOW', 'MOVE_TO_SPAM', 'MARK_AS_READ'),
                              is_active BOOLEAN DEFAULT TRUE,
                              priority INT DEFAULT 1,
                              description VARCHAR(500),
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              INDEX idx_type (type),
                              INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认管理员账户 (密码: admin123)
INSERT INTO users (username, email, password, nickname, role)
VALUES ('admin', 'admin@test.com',
        '$2a$10$5x7piehSC7ZcenbuV2pZJ.ifT1BxuqqPTHkAOD9ULCC/3rg9p2IX2',
        '系统管理员', 'ADMIN');

-- 为测试添加普通用户账户 (密码: test123)
-- 注意：此哈希值由PasswordHashTest生成，对应密码test123
INSERT INTO users (username, email, password, nickname, role)
VALUES ('user1', 'user1@test.com',
        '$2a$10$bGl08RY4hZtghB2YITIE2uvkMlCQjVy3WB1h9G7u7h/p9F3uXG/Jq',
        '测试用户', 'USER');

-- 插入默认过滤规则
INSERT INTO filter_rules (type, pattern, action, description) VALUES
                                                                  ('SUBJECT', '.*[免费|优惠|促销].*', 'MOVE_TO_SPAM', '拦截垃圾邮件'),
                                                                  ('CONTENT', '.*[点击领取|限时特价].*', 'MOVE_TO_SPAM', '拦截广告内容');

