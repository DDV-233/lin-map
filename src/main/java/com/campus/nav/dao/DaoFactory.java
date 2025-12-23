package com.campus.nav.dao;

import com.campus.nav.dao.impl.*;

/**
 * DAO工厂类，用于获取各个DAO的实例
 */
public class DaoFactory {
    
    private static volatile UserDao userDao;
    private static volatile LocationDao locationDao;
    private static volatile PathDao pathDao;
    private static volatile NavigationHistoryDao navigationHistoryDao;
    private static volatile SystemConfigDao systemConfigDao;
    
    private DaoFactory() {
        // 私有构造器，防止实例化
    }
    
    /**
     * 获取用户DAO实例
     */
    public static UserDao getUserDao() {
        if (userDao == null) {
            synchronized (DaoFactory.class) {
                if (userDao == null) {
                    userDao = new UserDaoImpl();
                }
            }
        }
        return userDao;
    }
    
    /**
     * 获取地点DAO实例
     */
    public static LocationDao getLocationDao() {
        if (locationDao == null) {
            synchronized (DaoFactory.class) {
                if (locationDao == null) {
                    locationDao = new LocationDaoImpl();
                }
            }
        }
        return locationDao;
    }
    
    /**
     * 获取路径DAO实例
     */
    public static PathDao getPathDao() {
        if (pathDao == null) {
            synchronized (DaoFactory.class) {
                if (pathDao == null) {
                    pathDao = new PathDaoImpl();
                }
            }
        }
        return pathDao;
    }
    
    /**
     * 获取导航历史DAO实例
     */
    public static NavigationHistoryDao getNavigationHistoryDao() {
        if (navigationHistoryDao == null) {
            synchronized (DaoFactory.class) {
                if (navigationHistoryDao == null) {
                    navigationHistoryDao = new NavigationHistoryDaoImpl();
                }
            }
        }
        return navigationHistoryDao;
    }
    
    /**
     * 获取系统配置DAO实例
     */
    public static SystemConfigDao getSystemConfigDao() {
        if (systemConfigDao == null) {
            synchronized (DaoFactory.class) {
                if (systemConfigDao == null) {
                    systemConfigDao = new SystemConfigDaoImpl();
                }
            }
        }
        return systemConfigDao;
    }
}