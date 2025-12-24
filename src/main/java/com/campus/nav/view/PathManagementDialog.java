package com.campus.nav.view;

import com.campus.nav.model.Location;
import com.campus.nav.model.Path;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * 路径管理对话框
 */
public class PathManagementDialog extends JDialog {
    // 搜索面板
    private JPanel searchPanel;
    private JTextField searchField;
    private JCheckBox activeOnlyCheckBox;
    private JButton searchButton;
    
    // 表格面板
    private JPanel tablePanel;
    private JTable pathTable;
    private JScrollPane scrollPane;
    
    // 分页面板
    private JPanel paginationPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageLabel;
    
    // 表单面板
    private JPanel formPanel;
    private JLabel startLocationLabel;
    private JComboBox<Location> startLocationComboBox;
    private JLabel endLocationLabel;
    private JComboBox<Location> endLocationComboBox;
    private JLabel distanceLabel;
    private JTextField distanceField;
    private JLabel timeCostLabel;
    private JTextField timeCostField;
    private JLabel shadeLabel;
    private JCheckBox hasShadeCheckBox;
    private JLabel scenicLabel;
    private JSpinner scenicLevelSpinner;
    private JLabel indoorLabel;
    private JCheckBox isIndoorCheckBox;
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
    private JButton toggleStatusButton;
    
    public PathManagementDialog(JFrame parent) {
        super(parent, "路径管理", true);
        initComponents();
        initLayout();
        initStyle();
        initWindow();
    }
    
    private void initComponents() {
        // 搜索面板
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchField = new JTextField(20);
        activeOnlyCheckBox = new JCheckBox("仅显示可用路径");
        activeOnlyCheckBox.setSelected(true);
        searchButton = new JButton("搜索");
        
        // 表格
        pathTable = new JTable(new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"ID", "起点", "终点", "距离(米)", "时间(分钟)", "绿荫", "景色", "室内", "状态"}
        ));
        scrollPane = new JScrollPane(pathTable);
        
        // 分页面板
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        prevButton = new JButton("上一页");
        nextButton = new JButton("下一页");
        pageLabel = new JLabel("第 1/1 页，共 0 条记录");
        
        // 表单面板
        formPanel = new JPanel(new GridBagLayout());
        
        startLocationLabel = new JLabel("起点:");
        startLocationComboBox = new JComboBox<>();
        
        endLocationLabel = new JLabel("终点:");
        endLocationComboBox = new JComboBox<>();
        
        distanceLabel = new JLabel("距离(米):");
        distanceField = new JTextField(10);
        
        timeCostLabel = new JLabel("时间(分钟):");
        timeCostField = new JTextField(10);
        
        shadeLabel = new JLabel("绿荫:");
        hasShadeCheckBox = new JCheckBox("有绿荫");
        
        scenicLabel = new JLabel("景色等级:");
        scenicLevelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        
        indoorLabel = new JLabel("室内路径:");
        isIndoorCheckBox = new JCheckBox("是室内路径");
        
        statusLabel = new JLabel("状态:");
        isActiveCheckBox = new JCheckBox("可用");
        isActiveCheckBox.setSelected(true);
        
        // 按钮面板
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        addButton = new JButton("新增");
        editButton = new JButton("编辑");
        deleteButton = new JButton("删除");
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        closeButton = new JButton("关闭");
        toggleStatusButton = new JButton("切换状态");
    }
    
    private void initLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // 搜索面板布局
        searchPanel.add(new JLabel("搜索起点/终点:"));
        searchPanel.add(searchField);
        searchPanel.add(activeOnlyCheckBox);
        searchPanel.add(searchButton);
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
        
        // 起点行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(startLocationLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(startLocationComboBox, gbc);
        row++;
        
        // 终点行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(endLocationLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(endLocationComboBox, gbc);
        row++;
        
        // 距离行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(distanceLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(distanceField, gbc);
        row++;
        
        // 时间行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(timeCostLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(timeCostField, gbc);
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
        
        // 景色行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(scenicLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(scenicLevelSpinner, gbc);
        row++;
        
        // 室内行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(indoorLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(isIndoorCheckBox, gbc);
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
        westPanel.setBorder(BorderFactory.createTitledBorder("路径信息"));
        westPanel.setPreferredSize(new Dimension(350, 0));
        westPanel.add(formPanel, BorderLayout.NORTH);
        add(westPanel, BorderLayout.WEST);
        
        // 按钮面板布局
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonContainer.add(addButton);
        buttonContainer.add(editButton);
        buttonContainer.add(deleteButton);
        buttonContainer.add(toggleStatusButton);
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
        
        startLocationLabel.setFont(labelFont);
        endLocationLabel.setFont(labelFont);
        distanceLabel.setFont(labelFont);
        timeCostLabel.setFont(labelFont);
        shadeLabel.setFont(labelFont);
        scenicLabel.setFont(labelFont);
        indoorLabel.setFont(labelFont);
        statusLabel.setFont(labelFont);
        
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        saveButton.setFont(buttonFont);
        cancelButton.setFont(buttonFont);
        closeButton.setFont(buttonFont);
        toggleStatusButton.setFont(buttonFont);
        searchButton.setFont(buttonFont);
        prevButton.setFont(buttonFont);
        nextButton.setFont(buttonFont);
        
        // 设置按钮颜色
        setButtonStyle(addButton, new Color(40, 167, 69)); // 绿色
        setButtonStyle(editButton, new Color(0, 123, 255)); // 蓝色
        setButtonStyle(deleteButton, new Color(220, 53, 69)); // 红色
        setButtonStyle(toggleStatusButton, new Color(255, 193, 7)); // 黄色
        setButtonStyle(saveButton, new Color(40, 167, 69)); // 绿色
        setButtonStyle(cancelButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(closeButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(searchButton, new Color(0, 123, 255)); // 蓝色
        setButtonStyle(prevButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(nextButton, new Color(108, 117, 125)); // 灰色
        
        // 设置表格
        pathTable.setRowHeight(25);
        pathTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        pathTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        
        // 设置Spinner大小
        scenicLevelSpinner.setPreferredSize(new Dimension(50, 25));
        
        // 初始禁用保存和取消按钮
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        toggleStatusButton.setEnabled(false);
        
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
        setSize(1100, 700);
        setLocationRelativeTo(getParent());
        setResizable(true);
    }
    
    // Getter方法
    public JTable getPathTable() { return pathTable; }
    public JComboBox<Location> getStartLocationComboBox() { return startLocationComboBox; }
    public JComboBox<Location> getEndLocationComboBox() { return endLocationComboBox; }
    public JTextField getDistanceField() { return distanceField; }
    public JTextField getTimeCostField() { return timeCostField; }
    public JCheckBox getHasShadeCheckBox() { return hasShadeCheckBox; }
    public JSpinner getScenicLevelSpinner() { return scenicLevelSpinner; }
    public JCheckBox getIsIndoorCheckBox() { return isIndoorCheckBox; }
    public JCheckBox getIsActiveCheckBox() { return isActiveCheckBox; }
    public JButton getAddButton() { return addButton; }
    public JButton getEditButton() { return editButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getSaveButton() { return saveButton; }
    public JButton getCancelButton() { return cancelButton; }
    public JButton getCloseButton() { return closeButton; }
    public JButton getToggleStatusButton() { return toggleStatusButton; }
    public JButton getSearchButton() { return searchButton; }
    public JTextField getSearchField() { return searchField; }
    public JCheckBox getActiveOnlyCheckBox() { return activeOnlyCheckBox; }
    public JButton getPrevButton() { return prevButton; }
    public JButton getNextButton() { return nextButton; }
    public JLabel getPageLabel() { return pageLabel; }
    
    /**
     * 设置表单可编辑状态
     */
    public void setFormEditable(boolean editable) {
        startLocationComboBox.setEnabled(editable);
        endLocationComboBox.setEnabled(editable);
        distanceField.setEditable(editable);
        timeCostField.setEditable(editable);
        hasShadeCheckBox.setEnabled(editable);
        scenicLevelSpinner.setEnabled(editable);
        isIndoorCheckBox.setEnabled(editable);
        isActiveCheckBox.setEnabled(editable);
    }
    
    /**
     * 清空表单
     */
    public void clearForm() {
        startLocationComboBox.setSelectedIndex(-1);
        endLocationComboBox.setSelectedIndex(-1);
        distanceField.setText("");
        timeCostField.setText("");
        hasShadeCheckBox.setSelected(false);
        scenicLevelSpinner.setValue(1);
        isIndoorCheckBox.setSelected(false);
        isActiveCheckBox.setSelected(true);
        pathTable.clearSelection();
    }
    
    /**
     * 获取搜索关键词
     */
    public String getSearchKeyword() {
        return searchField.getText().trim();
    }
    
    /**
     * 是否只显示可用路径
     */
    public boolean isActiveOnly() {
        return activeOnlyCheckBox.isSelected();
    }
}