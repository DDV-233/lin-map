package com.campus.nav.dao.impl;

import com.campus.nav.dao.LocationDao;
import com.campus.nav.model.Location;
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
 * 地点DAO实现类
 */
public class LocationDaoImpl extends AbstractBaseDao<Location, Integer> implements LocationDao {
    private static final Logger logger = LogManager.getLogger(LocationDaoImpl.class);
    
    // SQL语句
    private static final String TABLE_NAME = "locations";
    private static final String COLUMNS = "id, name, description, type, x_coordinate, y_coordinate, " +
            "has_shade, scenic_level, is_accessible, created_at";
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + 
            "(name, description, type, x_coordinate, y_coordinate, has_shade, scenic_level, is_accessible) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + 
            " SET name = ?, description = ?, type = ?, x_coordinate = ?, y_coordinate = ?, " +
            "has_shade = ?, scenic_level = ?, is_accessible = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " ORDER BY name";
    private static final String SELECT_BY_NAME = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE name = ?";
    private static final String SELECT_BY_TYPE = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE type = ?";
    private static final String SELECT_ACCESSIBLE = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + 
            " WHERE is_accessible = TRUE ORDER BY name";
    private static final String SELECT_BY_COORDINATE_RANGE = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + 
            " WHERE x_coordinate BETWEEN ? AND ? AND y_coordinate BETWEEN ? AND ?";
    private static final String SEARCH = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + 
            " WHERE name LIKE ? OR description LIKE ? ORDER BY name";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM " + TABLE_NAME;
    
    @Override
    public boolean save(Location location) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(INSERT_SQL,
                    location.getName(),
                    location.getDescription(),
                    location.getType() != null ? location.getType().name() : Location.LocationType.OTHER.name(),
                    location.getXCoordinate(),
                    location.getYCoordinate(),
                    location.getHasShade() != null ? location.getHasShade() : false,
                    location.getScenicLevel() != null ? location.getScenicLevel() : 1,
                    location.getIsAccessible() != null ? location.getIsAccessible() : true);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("保存地点失败: {}", location.getName(), e);
            return false;
        }
    }
    
    @Override
    public boolean update(Location location) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(UPDATE_SQL,
                    location.getName(),
                    location.getDescription(),
                    location.getType() != null ? location.getType().name() : Location.LocationType.OTHER.name(),
                    location.getXCoordinate(),
                    location.getYCoordinate(),
                    location.getHasShade() != null ? location.getHasShade() : false,
                    location.getScenicLevel() != null ? location.getScenicLevel() : 1,
                    location.getIsAccessible() != null ? location.getIsAccessible() : true,
                    location.getId());
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("更新地点失败: {}", location.getId(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(DELETE_SQL, id);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("删除地点失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public Optional<Location> findById(Integer id) {
        return queryForObject(SELECT_BY_ID, id);
    }
    
    @Override
    public List<Location> findAll() {
        return queryForList(SELECT_ALL);
    }
    
    @Override
    public PageResult<Location> findByPage(PageQuery query) {
        if (query == null) {
            query = PageQuery.defaultQuery();
        }
        
        // 构建查询SQL
        StringBuilder sqlBuilder = new StringBuilder("SELECT " + COLUMNS + " FROM " + TABLE_NAME);
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM " + TABLE_NAME);
        
        // 添加搜索条件
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = "%" + query.getKeyword() + "%";
            String whereClause = " WHERE name LIKE ? OR description LIKE ?";
            sqlBuilder.append(whereClause);
            countSqlBuilder.append(whereClause);
        }
        
        // 添加排序
        if (query.getSortBy() != null) {
            sqlBuilder.append(" ORDER BY ").append(query.getSortBy());
            if (query.getSortOrder() != null) {
                sqlBuilder.append(" ").append(query.getSortOrder());
            }
        } else {
            sqlBuilder.append(" ORDER BY name ASC");
        }
        
        // 添加分页
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        
        String sql = sqlBuilder.toString();
        String countSql = countSqlBuilder.toString();
        
        try {
            // 执行查询
            List<Location> data;
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
            logger.error("分页查询地点失败", e);
            return PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public long count() {
        try {
            return DatabaseUtil.executeQueryForSingle(COUNT_SQL, Long.class);
        } catch (Exception e) {
            logger.error("统计地点总数失败", e);
            return 0;
        }
    }
    
    @Override
    public Optional<Location> findByName(String name) {
        return queryForObject(SELECT_BY_NAME, name);
    }
    
    @Override
    public List<Location> findByType(Location.LocationType type) {
        return DatabaseUtil.executeQuery(SELECT_BY_TYPE, getRowMapper(), type.name());
    }
    
    @Override
    public List<Location> findAccessibleLocations() {
        return DatabaseUtil.executeQuery(SELECT_ACCESSIBLE, getRowMapper());
    }
    
    @Override
    public List<Location> findByCoordinateRange(double minX, double maxX, double minY, double maxY) {
        return DatabaseUtil.executeQuery(SELECT_BY_COORDINATE_RANGE, getRowMapper(), minX, maxX, minY, maxY);
    }
    
    @Override
    public List<Location> search(String keyword) {
        String searchKeyword = "%" + keyword + "%";
        return DatabaseUtil.executeQuery(SEARCH, getRowMapper(), searchKeyword, searchKeyword);
    }
    
    @Override
    protected DatabaseUtil.RowMapper<Location> getRowMapper() {
        return new DatabaseUtil.RowMapper<Location>() {
            @Override
            public Location mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Location.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .type(Location.LocationType.fromString(rs.getString("type")))
                        .xCoordinate(rs.getDouble("x_coordinate"))
                        .yCoordinate(rs.getDouble("y_coordinate"))
                        .hasShade(rs.getBoolean("has_shade"))
                        .scenicLevel(rs.getInt("scenic_level"))
                        .isAccessible(rs.getBoolean("is_accessible"))
                        .createdAt(rs.getTimestamp("created_at") != null ? 
                                rs.getTimestamp("created_at").toLocalDateTime() : null)
                        .build();
            }
        };
    }
}