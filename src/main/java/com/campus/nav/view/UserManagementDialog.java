package com.campus.nav.view;

import com.campus.nav.model.User;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * 用户管理对话框
 */
public class UserManagementDialog extends JDialog {
    // 搜索面板
    private JPanel searchPanel;
    private JTextField searchField;
    private JComboBox<String> userTypeFilterComboBox;
    private JButton searchButton;
    private JButton resetButton;
    
    // 表格面板
    private JPanel tablePanel;
    private JTable userTable;
    private JScrollPane scrollPane;
    
    // 分页面板
    private JPanel paginationPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageLabel;
    
    // 表单面板
    private JPanel formPanel;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JLabel emailLabel;
    private JTextField emailField;
    private JLabel userTypeLabel;
    private JComboBox<User.UserType> userTypeComboBox;
    private JLabel statusLabel;
    private JCheckBox isActiveCheckBox;
    
    // 按钮面板
    private JPanel buttonPanel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton closeButton;
    private JButton resetPasswordButton;
    
    public UserManagementDialog(JFrame parent) {
        super(parent, "用户管理", true);
        initComponents();
        initLayout();
        initStyle();
        initWindow();
    }
    
    private void initComponents() {
        // 搜索面板
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchField = new JTextField(15);
        userTypeFilterComboBox = new JComboBox<>(new String[]{"所有类型", "管理员", "普通用户"});
        searchButton = new JButton("搜索");
        resetButton = new JButton("重置");
        
        // 表格
        userTable = new JTable(new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"ID", "用户名", "邮箱", "用户类型", "状态", "创建时间"}
        ));
        scrollPane = new JScrollPane(userTable);
        
        // 分页面板
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        prevButton = new JButton("上一页");
        nextButton = new JButton("下一页");
        pageLabel = new JLabel("第 1/1 页，共 0 条记录");
        
        // 表单面板
        formPanel = new JPanel(new GridBagLayout());
        
        usernameLabel = new JLabel("用户名:");
        usernameField = new JTextField(15);
        
        passwordLabel = new JLabel("密码:");
        passwordField = new JPasswordField(15);
        
        emailLabel = new JLabel("邮箱:");
        emailField = new JTextField(15);
        
        userTypeLabel = new JLabel("用户类型:");
        userTypeComboBox = new JComboBox<>(User.UserType.values());
        
        statusLabel = new JLabel("激活状态:");
        isActiveCheckBox = new JCheckBox("激活");
        
        // 按钮面板
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        addButton = new JButton("新增");
        editButton = new JButton("编辑");
        deleteButton = new JButton("删除");
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        closeButton = new JButton("关闭");
        resetPasswordButton = new JButton("重置密码");
    }
    
    private void initLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // 搜索面板布局
        searchPanel.add(new JLabel("用户名/邮箱:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("用户类型:"));
        searchPanel.add(userTypeFilterComboBox);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);
        add(searchPanel, BorderLayout.NORTH);
        
        // 表格和分页布局
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextButton);
        centerPanel.add(paginationPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 表单布局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // 用户名行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(usernameField, gbc);
        row++;
        
        // 密码行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(passwordField, gbc);
        row++;
        
        // 邮箱行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(emailField, gbc);
        row++;
        
        // 用户类型行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(userTypeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(userTypeComboBox, gbc);
        row++;
        
        // 状态行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(isActiveCheckBox, gbc);
        
        // 添加表单到西侧
        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.setBorder(BorderFactory.createTitledBorder("用户信息"));
        westPanel.setPreferredSize(new Dimension(300, 0));
        westPanel.add(formPanel, BorderLayout.NORTH);
        add(westPanel, BorderLayout.WEST);
        
        // 按钮面板布局
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonContainer.add(addButton);
        buttonContainer.add(editButton);
        buttonContainer.add(deleteButton);
        buttonContainer.add(resetPasswordButton);
        buttonContainer.add(saveButton);
        buttonContainer.add(cancelButton);
        buttonContainer.add(closeButton);
        
        buttonPanel.add(buttonContainer);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void initStyle() {
        // 设置字体
        Font labelFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Font buttonFont = new Font("Microsoft YaHei", Font.PLAIN, 11);
        
        usernameLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        emailLabel.setFont(labelFont);
        userTypeLabel.setFont(labelFont);
        statusLabel.setFont(labelFont);
        
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        saveButton.setFont(buttonFont);
        cancelButton.setFont(buttonFont);
        closeButton.setFont(buttonFont);
        resetPasswordButton.setFont(buttonFont);
        searchButton.setFont(buttonFont);
        resetButton.setFont(buttonFont);
        prevButton.setFont(buttonFont);
        nextButton.setFont(buttonFont);
        
        // 设置按钮颜色
        setButtonStyle(addButton, new Color(40, 167, 69)); // 绿色
        setButtonStyle(editButton, new Color(0, 123, 255)); // 蓝色
        setButtonStyle(deleteButton, new Color(220, 53, 69)); // 红色
        setButtonStyle(resetPasswordButton, new Color(255, 193, 7)); // 黄色
        setButtonStyle(saveButton, new Color(40, 167, 69)); // 绿色
        setButtonStyle(cancelButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(closeButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(searchButton, new Color(0, 123, 255)); // 蓝色
        setButtonStyle(resetButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(prevButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(nextButton, new Color(108, 117, 125)); // 灰色
        
        // 设置表格
        userTable.setRowHeight(25);
        userTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        userTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        
        // 设置输入框提示
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入用户名");
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "留空表示不修改");
        emailField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入邮箱");
        
        // 初始禁用保存和取消按钮
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        resetPasswordButton.setEnabled(false);
        
        // 设置表单不可编辑
        setFormEditable(false);
    }
    
    private void setButtonStyle(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // 悬停效果
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
    
    private void initWindow() {
        setSize(1000, 700);
        setLocationRelativeTo(getParent());
        setResizable(true);
    }
    
    // Getter方法
    public JTable getUserTable() { return userTable; }
    public JTextField getUsernameField() { return usernameField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JTextField getEmailField() { return emailField; }
    public JComboBox<User.UserType> getUserTypeComboBox() { return userTypeComboBox; }
    public JCheckBox getIsActiveCheckBox() { return isActiveCheckBox; }
    public JButton getAddButton() { return addButton; }
    public JButton getEditButton() { return editButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getSaveButton() { return saveButton; }
    public JButton getCancelButton() { return cancelButton; }
    public JButton getCloseButton() { return closeButton; }
    public JButton getResetPasswordButton() { return resetPasswordButton; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getResetButton() { return resetButton; }
    public JTextField getSearchField() { return searchField; }
    public JComboBox<String> getUserTypeFilterComboBox() { return userTypeFilterComboBox; }
    public JButton getPrevButton() { return prevButton; }
    public JButton getNextButton() { return nextButton; }
    public JLabel getPageLabel() { return pageLabel; }
    
    /**
     * 设置表单可编辑状态
     */
    public void setFormEditable(boolean editable) {
        usernameField.setEditable(editable);
        passwordField.setEditable(editable);
        emailField.setEditable(editable);
        userTypeComboBox.setEnabled(editable);
        isActiveCheckBox.setEnabled(editable);
    }
    
    /**
     * 清空表单
     */
    public void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        userTypeComboBox.setSelectedIndex(0);
        isActiveCheckBox.setSelected(true);
        userTable.clearSelection();
    }
    
    /**
     * 获取搜索关键词
     */
    public String getSearchKeyword() {
        return searchField.getText().trim();
    }
    
    /**
     * 获取用户类型过滤条件
     */
    public String getUserTypeFilter() {
        int index = userTypeFilterComboBox.getSelectedIndex();
        if (index == 0) return null; // 所有类型
        if (index == 1) return "ADMIN"; // 管理员
        return "USER"; // 普通用户
    }
}