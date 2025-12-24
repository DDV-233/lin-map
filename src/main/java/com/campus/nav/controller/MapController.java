package com.campus.nav.controller;

import com.campus.nav.model.Location;
import com.campus.nav.service.ServiceFactory;
import com.campus.nav.service.LocationService;
import com.campus.nav.view.MapPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 地图控制器
 */
public class MapController extends BaseController {
    private static final Logger logger = LogManager.getLogger(MapController.class);
    
    private final MapPanel mapPanel;
    private final LocationService locationService;
    private final MainController mainController;
    
    public MapController(MapPanel mapPanel, MainController mainController) {
        this.mapPanel = mapPanel;
        this.locationService = ServiceFactory.getLocationService();
        this.mainController = mainController;
        
        initListeners();
        loadMapData();
    }
    
    /**
     * 初始化事件监听器
     */
    private void initListeners() {
        // 鼠标点击事件
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMapClick(e);
            }
        });
        
        // 鼠标移动事件（显示坐标）
        mapPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateMousePosition(e.getX(), e.getY());
            }
        });
    }
    
    /**
     * 加载地图数据
     */
    private void loadMapData() {
        try {
            List<Location> locations = locationService.getMapLocations();
            mapPanel.setLocations(locations);
            mapPanel.repaint();
            
            logger.info("地图数据加载完成，共 {} 个地点", locations.size());
            
        } catch (Exception e) {
            logger.error("加载地图数据失败", e);
            showErrorDialog("加载地图数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理地图点击
     */
    private void handleMapClick(MouseEvent e) {
        Location clickedLocation = findLocationAt(e.getX(), e.getY());
        
        if (clickedLocation != null) {
            logger.debug("地图点击: {} ({}, {})", 
                    clickedLocation.getName(), e.getX(), e.getY());
            
            // 询问用户要设置为起点还是终点
            Object[] options = {"设为起点", "设为终点", "查看详情", "取消"};
            int choice = JOptionPane.showOptionDialog(
                mapPanel,
                "选择操作: " + clickedLocation.getName(),
                "地点操作",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );
            
            switch (choice) {
                case 0: // 设为起点
                    setStartLocation(clickedLocation);
                    break;
                case 1: // 设为终点
                    setEndLocation(clickedLocation);
                    break;
                case 2: // 查看详情
                    showLocationDetails(clickedLocation);
                    break;
                default:
                    // 取消，不做任何操作
            }
        }
    }
    
    /**
     * 查找点击位置的地点
     */
    private Location findLocationAt(int x, int y) {
        int clickRadius = 15; // 点击半径
        
        for (Location location : mapPanel.getLocations()) {
            int locX = (int) location.getXCoordinate().doubleValue();
            int locY = (int) location.getYCoordinate().doubleValue();
            
            double distance = Math.sqrt(Math.pow(x - locX, 2) + Math.pow(y - locY, 2));
            if (distance <= clickRadius) {
                return location;
            }
        }
        
        return null;
    }
    
    /**
     * 设置为起点
     */
    private void setStartLocation(Location location) {
        mapPanel.setSelectedStartLocation(location);
        mapPanel.repaint();
        
        // 更新主界面的下拉框选择
        if (mainController != null) {
            JComboBox<Location> startCombo = mainController.getMainFrame().getStartLocationComboBox();
            startCombo.setSelectedItem(location);
        }
        
        logger.info("设置起点: {}", location.getName());
    }
    
    /**
     * 设置为终点
     */
    private void setEndLocation(Location location) {
        mapPanel.setSelectedEndLocation(location);
        mapPanel.repaint();
        
        // 更新主界面的下拉框选择
        if (mainController != null) {
            JComboBox<Location> endCombo = mainController.getMainFrame().getEndLocationComboBox();
            endCombo.setSelectedItem(location);
        }
        
        logger.info("设置终点: {}", location.getName());
    }
    
    /**
     * 显示地点详情
     */
    private void showLocationDetails(Location location) {
        StringBuilder details = new StringBuilder();
        details.append("<html>");
        details.append("<h2>").append(location.getName()).append("</h2>");
        details.append("<p><b>类型:</b> ").append(location.getType().getDescription()).append("</p>");
        
        if (location.getDescription() != null && !location.getDescription().isEmpty()) {
            details.append("<p><b>描述:</b> ").append(location.getDescription()).append("</p>");
        }
        
        details.append("<p><b>坐标:</b> (").append(location.getXCoordinate().intValue())
               .append(", ").append(location.getYCoordinate().intValue()).append(")</p>");
        
        details.append("<p><b>绿荫:</b> ").append(location.getHasShade() ? "有" : "无").append("</p>");
        details.append("<p><b>景色等级:</b> ").append(location.getScenicLevel()).append("/5</p>");
        details.append("<p><b>可通行:</b> ").append(location.getIsAccessible() ? "是" : "否").append("</p>");
        
        details.append("</html>");
        
        JOptionPane.showMessageDialog(
            mapPanel,
            details.toString(),
            "地点详情",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * 更新鼠标位置显示
     */
    private void updateMousePosition(int x, int y) {
        // 可以在地图面板上显示坐标信息
        mapPanel.setMousePosition(x, y);
        mapPanel.repaint();
    }
    
    /**
     * 刷新地图
     */
    public void refreshMap() {
        loadMapData();
    }
    
    /**
     * 清除路径显示
     */
    public void clearPath() {
        mapPanel.clearPath();
        mapPanel.repaint();
    }
}