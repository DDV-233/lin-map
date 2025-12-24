package com.campus.nav.controller;

import com.campus.nav.model.User;

import javax.swing.*;

/**
 * 控制器工厂类
 */
public class ControllerFactory {
    
    private ControllerFactory() {
        // 私有构造器，防止实例化
    }
    
    /**
     * 创建地点管理控制器
     */
    public static LocationManagementController createLocationManagementController(
            User currentUser, JFrame parentFrame) {
        return new LocationManagementController(currentUser, parentFrame);
    }
    
    /**
     * 创建地图控制器
     */
    public static MapController createMapController(
            com.campus.nav.view.MapPanel mapPanel, MainController mainController) {
        return new MapController(mapPanel, mainController);
    }
    
    // 可以添加其他控制器的创建方法
}