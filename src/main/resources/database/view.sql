-- 创建路径详情视图
CREATE VIEW path_details AS
SELECT
    p.id,
    p.distance,
    p.time_cost,
    p.has_shade,
    p.scenic_level,
    p.is_active,
    start_loc.name as start_name,
    start_loc.x_coordinate as start_x,
    start_loc.y_coordinate as start_y,
    end_loc.name as end_name,
    end_loc.x_coordinate as end_x,
    end_loc.y_coordinate as end_y
FROM paths p
         JOIN locations start_loc ON p.start_location_id = start_loc.id
         JOIN locations end_loc ON p.end_location_id = end_loc.id;

-- 创建导航历史详情视图
CREATE VIEW navigation_history_details AS
SELECT
    nh.id,
    nh.path_strategy,
    nh.total_distance,
    nh.total_time,
    nh.created_at,
    u.username,
    start_loc.name as start_location,
    end_loc.name as end_location
FROM navigation_history nh
         JOIN users u ON nh.user_id = u.id
         JOIN locations start_loc ON nh.start_location_id = start_loc.id
         JOIN locations end_loc ON nh.end_location_id = end_loc.id;