package com.campus.nav.view;

import com.campus.nav.controller.MainController;
import com.campus.nav.model.*;
import com.campus.nav.config.SystemConfig;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 主界面 - 校园导航系统
 */
public class MainFrame extends JFrame {
    private MainController controller;
    private final User currentUser;

    // 主布局面板
    private JPanel mainPanel;
    private JSplitPane mainSplitPane;

    // 左侧控制面板
    private JPanel leftPanel;
    private JPanel controlPanel;
    private JPanel locationPanel;
    private JPanel strategyPanel;
    private JPanel resultPanel;
    private JPanel buttonPanel;

    // 中间地图面板
    private MapPanel mapPanel;

    // 组件
    private JLabel titleLabel;
    private JLabel welcomeLabel;
    private JLabel startLocationLabel;
    private JLabel endLocationLabel;
    private JLabel strategyLabel;

    private JComboBox<Location> startLocationComboBox;
    private JComboBox<Location> endLocationComboBox;
    private JComboBox<NavigationStrategy> strategyComboBox;

    private JButton navigateButton;
    private JButton clearButton;
    private JButton refreshButton;

    // 结果显示组件
    private JTextArea resultTextArea;
    private JScrollPane resultScrollPane;

    // 菜单
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu toolsMenu;
    private JMenu adminMenu;
    private JMenu helpMenu;

    private JMenuItem logoutMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem viewMyHistoryMenuItem;
    private JMenuItem changePasswordMenuItem;
    private JMenuItem manageUsersMenuItem;
    private JMenuItem manageLocationsMenuItem;
    private JMenuItem managePathsMenuItem;
    private JMenuItem viewHistoryMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenuItem settingsMenuItem;

    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        initComponents();
        initLayout();
        initStyle();
        initWindow();
        initData();
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        // 主面板
        mainPanel = new JPanel(new BorderLayout());

        // 左侧控制面板
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        // 标题和欢迎标签
        titleLabel = new JLabel("校园导航系统");
        welcomeLabel = new JLabel("欢迎, " + currentUser.getUsername());

        // 控制面板
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        // 地点选择面板
        locationPanel = new JPanel(new GridBagLayout());
        locationPanel.setBorder(BorderFactory.createTitledBorder("地点选择"));

        startLocationLabel = new JLabel("起点:");
        endLocationLabel = new JLabel("终点:");
        startLocationComboBox = new JComboBox<>();
        endLocationComboBox = new JComboBox<>();

        // 策略选择面板
        strategyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        strategyPanel.setBorder(BorderFactory.createTitledBorder("导航策略"));

        strategyLabel = new JLabel("策略:");
        strategyComboBox = new JComboBox<>(NavigationStrategy.values());

        // 结果面板
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("导航结果"));

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        resultScrollPane = new JScrollPane(resultTextArea);

        // 按钮面板
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        navigateButton = new JButton("开始导航");
        clearButton = new JButton("清除选择");
        refreshButton = new JButton("刷新地图");

        // 地图面板
        mapPanel = new MapPanel();

        // 创建菜单
        createMenuBar();
    }

    /**
     * 初始化布局
     */
    private void initLayout() {
        // 设置主面板
        setContentPane(mainPanel);

        // 标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(welcomeLabel, BorderLayout.EAST);

        leftPanel.add(titlePanel);
        leftPanel.add(Box.createVerticalStrut(10));

        // 地点选择布局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 起点行
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        locationPanel.add(startLocationLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        locationPanel.add(startLocationComboBox, gbc);

        // 终点行
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        locationPanel.add(endLocationLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        locationPanel.add(endLocationComboBox, gbc);

        controlPanel.add(locationPanel);
        controlPanel.add(Box.createVerticalStrut(10));

        // 策略选择布局
        strategyPanel.add(strategyLabel);
        strategyPanel.add(strategyComboBox);
        strategyPanel.add(Box.createHorizontalStrut(20));

        controlPanel.add(strategyPanel);
        controlPanel.add(Box.createVerticalStrut(10));

        // 结果面板布局
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);
        resultPanel.setPreferredSize(new Dimension(300, 200));

        controlPanel.add(resultPanel);
        controlPanel.add(Box.createVerticalStrut(10));

        // 按钮面板布局
        buttonPanel.add(navigateButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        controlPanel.add(buttonPanel);

        // 添加控制面板到左侧面板
        leftPanel.add(controlPanel);

        // 设置地图面板
        mapPanel.setPreferredSize(new Dimension(
                SystemConfig.getMapWidth(),
                SystemConfig.getMapHeight()
        ));

        // 创建分割面板
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, mapPanel);
        mainSplitPane.setDividerLocation(350);
        mainSplitPane.setResizeWeight(0.3);
        mainSplitPane.setOneTouchExpandable(true);

        mainPanel.add(mainSplitPane, BorderLayout.CENTER);

        // 设置菜单栏
        setJMenuBar(menuBar);
    }

    /**
     * 创建菜单栏
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();

        // 文件菜单
        fileMenu = new JMenu("文件");
        logoutMenuItem = new JMenuItem("注销");
        exitMenuItem = new JMenuItem("退出");
        fileMenu.add(logoutMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        // 工具菜单
        toolsMenu = new JMenu("工具");
        viewMyHistoryMenuItem = new JMenuItem("我的导航历史");
        changePasswordMenuItem = new JMenuItem("修改密码");
        settingsMenuItem = new JMenuItem("系统设置");
        toolsMenu.add(viewMyHistoryMenuItem);
        toolsMenu.add(changePasswordMenuItem);
        toolsMenu.addSeparator();
        toolsMenu.add(settingsMenuItem);

        // 管理菜单（管理员可见）
        adminMenu = new JMenu("管理");
        manageUsersMenuItem = new JMenuItem("用户管理");
        manageLocationsMenuItem = new JMenuItem("地点管理");
        managePathsMenuItem = new JMenuItem("路径管理");
        viewHistoryMenuItem = new JMenuItem("查看所有历史");
        adminMenu.add(manageUsersMenuItem);
        adminMenu.add(manageLocationsMenuItem);
        adminMenu.add(managePathsMenuItem);
        adminMenu.addSeparator();
        adminMenu.add(viewHistoryMenuItem);

        // 帮助菜单
        helpMenu = new JMenu("帮助");
        aboutMenuItem = new JMenuItem("关于系统");
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(adminMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
    }

    /**
     * 初始化样式
     */
    private void initStyle() {
        // 设置字体
        Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 18);
        Font welcomeFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Font labelFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Font buttonFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Font menuFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Font resultFont = new Font("Microsoft YaHei", Font.PLAIN, 11);

        titleLabel.setFont(titleFont);
        welcomeLabel.setFont(welcomeFont);
        startLocationLabel.setFont(labelFont);
        endLocationLabel.setFont(labelFont);
        strategyLabel.setFont(labelFont);

        navigateButton.setFont(buttonFont);
        clearButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);

        resultTextArea.setFont(resultFont);

        // 设置菜单字体
        fileMenu.setFont(menuFont);
        toolsMenu.setFont(menuFont);
        adminMenu.setFont(menuFont);
        helpMenu.setFont(menuFont);

        logoutMenuItem.setFont(menuFont);
        exitMenuItem.setFont(menuFont);
        viewMyHistoryMenuItem.setFont(menuFont);
        changePasswordMenuItem.setFont(menuFont);
        settingsMenuItem.setFont(menuFont);
        manageUsersMenuItem.setFont(menuFont);
        manageLocationsMenuItem.setFont(menuFont);
        managePathsMenuItem.setFont(menuFont);
        viewHistoryMenuItem.setFont(menuFont);
        aboutMenuItem.setFont(menuFont);

        // 设置颜色
        titleLabel.setForeground(new Color(0, 102, 204));
        welcomeLabel.setForeground(new Color(102, 102, 102));

        // 设置按钮样式
        setupButtonStyle(navigateButton, new Color(40, 167, 69)); // 绿色
        setupButtonStyle(clearButton, new Color(108, 117, 125)); // 灰色
        setupButtonStyle(refreshButton, new Color(0, 123, 255)); // 蓝色

        // 设置下拉框样式
        startLocationComboBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "选择起点...");
        endLocationComboBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "选择终点...");

        setNavigationStrategyComboBoxRenderer();

        // 设置面板背景
        leftPanel.setBackground(new Color(248, 249, 250));
        controlPanel.setBackground(new Color(248, 249, 250));
        locationPanel.setBackground(new Color(248, 249, 250));
        strategyPanel.setBackground(new Color(248, 249, 250));
        resultPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBackground(new Color(248, 249, 250));

        // 设置边框
        TitledBorder locationBorder = (TitledBorder) locationPanel.getBorder();
        locationBorder.setTitleFont(new Font("Microsoft YaHei", Font.BOLD, 12));

        TitledBorder strategyBorder = (TitledBorder) strategyPanel.getBorder();
        strategyBorder.setTitleFont(new Font("Microsoft YaHei", Font.BOLD, 12));

        TitledBorder resultBorder = (TitledBorder) resultPanel.getBorder();
        resultBorder.setTitleFont(new Font("Microsoft YaHei", Font.BOLD, 12));

        // 设置结果区域样式
        resultTextArea.setBackground(new Color(255, 255, 255));
        resultScrollPane.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218), 1));
    }

    /**
     * 设置按钮样式
     */
    private void setupButtonStyle(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        // 鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    /**
     * 初始化窗口
     */
    private void initWindow() {
        setTitle("校园导航系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 设置窗口图标
        try {
            // 可以在这里设置图标
            // ImageIcon icon = new ImageIcon(getClass().getResource("/images/icon.png"));
            // setIconImage(icon.getImage());
        } catch (Exception e) {
            // 忽略图标加载错误
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置默认策略
        strategyComboBox.setSelectedItem(NavigationStrategy.SHORTEST);

        // 设置地点下拉框的渲染器
        setLocationComboBoxRenderer(startLocationComboBox);
        setLocationComboBoxRenderer(endLocationComboBox);

        // 根据用户类型设置管理员菜单可见性
        setAdminFeaturesVisible(currentUser.getUserType() == User.UserType.ADMIN);

        // 更新窗口标题
        setTitle("校园导航系统 - " + currentUser.getUsername() +
                " (" + currentUser.getUserType().getDescription() + ")");
    }

    /**
     * 设置控制器
     */
    public void setController(MainController controller) {
        this.controller = controller;
    }

    // Getter方法
    public JComboBox<Location> getStartLocationComboBox() {
        return startLocationComboBox;
    }

    public JComboBox<Location> getEndLocationComboBox() {
        return endLocationComboBox;
    }

    public JComboBox<NavigationStrategy> getStrategyComboBox() {
        return strategyComboBox;
    }

    public JButton getNavigateButton() {
        return navigateButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    // 菜单项Getter
    public JMenuItem getLogoutMenuItem() {
        return logoutMenuItem;
    }

    public JMenuItem getExitMenuItem() {
        return exitMenuItem;
    }

    public JMenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public JMenuItem getManageUsersMenuItem() {
        return manageUsersMenuItem;
    }

    public JMenuItem getManageLocationsMenuItem() {
        return manageLocationsMenuItem;
    }

    public JMenuItem getManagePathsMenuItem() {
        return managePathsMenuItem;
    }

    public JMenuItem getViewHistoryMenuItem() {
        return viewHistoryMenuItem;
    }

    public JMenuItem getViewMyHistoryMenuItem() {
        return viewMyHistoryMenuItem;
    }

    public JMenuItem getChangePasswordMenuItem() {
        return changePasswordMenuItem;
    }

    public JMenuItem getSettingsMenuItem() {
        return settingsMenuItem;
    }

    /**
     * 设置导航策略
     */
    public void setNavigationStrategy(NavigationStrategy strategy) {
        if (strategy != null) {
            strategyComboBox.setSelectedItem(strategy);
        }
    }
    /**
     * 设置导航策略组合框的渲染器
     */
    private void setNavigationStrategyComboBoxRenderer() {
        strategyComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof NavigationStrategy) {
                    NavigationStrategy strategy = (NavigationStrategy) value;
                    setText(strategy.getDisplayName());

                    // 设置图标颜色
                    switch (strategy) {
                        case SHORTEST:
                            setIcon(createStrategyIcon(new Color(52, 152, 219))); // 蓝色
                            break;
                        case SHADIEST:
                            setIcon(createStrategyIcon(new Color(46, 204, 113))); // 绿色
                            break;
                        case MOST_SCENIC:
                            setIcon(createStrategyIcon(new Color(255, 193, 7))); // 黄色
                            break;
                    }
                } else if (value == null) {
                    setText("选择导航策略...");
                    setFont(getFont().deriveFont(Font.ITALIC));
                    setForeground(Color.GRAY);
                }

                return this;
            }
        });

        // 设置下拉框提示文本
        strategyComboBox.setToolTipText("选择导航策略");
    }

    /**
     * 设置地点下拉框的渲染器
     */
    private void setLocationComboBoxRenderer(JComboBox<Location> comboBox) {
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value == null) {
                    setText("请选择地点...");
                    setFont(getFont().deriveFont(Font.ITALIC));
                    setForeground(Color.GRAY);
                } else if (value instanceof Location) {
                    Location location = (Location) value;
                    String displayText = location.getName();

                    // 添加类型信息
                    if (location.getType() != null && !displayText.contains(location.getType().getDescription())) {
                        displayText += " (" + location.getType().getDescription() + ")";
                    }

                    setText(displayText);
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setForeground(Color.BLACK);
                }

                return this;
            }
        });
    }

    private Icon createStrategyIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(x + 2, y + 2, 8, 8);
                g2d.setColor(color.darker());
                g2d.drawOval(x + 2, y + 2, 8, 8);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }

    /**
     * 设置导航结果
     */
    public void setNavigationResult(NavigationResult result) {
        if (result == null || !result.isSuccess()) {
            resultTextArea.setText("导航失败: " + (result != null ? result.getErrorMessage() : "未知错误"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("导航策略: ").append(result.getStrategy().getDisplayName()).append("\n");
        sb.append("========================================\n");
        sb.append("总距离: ").append(String.format("%.1f", result.getTotalDistance())).append(" 米\n");
        sb.append("预计时间: ").append(result.getTotalTime()).append(" 分钟\n");
        sb.append("========================================\n");
        sb.append("路径详情:\n");

        int step = 1;
        for (Location location : result.getPathLocations()) {
            sb.append(step).append(". ").append(location.getName());
            if (step == 1) {
                sb.append(" (起点)");
            } else if (step == result.getPathLocations().size()) {
                sb.append(" (终点)");
            }
            sb.append("\n");
            step++;
        }

        sb.append("========================================\n");
        if (result.isHasShadeCoverage()) {
            sb.append("✓ 此路径绿荫覆盖较好\n");
        }
        sb.append("平均景色等级: ").append(String.format("%.1f", result.getAverageScenicLevel())).append("/5.0\n");

        resultTextArea.setText(sb.toString());
        resultTextArea.setCaretPosition(0); // 滚动到顶部
    }

    /**
     * 清除导航结果
     */
    public void clearNavigationResult() {
        resultTextArea.setText("");
        mapPanel.clearPath();
    }

    /**
     * 设置管理员功能可见性
     */
    public void setAdminFeaturesVisible(boolean visible) {
        adminMenu.setVisible(visible);
    }

    /**
     * 添加刷新按钮监听器
     */
    public void addRefreshButtonListener(ActionListener listener) {
        refreshButton.addActionListener(listener);
    }

    /**
     * 显示消息对话框
     */
    public void showMessageDialog(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * 显示确认对话框
     */
    public int showConfirmDialog(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
    }

    /**
     * 刷新地图显示
     */
    public void refreshMap() {
        mapPanel.repaint();
    }
}