package com.campus.nav.dao.impl;

import com.campus.nav.dao.SystemConfigDao;
import com.campus.nav.model.PageQuery;
import com.campus.nav.model.PageResult;
import com.campus.nav.model.SystemConfig;
import com.campus.nav.utils.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 系统配置DAO实现类
 */
public class SystemConfigDaoImpl extends AbstractBaseDao<SystemConfig, Integer> implements SystemConfigDao {
    private static final Logger logger = LogManager.getLogger(SystemConfigDaoImpl.class);
    
    // SQL语句
    private static final String TABLE_NAME = "system_config";
    private static final String COLUMNS = "id, config_key, config_value, description, updated_at";
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + 
            "(config_key, config_value, description) VALUES (?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + 
            " SET config_value = ?, description = ?, updated_at = NOW() WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String SELECT_ALL = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " ORDER BY config_key";
    private static final String SELECT_BY_KEY = "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE config_key = ?";
    private static final String UPDATE_VALUE = "UPDATE " + TABLE_NAME + 
            " SET config_value = ?, updated_at = NOW() WHERE config_key = ?";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM " + TABLE_NAME;
    
    @Override
    public boolean save(SystemConfig config) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(INSERT_SQL,
                    config.getConfigKey(),
                    config.getConfigValue(),
                    config.getDescription());
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("保存系统配置失败: {}", config.getConfigKey(), e);
            return false;
        }
    }
    
    @Override
    public boolean update(SystemConfig config) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(UPDATE_SQL,
                    config.getConfigValue(),
                    config.getDescription(),
                    config.getId());
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("更新系统配置失败: {}", config.getId(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(DELETE_SQL, id);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("删除系统配置失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public Optional<SystemConfig> findById(Integer id) {
        return queryForObject(SELECT_BY_ID, id);
    }
    
    @Override
    public List<SystemConfig> findAll() {
        return queryForList(SELECT_ALL);
    }
    
    @Override
    public PageResult<SystemConfig> findByPage(PageQuery query) {
        if (query == null) {
            query = PageQuery.defaultQuery();
        }
        
        // 构建查询SQL
        StringBuilder sqlBuilder = new StringBuilder("SELECT " + COLUMNS + " FROM " + TABLE_NAME);
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM " + TABLE_NAME);
        
        // 添加搜索条件
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = "%" + query.getKeyword() + "%";
            String whereClause = " WHERE config_key LIKE ? OR description LIKE ?";
            sqlBuilder.append(whereClause);
            countSqlBuilder.append(whereClause);
        }
        
        // 添加排序
        sqlBuilder.append(" ORDER BY config_key ASC");
        
        // 添加分页
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        
        String sql = sqlBuilder.toString();
        String countSql = countSqlBuilder.toString();
        
        try {
            // 执行查询
            List<SystemConfig> data;
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
            logger.error("分页查询系统配置失败", e);
            return PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public long count() {
        try {
            return DatabaseUtil.executeQueryForSingle(COUNT_SQL, Long.class);
        } catch (Exception e) {
            logger.error("统计系统配置总数失败", e);
            return 0;
        }
    }
    
    @Override
    public Optional<SystemConfig> findByKey(String configKey) {
        return queryForObject(SELECT_BY_KEY, configKey);
    }
    
    @Override
    public boolean updateValue(String configKey, String configValue) {
        try {
            int affectedRows = DatabaseUtil.executeUpdate(UPDATE_VALUE, configValue, configKey);
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("更新配置值失败: {}", configKey, e);
            return false;
        }
    }
    
    @Override
    public boolean updateBatch(List<SystemConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return true;
        }
        
        boolean allSuccess = true;
        for (SystemConfig config : configs) {
            if (config.getId() != null) {
                allSuccess = update(config) && allSuccess;
            }
        }
        return allSuccess;
    }
    
    @Override
    protected DatabaseUtil.RowMapper<SystemConfig> getRowMapper() {
        return new DatabaseUtil.RowMapper<SystemConfig>() {
            @Override
            public SystemConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
                return SystemConfig.builder()
                        .id(rs.getInt("id"))
                        .configKey(rs.getString("config_key"))
                        .configValue(rs.getString("config_value"))
                        .description(rs.getString("description"))
                        .updatedAt(rs.getTimestamp("updated_at") != null ? 
                                rs.getTimestamp("updated_at").toLocalDateTime() : null)
                        .build();
            }
        };
    }
}