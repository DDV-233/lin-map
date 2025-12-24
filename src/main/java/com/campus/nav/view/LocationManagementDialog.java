package com.campus.nav.view;

import com.campus.nav.model.Location;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * 地点管理对话框
 */
public class LocationManagementDialog extends JDialog {
    // 搜索面板
    private JPanel searchPanel;
    private JTextField searchField;
    private JButton searchButton;
    private JComboBox<String> typeFilterComboBox;
    
    // 表格面板
    private JPanel tablePanel;
    private JTable locationTable;
    private JScrollPane scrollPane;
    
    // 分页面板
    private JPanel paginationPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageLabel;
    
    // 表单面板
    private JPanel formPanel;
    private JLabel nameLabel;
    private JTextField nameField;
    private JLabel descriptionLabel;
    private JTextArea descriptionArea;
    private JScrollPane descriptionScrollPane;
    private JLabel typeLabel;
    private JComboBox<Location.LocationType> typeComboBox;
    private JLabel coordinateLabel;
    private JTextField xField;
    private JLabel commaLabel;
    private JTextField yField;
    private JLabel shadeLabel;
    private JCheckBox hasShadeCheckBox;
    private JLabel scenicLabel;
    private JTextField scenicLevelField;
    private JLabel accessibleLabel;
    private JCheckBox isAccessibleCheckBox;
    
    // 按钮面板
    private JPanel buttonPanel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton closeButton;
    
    public LocationManagementDialog(JFrame parent) {
        super(parent, "地点管理", true);
        initComponents();
        initLayout();
        initStyle();
        initWindow();
    }
    
    private void initComponents() {
        // 搜索面板
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = new JButton("搜索");
        typeFilterComboBox = new JComboBox<>(new String[]{"所有类型", "教学楼", "花园", "食堂", "图书馆", "体育场馆", "校门"});
        
        // 表格
        locationTable = new JTable(new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"ID", "名称", "类型", "坐标", "绿荫", "景色", "可通行"}
        ));
        scrollPane = new JScrollPane(locationTable);
        
        // 分页面板
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevButton = new JButton("上一页");
        nextButton = new JButton("下一页");
        pageLabel = new JLabel("第 1/1 页，共 0 条记录");
        
        // 表单面板
        formPanel = new JPanel(new GridBagLayout());
        
        nameLabel = new JLabel("名称:");
        nameField = new JTextField(15);
        
        descriptionLabel = new JLabel("描述:");
        descriptionArea = new JTextArea(3, 15);
        descriptionScrollPane = new JScrollPane(descriptionArea);
        
        typeLabel = new JLabel("类型:");
        typeComboBox = new JComboBox<>(Location.LocationType.values());
        
        coordinateLabel = new JLabel("坐标:");
        xField = new JTextField(5);
        commaLabel = new JLabel(",");
        yField = new JTextField(5);
        
        shadeLabel = new JLabel("绿荫:");
        hasShadeCheckBox = new JCheckBox();
        
        scenicLabel = new JLabel("景色等级(1-5):");
        scenicLevelField = new JTextField(3);
        scenicLevelField.setText("1");
        
        accessibleLabel = new JLabel("可通行:");
        isAccessibleCheckBox = new JCheckBox();
        isAccessibleCheckBox.setSelected(true);
        
        // 按钮面板
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("新增");
        editButton = new JButton("编辑");
        deleteButton = new JButton("删除");
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        closeButton = new JButton("关闭");
    }
    
    private void initLayout() {
        setLayout(new BorderLayout());
        
        // 搜索面板布局
        searchPanel.add(new JLabel("搜索:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(new JLabel("类型过滤:"));
        searchPanel.add(typeFilterComboBox);
        add(searchPanel, BorderLayout.NORTH);
        
        // 表格和分页布局
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextButton);
        centerPanel.add(paginationPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 表单布局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // 名称行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(nameField, gbc);
        row++;
        
        // 描述行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(descriptionLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(descriptionScrollPane, gbc);
        row++;
        
        // 类型行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(typeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(typeComboBox, gbc);
        row++;
        
        // 坐标行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(coordinateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(xField, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(commaLabel, gbc);
        
        gbc.gridx = 3;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(yField, gbc);
        row++;
        
        // 绿荫行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(shadeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(hasShadeCheckBox, gbc);
        row++;
        
        // 景色等级行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(scenicLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(scenicLevelField, gbc);
        row++;
        
        // 可通行行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(accessibleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(isAccessibleCheckBox, gbc);
        
        // 添加表单到西侧
        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.setBorder(BorderFactory.createTitledBorder("地点信息"));
        westPanel.add(formPanel, BorderLayout.NORTH);
        add(westPanel, BorderLayout.WEST);
        
        // 按钮面板布局
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void initStyle() {
        // 设置字体
        Font labelFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Font buttonFont = new Font("Microsoft YaHei", Font.PLAIN, 11);
        
        nameLabel.setFont(labelFont);
        descriptionLabel.setFont(labelFont);
        typeLabel.setFont(labelFont);
        coordinateLabel.setFont(labelFont);
        shadeLabel.setFont(labelFont);
        scenicLabel.setFont(labelFont);
        accessibleLabel.setFont(labelFont);
        
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        saveButton.setFont(buttonFont);
        cancelButton.setFont(buttonFont);
        closeButton.setFont(buttonFont);
        searchButton.setFont(buttonFont);
        prevButton.setFont(buttonFont);
        nextButton.setFont(buttonFont);
        
        // 设置按钮颜色
        setButtonStyle(addButton, new Color(40, 167, 69)); // 绿色
        setButtonStyle(editButton, new Color(0, 123, 255)); // 蓝色
        setButtonStyle(deleteButton, new Color(220, 53, 69)); // 红色
        setButtonStyle(saveButton, new Color(40, 167, 69)); // 绿色
        setButtonStyle(cancelButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(closeButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(searchButton, new Color(0, 123, 255)); // 蓝色
        setButtonStyle(prevButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(nextButton, new Color(108, 117, 125)); // 灰色
        
        // 设置表格
        locationTable.setRowHeight(25);
        locationTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        locationTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        
        // 设置文本区域
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        // 初始禁用保存和取消按钮
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        
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
        setSize(900, 600);
        setLocationRelativeTo(getParent());
        setResizable(true);
    }
    
    // Getter方法
    public JTable getLocationTable() { return locationTable; }
    public JTextField getNameField() { return nameField; }
    public JTextArea getDescriptionArea() { return descriptionArea; }
    public JComboBox<Location.LocationType> getTypeComboBox() { return typeComboBox; }
    public JTextField getXField() { return xField; }
    public JTextField getYField() { return yField; }
    public JCheckBox getHasShadeCheckBox() { return hasShadeCheckBox; }
    public JTextField getScenicLevelField() { return scenicLevelField; }
    public JCheckBox getIsAccessibleCheckBox() { return isAccessibleCheckBox; }
    public JButton getAddButton() { return addButton; }
    public JButton getEditButton() { return editButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getSaveButton() { return saveButton; }
    public JButton getCancelButton() { return cancelButton; }
    public JButton getCloseButton() { return closeButton; }
    public JButton getSearchButton() { return searchButton; }
    public JTextField getSearchField() { return searchField; }
    public JComboBox<String> getTypeFilterComboBox() { return typeFilterComboBox; }
    public JButton getPrevButton() { return prevButton; }
    public JButton getNextButton() { return nextButton; }
    public JLabel getPageLabel() { return pageLabel; }
    
    /**
     * 设置表单可编辑状态
     */
    public void setFormEditable(boolean editable) {
        nameField.setEditable(editable);
        descriptionArea.setEditable(editable);
        typeComboBox.setEnabled(editable);
        xField.setEditable(editable);
        yField.setEditable(editable);
        hasShadeCheckBox.setEnabled(editable);
        scenicLevelField.setEditable(editable);
        isAccessibleCheckBox.setEnabled(editable);
    }
    
    /**
     * 获取搜索关键词
     */
    public String getSearchKeyword() {
        return searchField.getText().trim();
    }
}