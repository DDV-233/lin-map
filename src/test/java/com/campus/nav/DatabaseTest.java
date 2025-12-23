package com.campus.nav;

import com.campus.nav.config.DatabaseConfig;
import com.campus.nav.utils.DatabaseUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;

import static org.junit.Assert.*;

public class DatabaseTest {
    
    @BeforeClass
    public static void setUp() {
        DatabaseConfig.initialize();
    }
    
    @Test
    public void testDatabaseConnection() {
        try {
            Connection conn = DatabaseConfig.getConnection();
            assertNotNull("数据库连接不应为null", conn);
            assertFalse("连接不应已关闭", conn.isClosed());
            conn.close();
            System.out.println("数据库连接测试通过");
        } catch (Exception e) {
            fail("数据库连接失败: " + e.getMessage());
        }
    }
    
    @Test
    public void testQueryDatabaseVersion() {
        try {
            String version = DatabaseUtil.executeQueryForSingle(
                    "SELECT VERSION()", String.class);
            assertNotNull("数据库版本不应为null", version);
            System.out.println("数据库版本: " + version);
        } catch (Exception e) {
            fail("查询数据库版本失败: " + e.getMessage());
        }
    }
    
    @AfterClass
    public static void tearDown() {
        DatabaseConfig.closeDataSource();
    }
}