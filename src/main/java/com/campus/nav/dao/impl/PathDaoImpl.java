package com.campus.nav.dao.impl;

import com.campus.nav.dao.PathDao;
import com.campus.nav.model.Path;
import com.campus.nav.model.PageQuery;
import com.campus.nav.model.PageResult;
import com.campus.nav.utils.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 路径DAO实现类
 */
public class PathDaoImpl extends AbstractBaseDao<Path, Integer> implements PathDao {
    private static final Logger logger = LogManager.getLogger(PathDaoImpl.class);
    
    // SQL语句
    private static final String TABLE_NAME = "paths";
    private static final String COLUMNS = "p.id, p.start_location_id, p.end_location_id, p.distance, " +
            "p.time_cost, p.has_shade, p.scenic_level, p.is_indoor, p.is_active, p.created_at, " +
            "l1.name as start_name, l1.x_coordinate as start_x, l1.y_coordinate as start_y, " +
            "l2.name as end_name, l2.x_coordinate as end_x, l2.y_coordinate as end_y";
    private static final String FROM_CLAUSE = "FROM " + TABLE_NAME + " p " +
            "LEFT JOIN locations l1 ON p.start_location_id = l1.id " +
            "LEFT JOIN locations l2 ON p.end_location_id = l2.id";
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + 
            "(start_location_id, end_location_id, distance, time_cost, has_shade, scenic_level, is_indoor, is_active) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + 
            " SET distance = ?, time_cost = ?, has_shade = ?, scenic_level = ?, " +
            "is_indoor = ?, is_active = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNS + " " + FROM_CLAUSE + " WHERE p.id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNS + " " + FROM_CLAUSE + " ORDER BY p.id";
    private static final String SELECT_BY_START_END = "SELECT " + COLUMNS + " " + FROM_CLAUSE + 
            " WHERE p.start_location_id = ? AND p.end_location_id = ?";
    private static final String SELECT_BY_START = "SELECT " + COLUMNS + " " + FROM_CLAUSE + 
            " WHERE p.start_location_id = ? AND p.is_active = TRUE";
    private static final String SELECT_BY_END = "SELECT " + COLUMNS + " " + FROM_CLAUSE + 
            " WHERE p.end_location_id = ? AND p.is_active = TRUE";
    private static final String SELECT_BETWEEN = "SELECT " + COLUMNS + " " + FROM_CLAUSE + 
            " WHERE (p.start_location_id = ? AND p.end_location_id = ?) " +
            "OR (p.start_location_id = ? AND p.end_location_id = ?) AND p.is_active = TRUE";
    private static final String SELECT_ACTIVE = "SELECT " + COLUMNS + " " + FROM_CLAUSE + 
            " WHERE p.is_active = TRUE ORDER BY p.start_location_id, p.end_location_id";
    private static final String UPDATE_STATUS = "UPDATE " + TABLE_NAME + 
            " SET is_active = ? WHERE id = ?";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM " + TABLE_NAME;
    
    @Override
    public boolean save(Path path) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(INSERT_SQL,
                    path.getStartLocationId(),
                    path.getEndLocationId(),
                    path.getDistance(),
                    path.getTimeCost(),
                    path.getHasShade() != null ? path.getHasShade() : false,
                    path.getScenicLevel() != null ? path.getScenicLevel() : 1,
                    path.getIsIndoor() != null ? path.getIsIndoor() : false,
                    path.getIsActive() != null ? path.getIsActive() : true);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("保存路径失败: {} -> {}", 
                    path.getStartLocationId(), path.getEndLocationId(), e);
            return false;
        }
    }
    
    @Override
    public boolean update(Path path) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(UPDATE_SQL,
                    path.getDistance(),
                    path.getTimeCost(),
                    path.getHasShade(),
                    path.getScenicLevel(),
                    path.getIsIndoor(),
                    path.getIsActive(),
                    path.getId());
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("更新路径失败: {}", path.getId(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(DELETE_SQL, id);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("删除路径失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public Optional<Path> findById(Integer id) {
        return queryForObject(SELECT_BY_ID, id);
    }
    
    @Override
    public List<Path> findAll() {
        return queryForList(SELECT_ALL);
    }
    
    @Override
    public PageResult<Path> findByPage(PageQuery query) {
        if (query == null) {
            query = PageQuery.defaultQuery();
        }
        
        // 构建查询SQL
        StringBuilder sqlBuilder = new StringBuilder("SELECT " + COLUMNS + " " + FROM_CLAUSE);
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM " + TABLE_NAME);
        
        // 添加搜索条件
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = "%" + query.getKeyword() + "%";
            String whereClause = " WHERE l1.name LIKE ? OR l2.name LIKE ?";
            sqlBuilder.append(whereClause);
            countSqlBuilder.append(" p LEFT JOIN locations l1 ON p.start_location_id = l1.id " +
                    "LEFT JOIN locations l2 ON p.end_location_id = l2.id").append(whereClause);
        }
        
        // 添加排序
        if (query.getSortBy() != null) {
            sqlBuilder.append(" ORDER BY p.").append(query.getSortBy());
            if (query.getSortOrder() != null) {
                sqlBuilder.append(" ").append(query.getSortOrder());
            }
        } else {
            sqlBuilder.append(" ORDER BY p.id DESC");
        }
        
        // 添加分页
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        
        String sql = sqlBuilder.toString();
        String countSql = countSqlBuilder.toString();
        
        try {
            // 执行查询
            List<Path> data;
            Long total;
            
            if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
                String keyword = "%" + query.getKeyword() + "%";
                data = DatabaseUtil.executeQuery(sql, getRowMapper(),
                        keyword, keyword, query.getPageSize(), query.getOffset());
                total = DatabaseUtil.executeQueryForSingle(countSql, Long.class, keyword, keyword);
            } else {
                data = DatabaseUtil.executeQuery(sql, getRowMapper(),
                        query.getPageSize(), query.getOffset());
                total = DatabaseUtil.executeQueryForSingle(countSql, Long.class);
            }
            
            return PageResult.of(data, total, query);
            
        } catch (Exception e) {
            logger.error("分页查询路径失败", e);
            return PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public long count() {
        try {
            return DatabaseUtil.executeQueryForSingle(COUNT_SQL, Long.class);
        } catch (Exception e) {
            logger.error("统计路径总数失败", e);
            return 0;
        }
    }
    
    @Override
    public Optional<Path> findByStartAndEnd(Integer startLocationId, Integer endLocationId) {
        return queryForObject(SELECT_BY_START_END, startLocationId, endLocationId);
    }
    
    @Override
    public List<Path> findByStartLocation(Integer startLocationId) {
        return DatabaseUtil.executeQuery(SELECT_BY_START, getRowMapper(), startLocationId);
    }
    
    @Override
    public List<Path> findByEndLocation(Integer endLocationId) {
        return DatabaseUtil.executeQuery(SELECT_BY_END, getRowMapper(), endLocationId);
    }
    
    @Override
    public List<Path> findPathsBetween(Integer locationId1, Integer locationId2) {
        return DatabaseUtil.executeQuery(SELECT_BETWEEN, getRowMapper(), 
                locationId1, locationId2, locationId2, locationId1);
    }
    
    @Override
    public List<Path> findActivePaths() {
        return DatabaseUtil.executeQuery(SELECT_ACTIVE, getRowMapper());
    }
    
    @Override
    public boolean updateStatus(Integer pathId, boolean isActive) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(UPDATE_STATUS, isActive, pathId);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("更新路径状态失败: {}", pathId, e);
            return false;
        }
    }
    
    @Override
    protected DatabaseUtil.RowMapper<Path> getRowMapper() {
        return new DatabaseUtil.RowMapper<Path>() {
            @Override
            public Path mapRow(ResultSet rs, int rowNum) throws SQLException {
                // 构建起点地点对象
                com.campus.nav.model.Location startLocation = com.campus.nav.model.Location.builder()
                        .id(rs.getInt("start_location_id"))
                        .name(rs.getString("start_name"))
                        .xCoordinate(rs.getDouble("start_x"))
                        .yCoordinate(rs.getDouble("start_y"))
                        .build();
                
                // 构建终点地点对象
                com.campus.nav.model.Location endLocation = com.campus.nav.model.Location.builder()
                        .id(rs.getInt("end_location_id"))
                        .name(rs.getString("end_name"))
                        .xCoordinate(rs.getDouble("end_x"))
                        .yCoordinate(rs.getDouble("end_y"))
                        .build();
                
                return Path.builder()
                        .id(rs.getInt("id"))
                        .startLocationId(rs.getInt("start_location_id"))
                        .endLocationId(rs.getInt("end_location_id"))
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .distance(rs.getDouble("distance"))
                        .timeCost(rs.getInt("time_cost"))
                        .hasShade(rs.getBoolean("has_shade"))
                        .scenicLevel(rs.getInt("scenic_level"))
                        .isIndoor(rs.getBoolean("is_indoor"))
                        .isActive(rs.getBoolean("is_active"))
                        .createdAt(rs.getTimestamp("created_at") != null ? 
                                rs.getTimestamp("created_at").toLocalDateTime() : null)
                        .build();
            }
        };
    }
}