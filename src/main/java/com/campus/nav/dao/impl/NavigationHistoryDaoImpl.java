package com.campus.nav.dao.impl;

import com.campus.nav.dao.NavigationHistoryDao;
import com.campus.nav.model.NavigationHistory;
import com.campus.nav.model.NavigationStrategy;
import com.campus.nav.model.PageQuery;
import com.campus.nav.model.PageResult;
import com.campus.nav.model.User;
import com.campus.nav.utils.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 导航历史DAO实现类
 */
public class NavigationHistoryDaoImpl extends AbstractBaseDao<NavigationHistory, Integer> implements NavigationHistoryDao {
    private static final Logger logger = LogManager.getLogger(NavigationHistoryDaoImpl.class);
    
    // SQL语句
    private static final String TABLE_NAME = "navigation_history";
    private static final String COLUMNS = "nh.id, nh.user_id, nh.start_location_id, nh.end_location_id, " +
            "nh.path_strategy, nh.total_distance, nh.total_time, nh.created_at, " +
            "u.username, u.user_type, " +
            "l1.name as start_name, l2.name as end_name";
    private static final String FROM_CLAUSE = "FROM " + TABLE_NAME + " nh " +
            "LEFT JOIN users u ON nh.user_id = u.id " +
            "LEFT JOIN locations l1 ON nh.start_location_id = l1.id " +
            "LEFT JOIN locations l2 ON nh.end_location_id = l2.id";
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + 
            "(user_id, start_location_id, end_location_id, path_strategy, total_distance, total_time) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNS + " " + FROM_CLAUSE + " WHERE nh.id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNS + " " + FROM_CLAUSE + " ORDER BY nh.created_at DESC";
    private static final String SELECT_BY_USER_ID = "SELECT " + COLUMNS + " " + FROM_CLAUSE + 
            " WHERE nh.user_id = ? ORDER BY nh.created_at DESC";
    private static final String DELETE_BY_USER_ID = "DELETE FROM " + TABLE_NAME + " WHERE user_id = ?";
    private static final String COUNT_BY_USER_ID = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE user_id = ?";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM " + TABLE_NAME;
    
    @Override
    public boolean save(NavigationHistory history) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(INSERT_SQL,
                    history.getUserId(),
                    history.getStartLocationId(),
                    history.getEndLocationId(),
                    history.getPathStrategy() != null ? history.getPathStrategy().name() : NavigationStrategy.SHORTEST.name(),
                    history.getTotalDistance(),
                    history.getTotalTime());
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("保存导航历史失败", e);
            return false;
        }
    }
    
    @Override
    public boolean update(NavigationHistory entity) {
        // 导航历史通常不允许更新
        throw new UnsupportedOperationException("导航历史不允许更新");
    }
    
    @Override
    public boolean deleteById(Integer id) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(DELETE_SQL, id);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("删除导航历史失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public Optional<NavigationHistory> findById(Integer id) {
        return queryForObject(SELECT_BY_ID, id);
    }
    
    @Override
    public List<NavigationHistory> findAll() {
        return queryForList(SELECT_ALL);
    }
    
    @Override
    public PageResult<NavigationHistory> findByPage(PageQuery query) {
        if (query == null) {
            query = PageQuery.defaultQuery();
        }
        
        // 构建查询SQL
        StringBuilder sqlBuilder = new StringBuilder("SELECT " + COLUMNS + " " + FROM_CLAUSE);
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM " + TABLE_NAME + " nh");
        
        // 添加搜索条件
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = "%" + query.getKeyword() + "%";
            String whereClause = " WHERE u.username LIKE ? OR l1.name LIKE ? OR l2.name LIKE ?";
            sqlBuilder.append(whereClause);
            countSqlBuilder.append(" LEFT JOIN users u ON nh.user_id = u.id " +
                    "LEFT JOIN locations l1 ON nh.start_location_id = l1.id " +
                    "LEFT JOIN locations l2 ON nh.end_location_id = l2.id").append(whereClause);
        }
        
        // 添加排序
        sqlBuilder.append(" ORDER BY nh.created_at DESC");
        
        // 添加分页
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        
        String sql = sqlBuilder.toString();
        String countSql = countSqlBuilder.toString();
        
        try {
            // 执行查询
            List<NavigationHistory> data;
            Long total;
            
            if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
                String keyword = "%" + query.getKeyword() + "%";
                data = DatabaseUtil.executeQuery(sql, getRowMapper(),
                        keyword, keyword, keyword, query.getPageSize(), query.getOffset());
                total = DatabaseUtil.executeQueryForSingle(countSql, Long.class, keyword, keyword, keyword);
            } else {
                data = DatabaseUtil.executeQuery(sql, getRowMapper(),
                        query.getPageSize(), query.getOffset());
                total = DatabaseUtil.executeQueryForSingle(countSql, Long.class);
            }
            
            return PageResult.of(data, total, query);
            
        } catch (Exception e) {
            logger.error("分页查询导航历史失败", e);
            return PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public long count() {
        try {
            return DatabaseUtil.executeQueryForSingle(COUNT_SQL, Long.class);
        } catch (Exception e) {
            logger.error("统计导航历史总数失败", e);
            return 0;
        }
    }
    
    @Override
    public List<NavigationHistory> findByUserId(Integer userId) {
        return DatabaseUtil.executeQuery(SELECT_BY_USER_ID, getRowMapper(), userId);
    }
    
    @Override
    public PageResult<NavigationHistory> findByUserIdPage(Integer userId, PageQuery query) {
        if (query == null) {
            query = PageQuery.defaultQuery();
        }
        
        // 构建查询SQL
        String sql = "SELECT " + COLUMNS + " " + FROM_CLAUSE + 
                " WHERE nh.user_id = ? ORDER BY nh.created_at DESC LIMIT ? OFFSET ?";
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE user_id = ?";
        
        try {
            List<NavigationHistory> data = DatabaseUtil.executeQuery(sql, getRowMapper(),
                    userId, query.getPageSize(), query.getOffset());
            Long total = DatabaseUtil.executeQueryForSingle(countSql, Long.class, userId);
            
            return PageResult.of(data, total, query);
            
        } catch (Exception e) {
            logger.error("分页查询用户导航历史失败: {}", userId, e);
            return PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public boolean deleteByUserId(Integer userId) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(DELETE_BY_USER_ID, userId);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("删除用户导航历史失败: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public long countByUserId(Integer userId) {
        try {
            return DatabaseUtil.executeQueryForSingle(COUNT_BY_USER_ID, Long.class, userId);
        } catch (Exception e) {
            logger.error("统计用户导航次数失败: {}", userId, e);
            return 0;
        }
    }
    
    @Override
    protected DatabaseUtil.RowMapper<NavigationHistory> getRowMapper() {
        return new DatabaseUtil.RowMapper<NavigationHistory>() {
            @Override
            public NavigationHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
                // 构建用户对象
                User user = User.builder()
                        .id(rs.getInt("user_id"))
                        .username(rs.getString("username"))
                        .userType(User.UserType.fromString(rs.getString("user_type")))
                        .build();
                
                // 构建起点地点对象
                com.campus.nav.model.Location startLocation = com.campus.nav.model.Location.builder()
                        .id(rs.getInt("start_location_id"))
                        .name(rs.getString("start_name"))
                        .build();
                
                // 构建终点地点对象
                com.campus.nav.model.Location endLocation = com.campus.nav.model.Location.builder()
                        .id(rs.getInt("end_location_id"))
                        .name(rs.getString("end_name"))
                        .build();
                
                return NavigationHistory.builder()
                        .id(rs.getInt("id"))
                        .userId(rs.getInt("user_id"))
                        .user(user)
                        .startLocationId(rs.getInt("start_location_id"))
                        .startLocation(startLocation)
                        .endLocationId(rs.getInt("end_location_id"))
                        .endLocation(endLocation)
                        .pathStrategy(NavigationStrategy.fromString(rs.getString("path_strategy")))
                        .totalDistance(rs.getDouble("total_distance"))
                        .totalTime(rs.getInt("total_time"))
                        .createdAt(rs.getTimestamp("created_at") != null ? 
                                rs.getTimestamp("created_at").toLocalDateTime() : null)
                        .build();
            }
        };
    }
}