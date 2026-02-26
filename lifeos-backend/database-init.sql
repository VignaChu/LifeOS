-- LifeOS Database Initialization Script
-- 使用方法: mysql -u root -p < database-init.sql
-- 或者: mysql -u root -p -e "source database-init.sql"

-- 创建数据库 (如果不存在)
CREATE DATABASE IF NOT EXISTS lifeos 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE lifeos;

-- ============================================
-- 用户表 (users)
-- 存储前台用户信息
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密存储)',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 生活记录表 (life_records)
-- 存储用户输入的各种生活记录：消费、情绪、事件、日记
-- ============================================
CREATE TABLE IF NOT EXISTS life_records (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID,关联users表',
    content TEXT NOT NULL COMMENT '记录内容文本',
    record_type VARCHAR(50) DEFAULT NULL COMMENT '记录类型: expense(消费), mood(情绪), event(事件), diary(日记)',
    amount DECIMAL(10,2) DEFAULT NULL COMMENT '金额(仅消费类型)',
    tags TEXT COMMENT '标签列表, JSON格式存储',
    emotion_score INT DEFAULT NULL COMMENT '情绪分数(-10到10)',
    record_time DATETIME DEFAULT NULL COMMENT '记录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_record_type (record_type),
    INDEX idx_record_time (record_time),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生活记录表';

-- ============================================
-- LLM配置表 (llm_config)
-- 存储大模型API配置信息
-- ============================================
CREATE TABLE IF NOT EXISTS llm_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    provider VARCHAR(50) NOT NULL COMMENT 'LLM提供商: openai, azure, claude, gemini, deepseek等',
    api_key VARCHAR(500) NOT NULL COMMENT 'API密钥',
    api_url VARCHAR(500) DEFAULT NULL COMMENT 'API地址, 例如: https://api.openai.com/v1',
    model VARCHAR(100) NOT NULL COMMENT '模型名称, 例如: gpt-3.5-turbo',
    temperature DOUBLE DEFAULT 0.7 COMMENT '温度参数(0-2), 控制输出随机性',
    use_local_rules TINYINT(1) DEFAULT 0 COMMENT '是否使用本地规则: 0-否, 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM配置表';

-- ============================================
-- 可选: 插入示例数据 (如需默认数据可取消注释)
-- ============================================

-- 示例: 插入一条示例生活记录
-- INSERT INTO life_records (content, record_type, amount, tags, emotion_score, record_time) 
-- VALUES ('今天花了30元买午餐，味道不错', 'expense', 30.00, '["餐饮", "午餐"]', 5, NOW());

-- 示例: 插入默认LLM配置 (不建议, 建议用户自行配置)
-- INSERT INTO llm_config (provider, api_key, api_url, model, temperature, use_local_rules)
-- VALUES ('openai', 'your-api-key-here', 'https://api.openai.com/v1', 'gpt-3.5-turbo', 0.7, 0);

-- ============================================
-- 音乐播放列表配置表 (music_playlists)
-- 存储网易云音乐歌单配置，根据情绪值播放不同歌单
-- ============================================
CREATE TABLE IF NOT EXISTS music_playlists (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '歌单名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '歌单描述',
    netease_playlist_id VARCHAR(50) NOT NULL COMMENT '网易云歌单ID',
    emotion_min INT DEFAULT -10 COMMENT '适用情绪最小值(-10到10)',
    emotion_max INT DEFAULT 10 COMMENT '适用情绪最大值(-10到10)',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认歌单: 0-否, 1-是',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_emotion_range (emotion_min, emotion_max),
    INDEX idx_is_default (is_default),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='音乐播放列表配置表';

-- ============================================
-- 插入默认音乐播放列表配置
-- 根据情绪值范围配置不同的歌单
-- ============================================
INSERT INTO music_playlists (name, description, netease_playlist_id, emotion_min, emotion_max, is_default, sort_order) VALUES
('轻松愉快', '适合心情好的时候', '60198', 5, 10, 0, 1),           -- 轻音乐 - 心情好
('平静舒缓', '适合平静的心情', '26467411', 0, 4, 1, 2),         -- 纯音乐 - 平静
('治愈系', '适合心情低落时', '5270142388', -4, -1, 0, 3),       -- 治愈系 - 轻微低落
('励志加油', '适合情绪很差时', '3136952023', -10, -5, 0, 4);     -- 励志 - 情绪低落

-- ============================================
-- 验证表创建成功
-- ============================================
SELECT 'LifeOS database initialized successfully!' AS status;
SELECT CONCAT('Tables created: ', COUNT(*)) AS table_count FROM information_schema.tables WHERE table_schema = 'lifeos';
