package com.campus.nav.view;

import com.campus.nav.controller.MainController;
import com.campus.nav.model.Location;
import com.campus.nav.model.NavigationResult;
import com.campus.nav.model.NavigationStrategy;
import com.campus.nav.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * 主界面（占位类，将在下一步完善）
 */
public class MainFrame extends JFrame {
    private MainController controller;
    private final User currentUser;
    
    // 组件（占位）
    private JButton navigateButton = new JButton("导航");
    private JButton clearButton = new JButton("清除");
    private JComboBox<Location> startLocationComboBox = new JComboBox<>();
    private JComboBox<Location> endLocationComboBox = new JComboBox<>();
    private JComboBox<NavigationStrategy> strategyComboBox = new JComboBox<>();
    private MapPanel mapPanel = new MapPanel();
    
    // 菜单项
    private JMenuItem logoutMenuItem = new JMenuItem("注销");
    private JMenuItem exitMenuItem = new JMenuItem("退出");
    private JMenuItem aboutMenuItem = new JMenuItem("关于");
    private JMenuItem manageUsersMenuItem = new JMenuItem("用户管理");
    private JMenuItem manageLocationsMenuItem = new JMenuItem("地点管理");
    private JMenuItem managePathsMenuItem = new JMenuItem("路径管理");
    private JMenuItem viewHistoryMenuItem = new JMenuItem("查看历史");
    private JMenuItem viewMyHistoryMenuItem = new JMenuItem("我的历史");
    private JMenuItem changePasswordMenuItem = new JMenuItem("修改密码");
    
    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        initUI();
    }
    
    private void initUI() {
        setTitle("校园导航系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 创建菜单
        createMenuBar();
        
        // 设置布局
        setLayout(new BorderLayout());
        
        // 添加地图面板
        add(mapPanel, BorderLayout.CENTER);
        
        // 创建控制面板
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(new JLabel("起点:"));
        controlPanel.add(startLocationComboBox);
        controlPanel.add(new JLabel("终点:"));
        controlPanel.add(endLocationComboBox);
        controlPanel.add(new JLabel("策略:"));
        controlPanel.add(strategyComboBox);
        controlPanel.add(navigateButton);
        controlPanel.add(clearButton);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.add(logoutMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        
        // 工具菜单
        JMenu toolsMenu = new JMenu("工具");
        toolsMenu.add(viewMyHistoryMenuItem);
        toolsMenu.add(changePasswordMenuItem);
        
        // 管理菜单（管理员可见）
        JMenu adminMenu = new JMenu("管理");
        adminMenu.add(manageUsersMenuItem);
        adminMenu.add(manageLocationsMenuItem);
        adminMenu.add(managePathsMenuItem);
        adminMenu.add(viewHistoryMenuItem);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.add(aboutMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(adminMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    public void setController(MainController controller) {
        this.controller = controller;
    }
    
    // Getter方法
    public JButton getNavigateButton() { return navigateButton; }
    public JButton getClearButton() { return clearButton; }
    public JComboBox<Location> getStartLocationComboBox() { return startLocationComboBox; }
    public JComboBox<Location> getEndLocationComboBox() { return endLocationComboBox; }
    public JComboBox<NavigationStrategy> getStrategyComboBox() { return strategyComboBox; }
    public MapPanel getMapPanel() { return mapPanel; }
    
    // 菜单项Getter
    public JMenuItem getLogoutMenuItem() { return logoutMenuItem; }
    public JMenuItem getExitMenuItem() { return exitMenuItem; }
    public JMenuItem getAboutMenuItem() { return aboutMenuItem; }
    public JMenuItem getManageUsersMenuItem() { return manageUsersMenuItem; }
    public JMenuItem getManageLocationsMenuItem() { return manageLocationsMenuItem; }
    public JMenuItem getManagePathsMenuItem() { return managePathsMenuItem; }
    public JMenuItem getViewHistoryMenuItem() { return viewHistoryMenuItem; }
    public JMenuItem getViewMyHistoryMenuItem() { return viewMyHistoryMenuItem; }
    public JMenuItem getChangePasswordMenuItem() { return changePasswordMenuItem; }
    
    // 设置方法
    public void setNavigationStrategy(NavigationStrategy strategy) {
        strategyComboBox.setSelectedItem(strategy);
    }
    
    public void setNavigationResult(NavigationResult result) {
        // 将在后续实现
    }
    
    public void clearNavigationResult() {
        // 将在后续实现
    }
    
    public void setAdminFeaturesVisible(boolean visible) {
        // 将在后续实现
    }
}