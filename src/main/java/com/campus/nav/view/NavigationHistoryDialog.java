package com.campus.nav.view;

import com.campus.nav.model.NavigationStrategy;
import com.campus.nav.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 导航历史查看对话框
 */
public class NavigationHistoryDialog extends JDialog {
    // 过滤面板
    private JPanel filterPanel;
    private JLabel userLabel;
    private JComboBox<User> userComboBox;
    private JLabel strategyLabel;
    private JComboBox<NavigationStrategy> strategyComboBox;
    private JLabel dateLabel;
    private JSpinner dateFromSpinner;
    private JLabel toLabel;
    private JSpinner dateToSpinner;
    private JButton filterButton;
    private JButton resetButton;
    private JButton exportButton;
    
    // 表格面板
    private JPanel tablePanel;
    private JTable historyTable;
    private JScrollPane scrollPane;
    
    // 分页面板
    private JPanel paginationPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageLabel;
    private JLabel totalLabel;
    
    // 详情面板
    private JPanel detailPanel;
    private JTextArea detailTextArea;
    private JScrollPane detailScrollPane;
    
    // 按钮面板
    private JPanel buttonPanel;
    private JButton viewDetailButton;
    private JButton deleteButton;
    private JButton deleteAllButton;
    private JButton closeButton;
    
    // 统计面板
    private JPanel statsPanel;
    private JLabel totalCountLabel;
    private JLabel totalDistanceLabel;
    private JLabel avgDistanceLabel;
    
    public NavigationHistoryDialog(JFrame parent, boolean showUserFilter) {
        super(parent, "导航历史记录", true);
        initComponents(showUserFilter);
        initLayout(showUserFilter);
        initStyle();
        initWindow();
    }
    
    private void initComponents(boolean showUserFilter) {
        // 过滤面板
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        if (showUserFilter) {
            userLabel = new JLabel("用户:");
            userComboBox = new JComboBox<>();
            filterPanel.add(userLabel);
            filterPanel.add(userComboBox);
        }
        
        strategyLabel = new JLabel("导航策略:");
        strategyComboBox = new JComboBox<>(NavigationStrategy.values());
        strategyComboBox.insertItemAt(null, 0); // 添加空选项
        strategyComboBox.setSelectedIndex(0);
        
        dateLabel = new JLabel("日期从:");
        dateFromSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(dateFromSpinner, "yyyy-MM-dd");
        dateFromSpinner.setEditor(fromEditor);
        
        toLabel = new JLabel("到:");
        dateToSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(dateToSpinner, "yyyy-MM-dd");
        dateToSpinner.setEditor(toEditor);
        
        filterButton = new JButton("筛选");
        resetButton = new JButton("重置");
        exportButton = new JButton("导出");
        
        // 表格
        historyTable = new JTable(new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"ID", "用户", "起点", "终点", "策略", "距离(米)", "时间(分钟)", "导航时间"}
        ));
        scrollPane = new JScrollPane(historyTable);
        
        // 分页面板
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        prevButton = new JButton("上一页");
        nextButton = new JButton("下一页");
        pageLabel = new JLabel("第 1/1 页");
        totalLabel = new JLabel("共 0 条记录");
        
        // 详情面板
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("导航详情"));
        detailTextArea = new JTextArea();
        detailTextArea.setEditable(false);
        detailTextArea.setLineWrap(true);
        detailTextArea.setWrapStyleWord(true);
        detailScrollPane = new JScrollPane(detailTextArea);
        
        // 按钮面板
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        viewDetailButton = new JButton("查看详情");
        deleteButton = new JButton("删除记录");
        deleteAllButton = new JButton("清空历史");
        closeButton = new JButton("关闭");
        
        // 统计面板
        statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        totalCountLabel = new JLabel("总记录数: 0");
        totalDistanceLabel = new JLabel("总距离: 0 米");
        avgDistanceLabel = new JLabel("平均距离: 0 米");
    }
    
    private void initLayout(boolean showUserFilter) {
        setLayout(new BorderLayout(10, 10));
        
        // 过滤面板布局
        filterPanel.add(strategyLabel);
        filterPanel.add(strategyComboBox);
        filterPanel.add(dateLabel);
        filterPanel.add(dateFromSpinner);
        filterPanel.add(toLabel);
        filterPanel.add(dateToSpinner);
        filterPanel.add(filterButton);
        filterPanel.add(resetButton);
        filterPanel.add(exportButton);
        add(filterPanel, BorderLayout.NORTH);
        
        // 主面板（表格+详情）
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 表格面板
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        // 统计面板
        statsPanel.add(totalCountLabel);
        statsPanel.add(totalDistanceLabel);
        statsPanel.add(avgDistanceLabel);
        tableContainer.add(statsPanel, BorderLayout.NORTH);
        
        // 分页面板
        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(totalLabel);
        paginationPanel.add(nextButton);
        tableContainer.add(paginationPanel, BorderLayout.SOUTH);
        
        mainPanel.add(tableContainer, BorderLayout.CENTER);
        
        // 详情面板
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        detailPanel.setPreferredSize(new Dimension(400, 0));
        mainPanel.add(detailPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 按钮面板布局
        buttonPanel.add(viewDetailButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(deleteAllButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void initStyle() {
        // 设置字体
        Font labelFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Font buttonFont = new Font("Microsoft YaHei", Font.PLAIN, 11);
        Font statsFont = new Font("Microsoft YaHei", Font.BOLD, 12);
        
        if (userLabel != null) userLabel.setFont(labelFont);
        strategyLabel.setFont(labelFont);
        dateLabel.setFont(labelFont);
        toLabel.setFont(labelFont);
        
        filterButton.setFont(buttonFont);
        resetButton.setFont(buttonFont);
        exportButton.setFont(buttonFont);
        prevButton.setFont(buttonFont);
        nextButton.setFont(buttonFont);
        viewDetailButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        deleteAllButton.setFont(buttonFont);
        closeButton.setFont(buttonFont);
        
        totalCountLabel.setFont(statsFont);
        totalDistanceLabel.setFont(statsFont);
        avgDistanceLabel.setFont(statsFont);
        
        // 设置按钮颜色
        setButtonStyle(filterButton, new Color(0, 123, 255)); // 蓝色
        setButtonStyle(resetButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(exportButton, new Color(40, 167, 69)); // 绿色
        setButtonStyle(prevButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(nextButton, new Color(108, 117, 125)); // 灰色
        setButtonStyle(viewDetailButton, new Color(0, 123, 255)); // 蓝色
        setButtonStyle(deleteButton, new Color(220, 53, 69)); // 红色
        setButtonStyle(deleteAllButton, new Color(220, 53, 69)); // 红色
        setButtonStyle(closeButton, new Color(108, 117, 125)); // 灰色
        
        // 设置表格
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        historyTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        
        // 设置详情区域
        detailTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        detailTextArea.setBackground(new Color(248, 249, 250));
        
        // 设置Spinner大小
        dateFromSpinner.setPreferredSize(new Dimension(120, 25));
        dateToSpinner.setPreferredSize(new Dimension(120, 25));
        
        // 初始禁用按钮
        viewDetailButton.setEnabled(false);
        deleteButton.setEnabled(false);
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
        setSize(1200, 700);
        setLocationRelativeTo(getParent());
        setResizable(true);
    }
    
    // Getter方法
    public JTable getHistoryTable() { return historyTable; }
    public JComboBox<User> getUserComboBox() { return userComboBox; }
    public JComboBox<NavigationStrategy> getStrategyComboBox() { return strategyComboBox; }
    public JSpinner getDateFromSpinner() { return dateFromSpinner; }
    public JSpinner getDateToSpinner() { return dateToSpinner; }
    public JButton getFilterButton() { return filterButton; }
    public JButton getResetButton() { return resetButton; }
    public JButton getExportButton() { return exportButton; }
    public JButton getPrevButton() { return prevButton; }
    public JButton getNextButton() { return nextButton; }
    public JLabel getPageLabel() { return pageLabel; }
    public JLabel getTotalLabel() { return totalLabel; }
    public JTextArea getDetailTextArea() { return detailTextArea; }
    public JButton getViewDetailButton() { return viewDetailButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getDeleteAllButton() { return deleteAllButton; }
    public JButton getCloseButton() { return closeButton; }
    public JLabel getTotalCountLabel() { return totalCountLabel; }
    public JLabel getTotalDistanceLabel() { return totalDistanceLabel; }
    public JLabel getAvgDistanceLabel() { return avgDistanceLabel; }
    
    /**
     * 获取策略过滤条件
     */
    public NavigationStrategy getSelectedStrategy() {
        Object selected = strategyComboBox.getSelectedItem();
        return selected instanceof NavigationStrategy ? (NavigationStrategy) selected : null;
    }
    
    /**
     * 获取开始日期
     */
    public LocalDateTime getDateFrom() {
        java.util.Date date = (java.util.Date) dateFromSpinner.getValue();
        return date != null ? 
            date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : 
            null;
    }
    
    /**
     * 获取结束日期
     */
    public LocalDateTime getDateTo() {
        java.util.Date date = (java.util.Date) dateToSpinner.getValue();
        return date != null ? 
            date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : 
            null;
    }
    
    /**
     * 获取选中的用户ID
     */
    public Integer getSelectedUserId() {
        User selectedUser = (User) userComboBox.getSelectedItem();
        return selectedUser != null ? selectedUser.getId() : null;
    }
    
    /**
     * 清空过滤器
     */
    public void resetFilters() {
        if (userComboBox != null) {
            userComboBox.setSelectedIndex(-1);
        }
        strategyComboBox.setSelectedIndex(0);
        dateFromSpinner.setValue(new java.util.Date());
        dateToSpinner.setValue(new java.util.Date());
    }
    
    /**
     * 更新统计信息
     */
    public void updateStatistics(int totalCount, double totalDistance, double avgDistance) {
        totalCountLabel.setText("总记录数: " + totalCount);
        totalDistanceLabel.setText(String.format("总距离: %.1f 米", totalDistance));
        avgDistanceLabel.setText(String.format("平均距离: %.1f 米", avgDistance));
    }
}