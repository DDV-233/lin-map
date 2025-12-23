package com.campus.nav.dao.impl;

import com.campus.nav.dao.UserDao;
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
 * 用户DAO实现类
 */
public class UserDaoImpl extends AbstractBaseDao<User, Integer> implements UserDao {
    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);
    
    // SQL语句
    private static final String TABLE_NAME = "users";
    private static final String COLUMNS = "id, username, password, email, user_type, created_at, updated_at, is_active";
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + 
            "(username, password, email, user_type, is_active) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + 
            " SET username = ?, email = ?, user_type = ?, updated_at = NOW(), is_active = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " ORDER BY id";
    private static final String SELECT_BY_USERNAME = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE username = ?";
    private static final String SELECT_BY_EMAIL = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE email = ?";
    private static final String VALIDATE_USER = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + 
            " WHERE (username = ? OR email = ?) AND password = ? AND is_active = TRUE";
    private static final String EXISTS_BY_USERNAME = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE username = ?";
    private static final String EXISTS_BY_EMAIL = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE email = ?";
    private static final String UPDATE_PASSWORD = "UPDATE " + TABLE_NAME + 
            " SET password = ?, updated_at = NOW() WHERE id = ?";
    private static final String UPDATE_STATUS = "UPDATE " + TABLE_NAME + 
            " SET is_active = ?, updated_at = NOW() WHERE id = ?";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM " + TABLE_NAME;
    
    @Override
    public boolean save(User user) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(INSERT_SQL,
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getUserType() != null ? user.getUserType().name() : User.UserType.USER.name(),
                    user.getIsActive() != null ? user.getIsActive() : true);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("保存用户失败: {}", user.getUsername(), e);
            return false;
        }
    }
    
    @Override
    public boolean update(User user) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(UPDATE_SQL,
                    user.getUsername(),
                    user.getEmail(),
                    user.getUserType() != null ? user.getUserType().name() : User.UserType.USER.name(),
                    user.getIsActive() != null ? user.getIsActive() : true,
                    user.getId());
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("更新用户失败: {}", user.getId(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(DELETE_SQL, id);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("删除用户失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public Optional<User> findById(Integer id) {
        return queryForObject(SELECT_BY_ID, id);
    }
    
    @Override
    public List<User> findAll() {
        return queryForList(SELECT_ALL);
    }
    
    @Override
    public PageResult<User> findByPage(PageQuery query) {
        if (query == null) {
            query = PageQuery.defaultQuery();
        }
        
        // 构建查询SQL
        StringBuilder sqlBuilder = new StringBuilder("SELECT " + COLUMNS + " FROM " + TABLE_NAME);
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM " + TABLE_NAME);
        
        // 添加搜索条件
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = "%" + query.getKeyword() + "%";
            String whereClause = " WHERE username LIKE ? OR email LIKE ?";
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
            sqlBuilder.append(" ORDER BY id DESC");
        }
        
        // 添加分页
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        
        String sql = sqlBuilder.toString();
        String countSql = countSqlBuilder.toString();
        
        try {
            // 执行查询
            List<User> data;
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
            logger.error("分页查询用户失败", e);
            return PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public long count() {
        try {
            return DatabaseUtil.executeQueryForSingle(COUNT_SQL, Long.class);
        } catch (Exception e) {
            logger.error("统计用户总数失败", e);
            return 0;
        }
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return queryForObject(SELECT_BY_USERNAME, username);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return queryForObject(SELECT_BY_EMAIL, email);
    }
    
    @Override
    public Optional<User> validateUser(String username, String password) {
        List<User> users = DatabaseUtil.executeQuery(VALIDATE_USER, getRowMapper(),
                username, username, password);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
    
    @Override
    public boolean existsByUsername(String username) {
        try {
            Long count = DatabaseUtil.executeQueryForSingle(EXISTS_BY_USERNAME, Long.class, username);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("检查用户名是否存在失败: {}", username, e);
            return false;
        }
    }
    
    @Override
    public boolean existsByEmail(String email) {
        try {
            Long count = DatabaseUtil.executeQueryForSingle(EXISTS_BY_EMAIL, Long.class, email);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("检查邮箱是否存在失败: {}", email, e);
            return false;
        }
    }
    
    @Override
    public boolean updatePassword(Integer userId, String newPassword) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(UPDATE_PASSWORD, newPassword, userId);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("更新用户密码失败: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public boolean updateStatus(Integer userId, boolean isActive) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(UPDATE_STATUS, isActive, userId);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("更新用户状态失败: {}", userId, e);
            return false;
        }
    }
    
    @Override
    protected DatabaseUtil.RowMapper<User> getRowMapper() {
        return new DatabaseUtil.RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                return User.builder()
                        .id(rs.getInt("id"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .email(rs.getString("email"))
                        .userType(User.UserType.fromString(rs.getString("user_type")))
                        .createdAt(rs.getTimestamp("created_at") != null ? 
                                rs.getTimestamp("created_at").toLocalDateTime() : null)
                        .updatedAt(rs.getTimestamp("updated_at") != null ? 
                                rs.getTimestamp("updated_at").toLocalDateTime() : null)
                        .isActive(rs.getBoolean("is_active"))
                        .build();
            }
        };
    }
}