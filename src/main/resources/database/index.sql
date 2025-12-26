-- 为经常查询的字段创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_locations_name ON locations(name);
CREATE INDEX idx_locations_coordinates ON locations(x_coordinate, y_coordinate);
CREATE INDEX idx_paths_locations ON paths(start_location_id, end_location_id);
CREATE INDEX idx_nav_history_user ON navigation_history(user_id);
CREATE INDEX idx_nav_history_time ON navigation_history(created_at);