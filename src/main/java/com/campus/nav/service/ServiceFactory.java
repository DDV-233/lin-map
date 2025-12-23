package com.campus.nav.service;

import com.campus.nav.service.impl.*;

/**
 * Service工厂类，用于获取各个Service的实例
 */
public class ServiceFactory {
    
    private static volatile UserService userService;
    private static volatile LocationService locationService;
    private static volatile PathService pathService;
    private static volatile NavigationService navigationService;
    
    private ServiceFactory() {
        // 私有构造器，防止实例化
    }
    
    /**
     * 获取用户Service实例
     */
    public static UserService getUserService() {
        if (userService == null) {
            synchronized (ServiceFactory.class) {
                if (userService == null) {
                    userService = new UserServiceImpl();
                }
            }
        }
        return userService;
    }
    
    /**
     * 获取地点Service实例
     */
    public static LocationService getLocationService() {
        if (locationService == null) {
            synchronized (ServiceFactory.class) {
                if (locationService == null) {
                    locationService = new LocationServiceImpl();
                }
            }
        }
        return locationService;
    }
    
    /**
     * 获取路径Service实例
     */
    public static PathService getPathService() {
        if (pathService == null) {
            synchronized (ServiceFactory.class) {
                if (pathService == null) {
                    pathService = new PathServiceImpl();
                }
            }
        }
        return pathService;
    }
    
    /**
     * 获取导航Service实例
     */
    public static NavigationService getNavigationService() {
        if (navigationService == null) {
            synchronized (ServiceFactory.class) {
                if (navigationService == null) {
                    navigationService = new NavigationServiceImpl();
                }
            }
        }
        return navigationService;
    }
}