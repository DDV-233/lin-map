package com.campus.nav.controller;

import com.campus.nav.model.*;
import com.campus.nav.service.LocationService;
import com.campus.nav.service.NavigationService;
import com.campus.nav.service.PathService;
import com.campus.nav.service.ServiceFactory;
import com.campus.nav.view.MainFrame;
import com.campus.nav.view.MapPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * 主界面控制器
 */
public class MainController extends BaseController {
    private static final Logger logger = LogManager.getLogger(MainController.class);
    
    private final MainFrame mainFrame;
    private final User currentUser;
    private final LocationService locationService;
    private final PathService pathService;
    private final NavigationService navigationService;
    
    // 当前选择的状态
    private Location selectedStartLocation;
    private Location selectedEndLocation;
    private NavigationStrategy selectedStrategy;
    
    public MainController(MainFrame mainFrame, User currentUser) {
        this.mainFrame = mainFrame;
        this.currentUser = currentUser;
        this.locationService = ServiceFactory.getLocationService();
        this.pathService = ServiceFactory.getPathService();
        this.navigationService = ServiceFactory.getNavigationService();
        
        initData();
        initListeners();
        updateUI();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 加载地点数据
        loadLocations();
        // 加载路径数据
        loadPaths();

        // 设置默认导航策略
        selectedStrategy = NavigationStrategy.SHORTEST;
        mainFrame.setNavigationStrategy(selectedStrategy);

        // 创建地图控制器
        MapPanel mapPanel = mainFrame.getMapPanel();
        new MapController(mapPanel, this);

        logger.info("主界面控制器初始化完成，用户: {}", currentUser.getUsername());
    }
    

    /**
     * 初始化事件监听器
     */
    private void initListeners() {
        // 导航按钮事件
        mainFrame.getNavigateButton().addActionListener(this::handleNavigate);

        // 清除按钮事件
        mainFrame.getClearButton().addActionListener(this::handleClear);

        // 刷新按钮事件
        mainFrame.getRefreshButton().addActionListener(e -> handleRefresh());

        // 策略选择事件
        mainFrame.getStrategyComboBox().addActionListener(this::handleStrategyChange);

        // 地点选择事件
        mainFrame.getStartLocationComboBox().addActionListener(this::handleStartLocationChange);
        mainFrame.getEndLocationComboBox().addActionListener(this::handleEndLocationChange);

        // 菜单项事件
        mainFrame.getLogoutMenuItem().addActionListener(e -> handleLogout());
        mainFrame.getExitMenuItem().addActionListener(e -> handleExit());
        mainFrame.getAboutMenuItem().addActionListener(e -> handleAbout());
        mainFrame.getSettingsMenuItem().addActionListener(e -> handleSettings());

        // 管理员菜单项（仅管理员可见）
        if (currentUser.getUserType() == User.UserType.ADMIN) {
            mainFrame.getManageUsersMenuItem().addActionListener(e -> handleManageUsers());
            mainFrame.getManageLocationsMenuItem().addActionListener(e -> handleManageLocations());
            mainFrame.getManagePathsMenuItem().addActionListener(e -> handleManagePaths());
            mainFrame.getViewHistoryMenuItem().addActionListener(e -> handleViewHistory());
        }

        // 用户菜单项
        mainFrame.getViewMyHistoryMenuItem().addActionListener(e -> handleViewMyHistory());
        mainFrame.getChangePasswordMenuItem().addActionListener(e -> handleChangePassword());
    }
    
    /**
     * 加载地点数据
     */
    private void loadLocations() {
        try {
            List<Location> locations = locationService.findAccessibleLocations();
            
            // 更新下拉框
            DefaultComboBoxModel<Location> model = new DefaultComboBoxModel<Location>();
            locations.forEach(model::addElement);
            
            mainFrame.getStartLocationComboBox().setModel(model);
            mainFrame.getEndLocationComboBox().setModel(new DefaultComboBoxModel<Location>(
                    locations.toArray(new Location[0])
            ));
            
            // 更新地图面板
            MapPanel mapPanel = mainFrame.getMapPanel();
            mapPanel.setLocations(locations);
            
            logger.info("加载了 {} 个地点", locations.size());
            
        } catch (Exception e) {
            logger.error("加载地点数据失败", e);
            showErrorDialog("加载地点数据失败: " + e.getMessage());
        }
    }
    /*
    * 加载路径数据
     */
    private void loadPaths() {
        try {
            List<Path> paths = pathService.findActivePaths();
            MapPanel mapPanel = mainFrame.getMapPanel();
            mapPanel.setPaths(paths);
            mapPanel.repaint();

            logger.info("加载了 {} 个路径", paths.size());
        } catch (Exception ex) {
            logger.error("加载路径数据失败", ex);
            showErrorDialog("加载路径数据失败: " + ex.getMessage());
        }
    }
    /**
     * 处理导航
     */
    private void handleNavigate(ActionEvent e) {
        // 验证输入
        if (selectedStartLocation == null || selectedEndLocation == null) {
            showErrorDialog("请选择起点和终点");
            return;
        }
        
        if (selectedStartLocation.equals(selectedEndLocation)) {
            showErrorDialog("起点和终点不能相同");
            return;
        }
        
        // 执行导航
        try {
            logger.info("开始导航: {} -> {}, 策略: {}", 
                    selectedStartLocation.getName(), 
                    selectedEndLocation.getName(), 
                    selectedStrategy);
            
            NavigationResult result = navigationService.navigate(
                    selectedStartLocation.getId(),
                    selectedEndLocation.getId(),
                    selectedStrategy,
                    currentUser
            );
            
            if (result.isSuccess()) {
                // 更新结果面板
                mainFrame.setNavigationResult(result);
                
                // 更新地图显示路径
                MapPanel mapPanel = mainFrame.getMapPanel();
                mapPanel.setPathLocations(result.getPathLocations());
                mapPanel.repaint();
                
                // 显示导航详情
                showNavigationDetails(result);
                
                logger.info("导航成功: 距离={}米, 时间={}分钟", 
                        result.getTotalDistance(), result.getTotalTime());
                
            } else {
                showErrorDialog(result.getErrorMessage());
            }
            
        } catch (Exception ex) {
            logger.error("导航过程出错", ex);
            showErrorDialog("导航过程中出现错误: " + ex.getMessage());
        }
    }
    
    /**
     * 处理清除
     */
    private void handleClear(ActionEvent e) {
        // 清除选择
        selectedStartLocation = null;
        selectedEndLocation = null;
        
        // 更新UI
        mainFrame.getStartLocationComboBox().setSelectedItem(null);
        mainFrame.getEndLocationComboBox().setSelectedItem(null);
        
        // 清除地图路径
        MapPanel mapPanel = mainFrame.getMapPanel();
        mapPanel.clearPath();
        mapPanel.repaint();
        
        // 清除结果面板
        mainFrame.clearNavigationResult();
        
        logger.info("清除导航选择");
    }
    
    /**
     * 处理策略变更
     */
    private void handleStrategyChange(ActionEvent e) {
        Object selectedItem = mainFrame.getStrategyComboBox().getSelectedItem();
        if (selectedItem instanceof NavigationStrategy) {
            selectedStrategy = (NavigationStrategy) selectedItem;
            logger.debug("导航策略变更为: {}", selectedStrategy.getDisplayName());
        }
    }
    
    /**
     * 处理起点变更
     */
    private void handleStartLocationChange(ActionEvent e) {
        Object selectedItem = mainFrame.getStartLocationComboBox().getSelectedItem();
        if (selectedItem instanceof Location) {
            selectedStartLocation = (Location) selectedItem;
            
            // 更新地图选中状态
            MapPanel mapPanel = mainFrame.getMapPanel();
            mapPanel.setSelectedStartLocation(selectedStartLocation);
            mapPanel.repaint();
            
            logger.debug("起点变更为: {}", selectedStartLocation.getName());
        } else {
            selectedStartLocation = null;
            mainFrame.getMapPanel().setSelectedStartLocation(null);
        }
    }
    
    /**
     * 处理终点变更
     */
    private void handleEndLocationChange(ActionEvent e) {
        Object selectedItem = mainFrame.getEndLocationComboBox().getSelectedItem();
        if (selectedItem instanceof Location) {
            selectedEndLocation = (Location) selectedItem;
            
            // 更新地图选中状态
            MapPanel mapPanel = mainFrame.getMapPanel();
            mapPanel.setSelectedEndLocation(selectedEndLocation);
            mapPanel.repaint();
            
            logger.debug("终点变更为: {}", selectedEndLocation.getName());
        } else {
            selectedEndLocation = null;
            mainFrame.getMapPanel().setSelectedEndLocation(null);
        }
    }
    
    /**
     * 处理登出
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            mainFrame,
            "确定要注销当前用户吗？",
            "确认注销",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            logger.info("用户注销: {}", currentUser.getUsername());
            
            // 关闭主窗口
            mainFrame.setVisible(false);
            mainFrame.dispose();
            
            // 显示登录窗口
            SwingUtilities.invokeLater(() -> {
                com.campus.nav.view.LoginFrame loginFrame = new com.campus.nav.view.LoginFrame();
                LoginController loginController = new LoginController(loginFrame);
                loginFrame.setController(loginController);
                loginFrame.setVisible(true);
            });
        }
    }
    
    /**
     * 处理退出
     */
    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
            mainFrame,
            "确定要退出系统吗？",
            "确认退出",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            logger.info("用户退出系统");
            System.exit(0);
        }
    }
    
    /**
     * 处理关于
     */
    private void handleAbout() {
        String aboutMessage = """
            <html>
            <h2>校园导航系统</h2>
            <p>版本: 1.0.0</p>
            <p>开发者: Campus Navigation Team</p>
            <p>功能: 校园地图导航，支持最短路径、绿荫最多、景色最美三种导航策略</p>
            <p>当前用户: %s (%s)</p>
            </html>
            """.formatted(
                currentUser.getUsername(),
                currentUser.getUserType().getDescription()
            );
        
        JOptionPane.showMessageDialog(
            mainFrame,
            aboutMessage,
            "关于系统",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * 显示导航详情
     */
    private void showNavigationDetails(NavigationResult result) {
        StringBuilder details = new StringBuilder();
        details.append("<html>");
        details.append("<h2>导航详情</h2>");
        details.append("<p><b>策略:</b> ").append(result.getStrategy().getDisplayName()).append("</p>");
        details.append("<p><b>总距离:</b> ").append(String.format("%.1f", result.getTotalDistance())).append(" 米</p>");
        details.append("<p><b>预计时间:</b> ").append(result.getTotalTime()).append(" 分钟</p>");
        details.append("<p><b>路径:</b></p>");
        details.append("<ul>");
        
        List<Location> locations = result.getPathLocations();
        for (int i = 0; i < locations.size(); i++) {
            details.append("<li>").append(locations.get(i).getName());
            if (i == 0) details.append(" (起点)");
            if (i == locations.size() - 1) details.append(" (终点)");
            details.append("</li>");
        }
        
        details.append("</ul>");
        
        if (result.isHasShadeCoverage()) {
            details.append("<p><font color='green'>✓ 绿荫覆盖率较高</font></p>");
        }
        
        details.append("<p><b>平均景色等级:</b> ").append(String.format("%.1f", result.getAverageScenicLevel())).append("/5.0</p>");
        details.append("</html>");
        
        JOptionPane.showMessageDialog(
            mainFrame,
            details.toString(),
            "导航结果",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * 更新UI状态
     */
    private void updateUI() {
        // 根据用户类型显示/隐藏管理员功能
        boolean isAdmin = currentUser.getUserType() == User.UserType.ADMIN;
        mainFrame.setAdminFeaturesVisible(isAdmin);
        
        // 更新窗口标题
        mainFrame.setTitle("校园导航系统 - " + currentUser.getUsername() + 
                          " (" + currentUser.getUserType().getDescription() + ")");
        
        logger.debug("UI状态更新完成，用户类型: {}", currentUser.getUserType());
    }



    // 以下为管理员功能处理（待实现）

    /**
     * 处理用户管理
     */
    private void handleManageUsers() {
        try {
            new UserManagementController(currentUser, mainFrame);
        } catch (Exception e) {
            logger.error("打开用户管理失败", e);
            showErrorDialog("打开用户管理失败: " + e.getMessage());
        }
    }

    /**
     * 处理地点管理
     */
    private void handleManageLocations() {
        try {
            new LocationManagementController(currentUser, mainFrame);
        } catch (Exception e) {
            logger.error("打开地点管理失败", e);
            showErrorDialog("打开地点管理失败: " + e.getMessage());
        }
    }

    /**
     * 处理路径管理
     */
    private void handleManagePaths() {
        try {
            new PathManagementController(currentUser, mainFrame);
        } catch (Exception e) {
            logger.error("打开路径管理失败", e);
            showErrorDialog("打开路径管理失败: " + e.getMessage());
        }
    }

    /**
     * 处理查看历史（管理员）
     */
    private void handleViewHistory() {
        try {
            new NavigationHistoryController(currentUser, mainFrame, true);
        } catch (Exception e) {
            logger.error("打开历史记录失败", e);
            showErrorDialog("打开历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 处理查看我的历史
     */
    private void handleViewMyHistory() {
        try {
            new NavigationHistoryController(currentUser, mainFrame, false);
        } catch (Exception e) {
            logger.error("打开我的历史记录失败", e);
            showErrorDialog("打开我的历史记录失败: " + e.getMessage());
        }
    }
    
    private void handleChangePassword() {
        showWarningDialog("修改密码功能正在开发中...");
        // TODO: 实现修改密码界面
    }
    
    /**
     * 获取当前用户
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 处理刷新
     */
    private void handleRefresh() {
        loadLocations();
        loadPaths();
        showSuccessDialog("地图数据已刷新");
    }

    /**
     * 处理设置
     */
    private void handleSettings() {
        showWarningDialog("系统设置功能正在开发中...");
        // TODO: 实现系统设置界面
    }

    // 更新获取主窗口的方法
    public MainFrame getMainFrame() {
        return mainFrame;
    }


}