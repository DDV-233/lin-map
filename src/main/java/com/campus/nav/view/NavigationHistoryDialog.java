package com.campus.nav.view;

import com.campus.nav.model.NavigationStrategy;
import com.campus.nav.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * 导航历史查看对话框 - 完整修复版
 */
public class NavigationHistoryDialog extends JDialog {
    // 主容器
    private JPanel mainPanel;

    // 搜索面板
    private JPanel searchPanel;
    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchButton;

    // 过滤面板
    private JPanel filterPanel;
    private JPanel userFilterPanel;
    private JLabel userLabel;
    private JComboBox<User> userComboBox;
    private JPanel strategyFilterPanel;
    private JLabel strategyLabel;
    private JComboBox<NavigationStrategy> strategyComboBox;
    private JPanel dateFilterPanel;
    private JLabel dateLabel;
    private JSpinner dateFromSpinner;
    private JLabel toLabel;
    private JSpinner dateToSpinner;
    private JPanel buttonFilterPanel;
    private JButton filterButton;
    private JButton resetButton;
    private JButton exportButton;

    // 表格和统计面板
    private JPanel dataPanel;
    private JPanel statsPanel;
    private JLabel totalCountLabel;
    private JLabel totalDistanceLabel;
    private JLabel avgDistanceLabel;
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

    // 操作按钮面板
    private JPanel actionPanel;
    private JButton viewDetailButton;
    private JButton deleteButton;
    private JButton deleteAllButton;
    private JButton closeButton;

    public NavigationHistoryDialog(JFrame parent, boolean showUserFilter) {
        super(parent, "导航历史记录", true);
        initComponents(showUserFilter);
        initLayout(showUserFilter);
        initStyle();
        initWindow();
    }

    private void initComponents(boolean showUserFilter) {
        // 主面板
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ====== 搜索面板 ======
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchLabel = new JLabel("搜索：");
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchButton = createStyledButton("搜索", new Color(0, 123, 255));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.setBackground(new Color(248, 249, 250));

        // ====== 过滤面板 ======
        filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("筛选条件"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        filterPanel.setBackground(new Color(248, 249, 250));

        // 用户筛选（仅管理员）
        if (showUserFilter) {
            userFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            userLabel = new JLabel("选择用户：");
            userComboBox = new JComboBox<>();
            userComboBox.setPreferredSize(new Dimension(150, 30));
            userFilterPanel.add(userLabel);
            userFilterPanel.add(userComboBox);
            userFilterPanel.setBackground(new Color(248, 249, 250));
            filterPanel.add(userFilterPanel);
            filterPanel.add(Box.createVerticalStrut(5));
        }

        // 策略筛选
        strategyFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        strategyLabel = new JLabel("导航策略：");
        strategyComboBox = new JComboBox<>(NavigationStrategy.values());
        strategyComboBox.setPreferredSize(new Dimension(120, 30));
        strategyComboBox.insertItemAt(null, 0);
        strategyComboBox.setSelectedIndex(0);
        strategyFilterPanel.add(strategyLabel);
        strategyFilterPanel.add(strategyComboBox);
        strategyFilterPanel.setBackground(new Color(248, 249, 250));
        filterPanel.add(strategyFilterPanel);
        filterPanel.add(Box.createVerticalStrut(5));

        // 日期筛选
        dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        dateLabel = new JLabel("日期范围：");
        dateFromSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(dateFromSpinner, "yyyy-MM-dd");
        dateFromSpinner.setEditor(fromEditor);
        dateFromSpinner.setPreferredSize(new Dimension(110, 30));

        toLabel = new JLabel("至");

        dateToSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(dateToSpinner, "yyyy-MM-dd");
        dateToSpinner.setEditor(toEditor);
        dateToSpinner.setPreferredSize(new Dimension(110, 30));

        dateFilterPanel.add(dateLabel);
        dateFilterPanel.add(dateFromSpinner);
        dateFilterPanel.add(toLabel);
        dateFilterPanel.add(dateToSpinner);
        dateFilterPanel.setBackground(new Color(248, 249, 250));
        filterPanel.add(dateFilterPanel);
        filterPanel.add(Box.createVerticalStrut(10));

        // 筛选按钮
        buttonFilterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        filterButton = createStyledButton("筛选", new Color(0, 123, 255));
        resetButton = createStyledButton("重置", new Color(108, 117, 125));
        exportButton = createStyledButton("导出数据", new Color(40, 167, 69));

        buttonFilterPanel.add(filterButton);
        buttonFilterPanel.add(resetButton);
        buttonFilterPanel.add(exportButton);
        buttonFilterPanel.setBackground(new Color(248, 249, 250));
        filterPanel.add(buttonFilterPanel);

        // ====== 表格和统计面板 ======
        dataPanel = new JPanel(new BorderLayout(0, 10));

        // 统计面板
        statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statsPanel.setBackground(new Color(233, 236, 239));

        totalCountLabel = createStatLabel("总记录数：0", new Color(0, 123, 255));
        totalDistanceLabel = createStatLabel("总距离：0 米", new Color(40, 167, 69));
        avgDistanceLabel = createStatLabel("平均距离：0 米", new Color(255, 193, 7));

        statsPanel.add(totalCountLabel);
        statsPanel.add(totalDistanceLabel);
        statsPanel.add(avgDistanceLabel);

        // 表格
        historyTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 不可编辑
            }
        };

        // 设置表格属性
        historyTable.setRowHeight(32);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setShowVerticalLines(true);
        historyTable.setShowHorizontalLines(true);
        historyTable.setGridColor(new Color(222, 226, 230));
        historyTable.setIntercellSpacing(new Dimension(0, 0));

        scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("导航历史记录"));

        // 分页面板
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        paginationPanel.setBackground(new Color(248, 249, 250));

        prevButton = createStyledButton("◀ 上一页", new Color(108, 117, 125));
        nextButton = createStyledButton("下一页 ▶", new Color(108, 117, 125));
        pageLabel = new JLabel("第 1/1 页");
        pageLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        totalLabel = new JLabel("共 0 条记录");
        totalLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));

        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(totalLabel);
        paginationPanel.add(nextButton);

        // 组装数据面板
        dataPanel.add(statsPanel, BorderLayout.NORTH);
        dataPanel.add(scrollPane, BorderLayout.CENTER);
        dataPanel.add(paginationPanel, BorderLayout.SOUTH);

        // ====== 详情面板 ======
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("导航详情"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        detailPanel.setPreferredSize(new Dimension(350, 0));

        detailTextArea = new JTextArea();
        detailTextArea.setEditable(false);
        detailTextArea.setLineWrap(true);
        detailTextArea.setWrapStyleWord(true);
        detailTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        detailTextArea.setBackground(new Color(248, 249, 250));
        detailTextArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        detailScrollPane = new JScrollPane(detailTextArea);
        detailScrollPane.setBorder(null);
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);

        // ====== 操作按钮面板 ======
        actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        viewDetailButton = createStyledButton("查看详情", new Color(0, 123, 255));
        deleteButton = createStyledButton("删除记录", new Color(220, 53, 69));
        deleteAllButton = createStyledButton("清空历史", new Color(220, 53, 69));
        closeButton = createStyledButton("关闭窗口", new Color(108, 117, 125));

        actionPanel.add(viewDetailButton);
        actionPanel.add(deleteButton);
        actionPanel.add(deleteAllButton);
        actionPanel.add(closeButton);
    }

    private void initLayout(boolean showUserFilter) {
        // 创建内容面板
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部：搜索面板
        contentPanel.add(searchPanel, BorderLayout.NORTH);

        // 中部：过滤和表格详情区域
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));

        // 过滤面板
        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // 表格和详情面板
        JPanel tableDetailPanel = new JPanel(new BorderLayout(15, 0));
        tableDetailPanel.add(dataPanel, BorderLayout.CENTER);
        tableDetailPanel.add(detailPanel, BorderLayout.EAST);

        centerPanel.add(tableDetailPanel, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // 底部：操作按钮
        contentPanel.add(actionPanel, BorderLayout.SOUTH);

        // 设置主面板
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void initStyle() {
        // 设置表格字体和渲染
        Font headerFont = new Font("Microsoft YaHei", Font.BOLD, 13);
        Font tableFont = new Font("Microsoft YaHei", Font.PLAIN, 12);

        historyTable.getTableHeader().setFont(headerFont);
        historyTable.getTableHeader().setBackground(new Color(52, 58, 64));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.getTableHeader().setReorderingAllowed(false);

        historyTable.setFont(tableFont);
        historyTable.setSelectionBackground(new Color(220, 240, 255));
        historyTable.setSelectionForeground(Color.BLACK);

        // 设置交替行颜色
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(248, 249, 250));
                    }
                }

                // 居中对齐
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        // 设置文本区域字体
        Font detailFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        detailTextArea.setFont(detailFont);

        // 禁用初始按钮
        viewDetailButton.setEnabled(false);
        deleteButton.setEnabled(false);

        // 设置对话框背景
        mainPanel.setBackground(Color.WHITE);
    }

    private void initWindow() {
        setSize(1000, 750);
        setLocationRelativeTo(getParent());
        setResizable(true);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(6, 15, 6, 15)
        ));
        button.setBackground(color);
        button.setForeground(Color.WHITE);

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

        return button;
    }

    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        label.setForeground(color);
        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return label;
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

    // 新增搜索相关Getter方法
    public JTextField getSearchField() { return searchField; }
    public JButton getSearchButton() { return searchButton; }

    /**
     * 获取搜索关键词
     */
    public String getSearchKeyword() {
        String keyword = searchField.getText();
        return (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
    }

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
        // 清空搜索框
        searchField.setText("");

        if (userComboBox != null) {
            userComboBox.setSelectedIndex(0);
        }
        strategyComboBox.setSelectedIndex(0);

        // 重置为默认日期（最近30天）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        dateFromSpinner.setValue(java.sql.Timestamp.valueOf(thirtyDaysAgo));
        dateToSpinner.setValue(java.sql.Timestamp.valueOf(now));

        // 清空详情区域
        detailTextArea.setText("");
    }

    /**
     * 更新统计信息
     */
    public void updateStatistics(int totalCount, double totalDistance, double avgDistance) {
        totalCountLabel.setText(String.format("总记录数：%d", totalCount));
        totalDistanceLabel.setText(String.format("总距离：%.1f 米", totalDistance));
        avgDistanceLabel.setText(String.format("平均距离：%.1f 米", avgDistance));
    }
}