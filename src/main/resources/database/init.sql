-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_navigation_system;
USE campus_navigation_system;

-- 用户表（区分管理员和普通用户）
CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       user_type ENUM('ADMIN', 'USER') DEFAULT 'USER',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       is_active BOOLEAN DEFAULT TRUE
);

-- 地点表
CREATE TABLE locations (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           name VARCHAR(100) NOT NULL,
                           description TEXT,
                           type ENUM('BUILDING', 'GARDEN', 'CAFETERIA', 'LIBRARY', 'SPORTS', 'GATE', 'OTHER') NOT NULL,
                           x_coordinate DOUBLE NOT NULL COMMENT 'X坐标（用于地图显示）',
                           y_coordinate DOUBLE NOT NULL COMMENT 'Y坐标（用于地图显示）',
                           has_shade BOOLEAN DEFAULT FALSE COMMENT '是否有绿荫',
                           scenic_level INT DEFAULT 1 COMMENT '景色等级 1-5',
                           is_accessible BOOLEAN DEFAULT TRUE COMMENT '是否可通行',
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 路径表
CREATE TABLE paths (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       start_location_id INT NOT NULL,
                       end_location_id INT NOT NULL,
                       distance DOUBLE NOT NULL COMMENT '路径距离（米）',
                       time_cost INT COMMENT '预估时间（分钟）',
                       has_shade BOOLEAN DEFAULT FALSE COMMENT '路径是否有绿荫',
                       scenic_level INT DEFAULT 1 COMMENT '路径景色等级 1-5',
                       is_indoor BOOLEAN DEFAULT FALSE COMMENT '是否为室内路径',
                       is_active BOOLEAN DEFAULT TRUE COMMENT '是否可用',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (start_location_id) REFERENCES locations(id),
                       FOREIGN KEY (end_location_id) REFERENCES locations(id),
                       UNIQUE KEY unique_path (start_location_id, end_location_id)
);

-- 导航历史表
CREATE TABLE navigation_history (
                                    id INT PRIMARY KEY AUTO_INCREMENT,
                                    user_id INT NOT NULL,
                                    start_location_id INT NOT NULL,
                                    end_location_id INT NOT NULL,
                                    path_strategy ENUM('SHORTEST', 'SHADIEST', 'MOST_SCENIC') NOT NULL,
                                    total_distance DOUBLE NOT NULL,
                                    total_time INT NOT NULL,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (user_id) REFERENCES users(id),
                                    FOREIGN KEY (start_location_id) REFERENCES locations(id),
                                    FOREIGN KEY (end_location_id) REFERENCES locations(id)
);

-- 系统配置表（管理员可配置）
CREATE TABLE system_config (
                               id INT PRIMARY KEY AUTO_INCREMENT,
                               config_key VARCHAR(50) UNIQUE NOT NULL,
                               config_value VARCHAR(255) NOT NULL,
                               description TEXT,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
