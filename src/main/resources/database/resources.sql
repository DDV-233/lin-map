-- 插入默认管理员账号（密码：admin123）
INSERT INTO users (username, password, email, user_type)
VALUES ('admin', 'admin12345', 'admin@campus.edu', 'ADMIN');

-- 插入一些示例地点
INSERT INTO locations (name, description, type, x_coordinate, y_coordinate, has_shade, scenic_level) VALUES
                                                                                                         ('南大门', '学校正门，气势恢宏', 'GATE', 100, 500, FALSE, 3),
                                                                                                         ('图书馆', '主图书馆，藏书丰富', 'LIBRARY', 300, 450, FALSE, 4),
                                                                                                         ('教学楼A', '主要教学楼', 'BUILDING', 400, 300, FALSE, 2),
                                                                                                         ('学生食堂', '第一学生食堂', 'CAFETERIA', 350, 200, FALSE, 3),
                                                                                                         ('中心花园', '绿树成荫的休息区', 'GARDEN', 250, 350, TRUE, 5),
                                                                                                         ('体育馆', '室内体育场馆', 'SPORTS', 450, 100, FALSE, 3),
                                                                                                         ('北门', '学校后门', 'GATE', 200, 50, FALSE, 2),
                                                                                                         ('实验楼', '科研实验楼', 'BUILDING', 500, 400, FALSE, 2);

-- 插入路径数据（双向路径）
INSERT INTO paths (start_location_id, end_location_id, distance, time_cost, has_shade, scenic_level) VALUES
                                                                                                         (1, 2, 150, 3, TRUE, 4),   -- 南大门到图书馆
                                                                                                         (2, 3, 120, 2, FALSE, 3),  -- 图书馆到教学楼A
                                                                                                         (3, 4, 100, 2, TRUE, 4),   -- 教学楼A到食堂
                                                                                                         (4, 5, 80, 1, TRUE, 5),    -- 食堂到中心花园
                                                                                                         (5, 6, 200, 4, TRUE, 5),   -- 中心花园到体育馆
                                                                                                         (2, 5, 90, 2, TRUE, 5),    -- 图书馆到中心花园
                                                                                                         (3, 6, 180, 3, FALSE, 3),  -- 教学楼A到体育馆
                                                                                                         (1, 7, 400, 8, FALSE, 2);  -- 南大门到北门

-- 插入对称的返回路径
INSERT INTO paths (start_location_id, end_location_id, distance, time_cost, has_shade, scenic_level)
SELECT end_location_id, start_location_id, distance, time_cost, has_shade, scenic_level
FROM paths;

-- 插入系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
                                                                      ('MAP_WIDTH', '800', '地图宽度'),
                                                                      ('MAP_HEIGHT', '600', '地图高度'),
                                                                      ('SHADE_WEIGHT', '1.5', '绿荫路径权重'),
                                                                      ('SCENIC_WEIGHT', '1.3', '景色路径权重'),
                                                                      ('DEFAULT_STRATEGY', 'SHORTEST', '默认导航策略');