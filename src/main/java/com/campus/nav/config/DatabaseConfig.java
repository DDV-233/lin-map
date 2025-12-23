package com.campus.nav.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库配置和连接池管理类
 */
public class DatabaseConfig {
    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    private static BasicDataSource dataSource;
    private static Properties properties = new Properties();

    // 私有构造器，防止实例化
    private DatabaseConfig() {
    }

    /**
     * 初始化数据库连接池
     */
    public static void initialize() {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            
            if (input == null) {
                throw new RuntimeException("找不到配置文件 config.properties");
            }
            
            properties.load(input);
            logger.info("加载配置文件成功");
            
            // 初始化连接池
            initializeDataSource();
            
        } catch (IOException e) {
            logger.error("加载配置文件失败", e);
            throw new RuntimeException("加载配置文件失败", e);
        }
    }

    /**
     * 初始化数据源连接池
     */
    private static void initializeDataSource() {
        if (dataSource == null) {
            synchronized (DatabaseConfig.class) {
                if (dataSource == null) {
                    try {
                        dataSource = new BasicDataSource();
                        
                        // 基础连接配置
                        dataSource.setDriverClassName(properties.getProperty("db.driver"));
                        dataSource.setUrl(properties.getProperty("db.url"));
                        dataSource.setUsername(properties.getProperty("db.username"));
                        dataSource.setPassword(properties.getProperty("db.password"));
                        
                        // 连接池配置
                        dataSource.setInitialSize(Integer.parseInt(
                                properties.getProperty("db.pool.initialSize", "5")));
                        dataSource.setMaxTotal(Integer.parseInt(
                                properties.getProperty("db.pool.maxTotal", "20")));
                        dataSource.setMaxIdle(Integer.parseInt(
                                properties.getProperty("db.pool.maxIdle", "10")));
                        dataSource.setMinIdle(Integer.parseInt(
                                properties.getProperty("db.pool.minIdle", "5")));
                        dataSource.setMaxWaitMillis(Long.parseLong(
                                properties.getProperty("db.pool.maxWaitMillis", "10000")));
                        
                        // 连接验证配置
                        dataSource.setValidationQuery("SELECT 1");
                        dataSource.setTestOnBorrow(true);
                        dataSource.setTestWhileIdle(true);
                        dataSource.setTimeBetweenEvictionRunsMillis(30000);
                        
                        // 测试连接
                        try (Connection conn = dataSource.getConnection()) {
                            if (conn != null) {
                                logger.info("数据库连接池初始化成功");
                                logger.info("数据库URL: {}", properties.getProperty("db.url"));
                            }
                        }
                        
                    } catch (Exception e) {
                        logger.error("数据库连接池初始化失败", e);
                        throw new RuntimeException("数据库连接池初始化失败", e);
                    }
                }
            }
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        return dataSource.getConnection();
    }

    /**
     * 关闭数据源（应用关闭时调用）
     */
    public static void closeDataSource() {
        if (dataSource != null) {
            try {
                dataSource.close();
                logger.info("数据库连接池已关闭");
            } catch (SQLException e) {
                logger.error("关闭数据库连接池失败", e);
            }
        }
    }

    /**
     * 获取配置属性
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * 获取配置属性（带默认值）
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 获取连接池状态信息
     */
    public static String getPoolStatus() {
        if (dataSource == null) {
            return "连接池未初始化";
        }
        
        return String.format("连接池状态: 活动连接=%d, 空闲连接=%d, 最大连接=%d",
                dataSource.getNumActive(),
                dataSource.getNumIdle(),
                dataSource.getMaxTotal());
    }
}