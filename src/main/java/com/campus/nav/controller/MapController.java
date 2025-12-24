package com.campus.nav.controller;

import com.campus.nav.model.Location;
import com.campus.nav.model.PageQuery;
import com.campus.nav.model.Path;
import com.campus.nav.service.PathService;
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
    private final PathService pathService;
    private final MainController mainController;

    // 点击检测半径
    private static final int CLICK_RADIUS = 20;

    public MapController(MapPanel mapPanel, MainController mainController) {
        this.mapPanel = mapPanel;
        this.locationService = ServiceFactory.getLocationService();
        this.pathService = ServiceFactory.getPathService();
        this.mainController = mainController;

        initListeners();
        loadMapData();
    }

    /**
     * 初始化事件监听器
     */
    private void initListeners() {
        // 移除MapPanel原有的鼠标监听器，避免冲突
        for (var listener : mapPanel.getMouseListeners()) {
            mapPanel.removeMouseListener(listener);
        }
        for (var listener : mapPanel.getMouseMotionListeners()) {
            mapPanel.removeMouseMotionListener(listener);
        }

        // 添加新的鼠标监听器
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMapClick(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // 可以添加按下事件处理
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 可以添加释放事件处理
            }
        });

        mapPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateMousePosition(e.getX(), e.getY());
                updateHoveredLocation(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // 可以添加拖动事件处理
            }
        });
    }

    /**
     * 加载地图数据
     */
    private void loadMapData() {
        try {
            List<Location> locations = locationService.getMapLocations();
            List<Path> paths = pathService.findActivePaths();

            // 过滤掉坐标为空的地点
            List<Location> validLocations = locations.stream()
                    .filter(loc -> loc != null &&
                            loc.getXCoordinate() != null &&
                            loc.getYCoordinate() != null)
                    .toList();

            mapPanel.setLocations(validLocations);
            mapPanel.setPaths(paths);
            mapPanel.repaint();

            logger.info("地图数据加载完成，共 {} 个有效地点，{} 条路径",
                    validLocations.size(), paths.size());

        } catch (Exception e) {
            logger.error("加载地图数据失败", e);
            showErrorDialog("加载地图数据失败: " + e.getMessage());
        }
    }

    /**
     * 处理地图点击
     */
    private void handleMapClick(MouseEvent e) {
        logger.debug("地图点击事件: ({}, {}), 按钮: {}", e.getX(), e.getY(), e.getButton());

        Location clickedLocation = findLocationAt(e.getX(), e.getY());

        if (clickedLocation != null) {
            logger.info("点击了地点: {} (ID: {})", clickedLocation.getName(), clickedLocation.getId());

            // 右键点击直接显示详情
            if (e.getButton() == MouseEvent.BUTTON3) {
                showLocationDetails(clickedLocation);
                return;
            }

            // 左键点击显示选择菜单
            if (e.getButton() == MouseEvent.BUTTON1) {
                showLocationSelectionMenu(clickedLocation, e.getX(), e.getY());
            }
        } else {
            logger.debug("点击了空白区域，无地点");

            // 如果点击空白区域，可以清除选择
            if (e.getButton() == MouseEvent.BUTTON3) {
                // 清除路径显示
                mapPanel.clearPath();
                mapPanel.repaint();
            }
        }
    }

    /**
     * 显示地点选择菜单
     */
    private void showLocationSelectionMenu(Location location, int x, int y) {
        String[] options = {
                "设为起点",
                "设为终点",
                "设为起点并导航",
                "设为终点并导航",
                "查看详情",
                "取消"
        };

        int choice = JOptionPane.showOptionDialog(
                mapPanel,
                "选择对地点 \"" + location.getName() + "\" 的操作:",
                "地点操作",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case 0: // 设为起点
                setStartLocation(location);
                break;
            case 1: // 设为终点
                setEndLocation(location);
                break;
            case 2: // 设为起点并导航
                setStartLocation(location);
                if (mapPanel.getSelectedEndLocation() != null) {
                    triggerNavigation();
                } else {
                    showInfoDialog("请先选择终点，然后点击导航按钮");
                }
                break;
            case 3: // 设为终点并导航
                setEndLocation(location);
                if (mapPanel.getSelectedStartLocation() != null) {
                    triggerNavigation();
                } else {
                    showInfoDialog("请先选择起点，然后点击导航按钮");
                }
                break;
            case 4: // 查看详情
                showLocationDetails(location);
                break;
            default: // 取消，不做任何操作
                logger.debug("用户取消了操作");
        }
    }

    /**
     * 查找点击位置的地点
     */
    private Location findLocationAt(int x, int y) {
        for (Location location : mapPanel.getLocations()) {
            if (location == null || location.getXCoordinate() == null || location.getYCoordinate() == null) {
                continue;
            }

            int locX = (int) location.getXCoordinate().doubleValue();
            int locY = (int) location.getYCoordinate().doubleValue();

            double distance = Math.sqrt(Math.pow(x - locX, 2) + Math.pow(y - locY, 2));
            if (distance <= CLICK_RADIUS) {
                return location;
            }
        }

        return null;
    }

    /**
     * 更新悬停地点
     */
    private void updateHoveredLocation(int x, int y) {
        Location hoveredLocation = findLocationAt(x, y);

        // 获取地图面板当前悬停的地点
        Location currentHovered = mapPanel.getHoveredLocation();

        // 如果悬停状态有变化，更新并重绘
        if (hoveredLocation != currentHovered) {
            if (hoveredLocation != null) {
                logger.debug("鼠标悬停在地点上: {}", hoveredLocation.getName());
            }
            // 这里需要给MapPanel添加设置悬停地点的方法
            // mapPanel.setHoveredLocation(hoveredLocation);
            mapPanel.repaint();
        }
    }

    /**
     * 设置为起点
     */
    private void setStartLocation(Location location) {
        mapPanel.setSelectedStartLocation(location);
        mapPanel.repaint();

        // 更新主界面的下拉框选择
        if (mainController != null && mainController.getMainFrame() != null) {
            JComboBox<Location> startCombo = mainController.getMainFrame().getStartLocationComboBox();
            startCombo.setSelectedItem(location);
        }

        showSuccessDialog("已设置起点: " + location.getName());
        logger.info("设置起点: {}", location.getName());
    }

    /**
     * 设置为终点
     */
    private void setEndLocation(Location location) {
        mapPanel.setSelectedEndLocation(location);
        mapPanel.repaint();

        // 更新主界面的下拉框选择
        if (mainController != null && mainController.getMainFrame() != null) {
            JComboBox<Location> endCombo = mainController.getMainFrame().getEndLocationComboBox();
            endCombo.setSelectedItem(location);
        }

        showSuccessDialog("已设置终点: " + location.getName());
        logger.info("设置终点: {}", location.getName());
    }

    /**
     * 触发导航
     */
    private void triggerNavigation() {
        if (mainController != null && mainController.getMainFrame() != null) {
            JButton navigateButton = mainController.getMainFrame().getNavigateButton();
            navigateButton.doClick();
        }
    }

    /**
     * 显示地点详情
     */
    private void showLocationDetails(Location location) {
        if (location == null) {
            showErrorDialog("地点信息为空");
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("<html>");
        details.append("<h2 style='color:#2c3e50; margin-bottom:10px;'>")
                .append(location.getName()).append("</h2>");
        details.append("<div style='padding:10px; background-color:#f8f9fa; border-radius:5px;'>");

        details.append("<p><b>类型:</b> <span style='color:#3498db;'>")
                .append(location.getType().getDescription()).append("</span></p>");

        if (location.getDescription() != null && !location.getDescription().isEmpty()) {
            details.append("<p><b>描述:</b> ").append(location.getDescription()).append("</p>");
        }

        if (location.getXCoordinate() != null && location.getYCoordinate() != null) {
            details.append("<p><b>坐标:</b> (")
                    .append(location.getXCoordinate().intValue())
                    .append(", ").append(location.getYCoordinate().intValue()).append(")</p>");
        } else {
            details.append("<p><b>坐标:</b> <span style='color:#e74c3c;'>未设置</span></p>");
        }

        details.append("<p><b>绿荫:</b> ")
                .append(location.getHasShade() ?
                        "<span style='color:#27ae60;'>有</span>" :
                        "<span style='color:#e74c3c;'>无</span>")
                .append("</p>");

        details.append("<p><b>景色等级:</b> ")
                .append("<span style='color:#f39c12;'>")
                .append(location.getScenicLevel()).append("/5</span></p>");

        details.append("<p><b>可通行:</b> ")
                .append(location.getIsAccessible() ?
                        "<span style='color:#27ae60;'>是</span>" :
                        "<span style='color:#e74c3c;'>否</span>")
                .append("</p>");

        details.append("</div>");
        details.append("</html>");

        JOptionPane.showMessageDialog(
                mapPanel,
                details.toString(),
                "地点详情 - " + location.getName(),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * 更新鼠标位置显示
     */
    private void updateMousePosition(int x, int y) {
        mapPanel.setShowCoordinates(true);
        mapPanel.setMousePosition(x, y);

        // 如果鼠标移动过快，可能需要限制重绘频率
        mapPanel.repaint();
    }

    /**
     * 刷新地图
     */
    public void refreshMap() {
        loadMapData();
        mapPanel.repaint();
        logger.info("地图已刷新");
    }

    /**
     * 清除路径显示
     */
    public void clearPath() {
        mapPanel.clearPath();
        mapPanel.repaint();
        logger.info("已清除路径显示");
    }

    /**
     * 显示信息对话框
     */
    private void showInfoDialog(String message) {
        JOptionPane.showMessageDialog(
                mapPanel,
                message,
                "提示",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * 显示成功对话框
     */
    protected void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(
                mapPanel,
                message,
                "操作成功",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}