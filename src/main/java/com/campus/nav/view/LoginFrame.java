package com.campus.nav.view;

import com.campus.nav.controller.LoginController;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

/**
 * 登录界面
 */
public class LoginFrame extends JFrame {
    private LoginController controller;
    
    // 组件
    private JPanel mainPanel;
    private JPanel formPanel;
    private JPanel buttonPanel;
    
    private JLabel titleLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    private JButton loginButton;
    private JButton registerButton;
    private JButton cancelButton;
    
    public LoginFrame() {
        initComponents();
        initLayout();
        initStyle();
        initWindow();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        // 主面板
        mainPanel = new JPanel(new BorderLayout());
        
        // 标题标签
        titleLabel = new JLabel("校园导航系统");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 表单面板
        formPanel = new JPanel(new GridBagLayout());
        
        // 用户名标签和输入框
        usernameLabel = new JLabel("用户名:");
        usernameField = new JTextField(20);
        
        // 密码标签和输入框
        passwordLabel = new JLabel("密  码:");
        passwordField = new JPasswordField(20);
        
        // 按钮面板
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        // 按钮
        loginButton = new JButton("登录");
        registerButton = new JButton("注册");
        cancelButton = new JButton("取消");
    }
    
    /**
     * 初始化布局
     */
    private void initLayout() {
        // 设置主面板边框
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 添加标题
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 设置表单布局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 用户名行
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameField, gbc);
        
        // 密码行
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);
        
        // 添加表单到主面板
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // 按钮面板
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        // 添加按钮面板到主面板
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置内容面板
        setContentPane(mainPanel);
    }
    
    /**
     * 初始化样式
     */
    private void initStyle() {
        // 设置字体
        Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 24);
        Font labelFont = new Font("Microsoft YaHei", Font.PLAIN, 14);
        Font buttonFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        
        titleLabel.setFont(titleFont);
        usernameLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        
        loginButton.setFont(buttonFont);
        registerButton.setFont(buttonFont);
        cancelButton.setFont(buttonFont);
        
        // 设置按钮颜色
        loginButton.setBackground(new Color(70, 130, 180)); // 钢蓝色
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        
        registerButton.setBackground(new Color(60, 179, 113)); // 海洋绿
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        
        cancelButton.setBackground(new Color(220, 20, 60)); // 深红色
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        
        // 设置按钮边框
        loginButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 110, 160), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        
        registerButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 159, 93), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 0, 40), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        
        // 设置鼠标悬停效果
        setupButtonHoverEffect(loginButton, new Color(80, 140, 190), new Color(70, 130, 180));
        setupButtonHoverEffect(registerButton, new Color(70, 189, 123), new Color(60, 179, 113));
        setupButtonHoverEffect(cancelButton, new Color(230, 30, 70), new Color(220, 20, 60));
        
        // 设置输入框样式
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入用户名");
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入密码");
        
        // 设置面板背景
        mainPanel.setBackground(new Color(240, 245, 249)); // 浅蓝色背景
        formPanel.setBackground(new Color(240, 245, 249));
        buttonPanel.setBackground(new Color(240, 245, 249));
        
        // 设置标签颜色
        usernameLabel.setForeground(new Color(50, 50, 50));
        passwordLabel.setForeground(new Color(50, 50, 50));
    }
    
    /**
     * 设置按钮悬停效果
     */
    private void setupButtonHoverEffect(JButton button, Color hoverColor, Color normalColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normalColor);
            }
        });
    }
    
    /**
     * 初始化窗口
     */
    private void initWindow() {
        setTitle("校园导航系统 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // 居中显示
        setResizable(false);
        
        // 设置图标
        try {
            // 这里可以设置窗口图标，暂时使用默认图标
            // ImageIcon icon = new ImageIcon(getClass().getResource("/images/icon.png"));
            // setIconImage(icon.getImage());
        } catch (Exception e) {
            // 图标加载失败，使用默认
        }
    }
    
    /**
     * 设置控制器
     */
    public void setController(LoginController controller) {
        this.controller = controller;
    }
    
    // Getter方法供控制器使用
    
    public JTextField getUsernameField() {
        return usernameField;
    }
    
    public JPasswordField getPasswordField() {
        return passwordField;
    }
    
    public JButton getLoginButton() {
        return loginButton;
    }
    
    public JButton getRegisterButton() {
        return registerButton;
    }
    
    public JButton getCancelButton() {
        return cancelButton;
    }
    
    /**
     * 显示错误消息
     */
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, 
            message, "错误", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * 显示成功消息
     */
    public void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, 
            message, "成功", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 主方法用于测试
     */
    public static void main(String[] args) {
        try {
            // 设置UI外观
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}