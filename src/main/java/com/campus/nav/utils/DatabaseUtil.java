package com.campus.nav.utils;

import com.campus.nav.config.DatabaseConfig;
import com.campus.nav.exception.DatabaseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库操作工具类
 */
public class DatabaseUtil {
    private static final Logger logger = LogManager.getLogger(DatabaseUtil.class);

    /**
     * 执行更新操作（INSERT, UPDATE, DELETE）
     */
    public static int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            // 设置参数
            setParameters(pstmt, params);
            
            int result = pstmt.executeUpdate();
            logger.debug("SQL执行成功: {}, 影响行数: {}", sql, result);
            return result;
            
        } catch (SQLException e) {
            logger.error("SQL执行失败: {}", sql, e);
            throw new DatabaseException("数据库操作失败: " + e.getMessage(), e);
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    /**
     * 执行查询操作，返回单个值
     */
    public static <T> T executeQueryForSingle(String sql, Class<T> clazz, Object... params) {
        List<T> results = executeQuery(sql, (rs, rowNum) -> {
            if (clazz == Integer.class) {
                return clazz.cast(rs.getInt(1));
            } else if (clazz == Long.class) {
                return clazz.cast(rs.getLong(1));
            } else if (clazz == String.class) {
                return clazz.cast(rs.getString(1));
            } else if (clazz == Double.class) {
                return clazz.cast(rs.getDouble(1));
            } else if (clazz == Boolean.class) {
                return clazz.cast(rs.getBoolean(1));
            } else {
                throw new IllegalArgumentException("不支持的返回类型: " + clazz.getName());
            }
        }, params);
        
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 执行查询操作，使用RowMapper处理结果集
     */
    public static <T> List<T> executeQuery(String sql, RowMapper<T> rowMapper, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<T> results = new ArrayList<>();
        
        try {
            conn = DatabaseConfig.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            // 设置参数
            setParameters(pstmt, params);
            
            rs = pstmt.executeQuery();
            int rowNum = 0;
            
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rowNum++));
            }
            
            logger.debug("SQL查询成功: {}, 返回行数: {}", sql, results.size());
            return results;
            
        } catch (SQLException e) {
            logger.error("SQL查询失败: {}", sql, e);
            throw new DatabaseException("数据库查询失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    /**
     * 设置PreparedStatement参数
     */
    private static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }

    /**
     * 关闭数据库资源
     */
    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            logger.warn("关闭ResultSet失败", e);
        }
        
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            logger.warn("关闭Statement失败", e);
        }
        
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.warn("关闭Connection失败", e);
        }
    }

    /**
     * 开启事务
     */
    public static void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    /**
     * 提交事务
     */
    public static void commitTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
            } catch (SQLException e) {
                logger.error("提交事务失败", e);
                throw new DatabaseException("提交事务失败", e);
            }
        }
    }

    /**
     * 回滚事务
     */
    public static void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("回滚事务失败", e);
            }
        }
    }

    /**
     * 恢复自动提交模式
     */
    public static void restoreAutoCommit(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.warn("恢复自动提交失败", e);
            }
        }
    }

    /**
     * RowMapper接口（类似Spring JdbcTemplate的设计）
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(ResultSet rs, int rowNum) throws SQLException;
    }
}