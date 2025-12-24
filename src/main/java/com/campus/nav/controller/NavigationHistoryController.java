package com.campus.nav.controller;

import com.campus.nav.dao.DaoFactory;
import com.campus.nav.dao.NavigationHistoryDao;
import com.campus.nav.dao.UserDao;
import com.campus.nav.model.*;
import com.campus.nav.service.NavigationService;
import com.campus.nav.service.ServiceFactory;
import com.campus.nav.view.NavigationHistoryDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 导航历史控制器 - 完整修复版（适配搜索功能）
 */
public class NavigationHistoryController extends AdminBaseController {
    private static final Logger logger = LogManager.getLogger(NavigationHistoryController.class);

    private final NavigationHistoryDialog dialog;
    private final NavigationHistoryDao historyDao;
    private final NavigationService navigationService;
    private final UserDao userDao;
    private final boolean showUserFilter;

    private List<NavigationHistory> currentHistories;
    private NavigationHistory selectedHistory;
    private int currentPage = 1;
    private final int pageSize = 20;
    private String lastSearchKeyword = null;

    public NavigationHistoryController(User currentUser, JFrame parentFrame, boolean showUserFilter) {
        super(currentUser, parentFrame);
        this.showUserFilter = showUserFilter;
        this.dialog = new NavigationHistoryDialog(parentFrame, showUserFilter);
        this.historyDao = DaoFactory.getNavigationHistoryDao();
        this.navigationService = ServiceFactory.getNavigationService();
        this.userDao = DaoFactory.getUserDao();

        initListeners();
        initData();
        loadData();
        updateUI();
        dialog.setVisible(true);
    }

    protected void initListeners() {
        // 搜索相关事件
        dialog.getSearchButton().addActionListener(this::handleSearch);
        dialog.getSearchField().addActionListener(this::handleSearch);

        // 过滤按钮事件
        dialog.getFilterButton().addActionListener(this::handleFilter);
        dialog.getResetButton().addActionListener(this::handleReset);
        dialog.getExportButton().addActionListener(this::handleExport);

        // 分页按钮事件
        dialog.getPrevButton().addActionListener(e -> handlePageChange(-1));
        dialog.getNextButton().addActionListener(e -> handlePageChange(1));

        // 操作按钮事件
        dialog.getViewDetailButton().addActionListener(this::handleViewDetail);
        dialog.getDeleteButton().addActionListener(this::handleDelete);
        dialog.getDeleteAllButton().addActionListener(this::handleDeleteAll);
        dialog.getCloseButton().addActionListener(this::handleClose);

        // 表格选择事件
        dialog.getHistoryTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });

        // 过滤条件变化事件
        if (showUserFilter && dialog.getUserComboBox() != null) {
            dialog.getUserComboBox().addActionListener(e -> handleFilterChange());
        }
        dialog.getStrategyComboBox().addActionListener(e -> handleFilterChange());
        dialog.getDateFromSpinner().addChangeListener(e -> handleFilterChange());
        dialog.getDateToSpinner().addChangeListener(e -> handleFilterChange());
    }

    /**
     * 处理搜索
     */
    private void handleSearch(ActionEvent e) {
        String keyword = dialog.getSearchKeyword();
        if (keyword != null && !keyword.equals(lastSearchKeyword)) {
            currentPage = 1;
            lastSearchKeyword = keyword;
        }
        loadData();
    }

    /**
     * 处理过滤条件变化
     */
    private void handleFilterChange() {
        // 当过滤条件变化时，重置到第一页
        if (currentPage != 1) {
            currentPage = 1;
            loadData();
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 如果是管理员查看所有历史，加载用户列表
        if (showUserFilter) {
            try {
                List<User> users = userDao.findAll();
                DefaultComboBoxModel<User> model = new DefaultComboBoxModel<>();
                model.addElement(null); // 添加空选项
                for (User user : users) {
                    model.addElement(user);
                }
                dialog.getUserComboBox().setModel(model);
                dialog.getUserComboBox().setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                  int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value == null) {
                            setText("所有用户");
                        } else if (value instanceof User) {
                            User user = (User) value;
                            setText(String.format("%s", user.getUsername()));
                        }
                        return this;
                    }
                });
            } catch (Exception e) {
                logger.error("加载用户列表失败", e);
                showErrorDialog("加载用户列表失败: " + e.getMessage());
            }
        }

        // 设置默认日期范围（最近30天）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        dialog.getDateFromSpinner().setValue(
                java.sql.Timestamp.valueOf(thirtyDaysAgo));
        dialog.getDateToSpinner().setValue(
                java.sql.Timestamp.valueOf(now));
    }

    @Override
    protected void loadData() {
        try {
            // 获取搜索关键词
            String searchKeyword = dialog.getSearchKeyword();

            // 构建分页查询
            PageQuery query = PageQuery.builder()
                    .pageNum(currentPage)
                    .pageSize(pageSize)
                    .keyword(searchKeyword) // 设置搜索关键词
                    .build();

            PageResult<NavigationHistory> pageResult;

            if (showUserFilter) {
                // 管理员查看所有用户的历史 - 使用支持搜索的findByPage方法
                pageResult = historyDao.findByPage(query);
                currentHistories = pageResult.getData();

                // 应用其他过滤条件（用户、策略、日期）
                applyFilters();

                // 重新计算分页信息
                long filteredTotal = currentHistories.size();
                int totalPages = (int) Math.ceil((double) filteredTotal / pageSize);
                pageResult = PageResult.of(currentHistories, filteredTotal, query);

            } else {
                // 普通用户查看自己的历史
                // 注意：需要检查getUserNavigationHistoryPage是否支持搜索
                // 如果不支持，可能需要分页后在内存中过滤
                pageResult = navigationService.getUserNavigationHistoryPage(
                        currentUser.getId(), query);

                currentHistories = pageResult.getData();

                // 应用其他过滤条件（策略、日期）
                applyFilters();

                // 应用搜索过滤（如果service不支持搜索）
                if (searchKeyword != null && !searchKeyword.isEmpty()) {
                    applySearchFilter(searchKeyword);
                }

                // 重新计算分页信息
                long filteredTotal = currentHistories.size();
                int totalPages = (int) Math.ceil((double) filteredTotal / pageSize);
                pageResult = PageResult.of(currentHistories, filteredTotal, query);
            }

            // 更新表格
            updateTable();

            // 更新分页信息
            updatePagination(pageResult);

            // 更新统计信息
            updateStatistics();

            logger.info("加载导航历史数据，第 {} 页，共 {} 条记录，搜索词: {}",
                    currentPage, pageResult.getTotal(), searchKeyword);

        } catch (Exception e) {
            logger.error("加载导航历史数据失败", e);
            showErrorDialog("加载导航历史数据失败: " + e.getMessage());
        }
    }

    /**
     * 应用搜索过滤（用于普通用户模式）
     */
    private void applySearchFilter(String keyword) {
        if (currentHistories == null || keyword == null || keyword.isEmpty()) {
            return;
        }

        String lowerKeyword = keyword.toLowerCase();
        List<NavigationHistory> filteredHistories = new java.util.ArrayList<>();

        for (NavigationHistory history : currentHistories) {
            if (matchesSearchKeyword(history, lowerKeyword)) {
                filteredHistories.add(history);
            }
        }

        currentHistories = filteredHistories;
    }

    /**
     * 检查导航历史是否匹配搜索关键词
     */
    private boolean matchesSearchKeyword(NavigationHistory history, String keyword) {
        // 检查用户名
        if (history.getUser() != null && history.getUser().getUsername() != null &&
                history.getUser().getUsername().toLowerCase().contains(keyword)) {
            return true;
        }

        // 检查起点名称
        if (history.getStartLocation() != null && history.getStartLocation().getName() != null &&
                history.getStartLocation().getName().toLowerCase().contains(keyword)) {
            return true;
        }

        // 检查终点名称
        if (history.getEndLocation() != null && history.getEndLocation().getName() != null &&
                history.getEndLocation().getName().toLowerCase().contains(keyword)) {
            return true;
        }

        // 检查策略名称
        if (history.getPathStrategy() != null && history.getPathStrategy().getDisplayName() != null &&
                history.getPathStrategy().getDisplayName().toLowerCase().contains(keyword)) {
            return true;
        }

        return false;
    }

    /**
     * 应用其他过滤条件
     */
    private void applyFilters() {
        if (currentHistories == null) return;

        List<NavigationHistory> filteredHistories = new java.util.ArrayList<>(currentHistories);

        // 策略过滤
        NavigationStrategy strategyFilter = dialog.getSelectedStrategy();
        if (strategyFilter != null) {
            filteredHistories.removeIf(history ->
                    history.getPathStrategy() == null ||
                            !history.getPathStrategy().equals(strategyFilter));
        }

        // 用户过滤（仅管理员）
        if (showUserFilter) {
            Integer userId = dialog.getSelectedUserId();
            if (userId != null) {
                filteredHistories.removeIf(history ->
                        history.getUserId() == null ||
                                !history.getUserId().equals(userId));
            }
        }

        // 日期过滤
        LocalDateTime dateFrom = dialog.getDateFrom();
        LocalDateTime dateTo = dialog.getDateTo();
        if (dateFrom != null && dateTo != null) {
            filteredHistories.removeIf(history ->
                    history.getCreatedAt() == null ||
                            history.getCreatedAt().isBefore(dateFrom) ||
                            history.getCreatedAt().isAfter(dateTo));
        }

        currentHistories = filteredHistories;
    }

    @Override
    protected boolean saveData() {
        return true;
    }

    @Override
    protected boolean validateInput() {
        // 验证日期范围
        LocalDateTime dateFrom = dialog.getDateFrom();
        LocalDateTime dateTo = dialog.getDateTo();

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            showErrorDialog("开始日期不能晚于结束日期");
            return false;
        }
        return true;
    }

    @Override
    public void showManagementDialog() {
        dialog.setVisible(true);
    }

    /**
     * 处理过滤
     */
    private void handleFilter(ActionEvent e) {
        if (!validateInput()) {
            return;
        }

        currentPage = 1;
        loadData();
    }

    /**
     * 处理重置
     */
    private void handleReset(ActionEvent e) {
        dialog.resetFilters();
        currentPage = 1;
        lastSearchKeyword = null;
        loadData();
    }

    /**
     * 处理导出
     */
    private void handleExport(ActionEvent e) {
        if (currentHistories == null || currentHistories.isEmpty()) {
            showWarningDialog("没有数据可以导出");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出导航历史");
        fileChooser.setSelectedFile(new java.io.File("导航历史记录_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv"));

        if (fileChooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try {
                exportToCSV(file);
                showSuccessDialog("导出成功！文件已保存到：" + file.getAbsolutePath());
            } catch (Exception ex) {
                logger.error("导出失败", ex);
                showErrorDialog("导出失败: " + ex.getMessage());
            }
        }
    }

    /**
     * 导出到CSV
     */
    private void exportToCSV(java.io.File file) throws java.io.IOException {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
            // 写入表头
            if (showUserFilter) {
                writer.println("用户名,起点,终点,策略,距离(米),时间(分钟),导航时间");
            } else {
                writer.println("起点,终点,策略,距离(米),时间(分钟),导航时间");
            }

            // 写入数据
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (NavigationHistory history : currentHistories) {
                if (showUserFilter) {
                    writer.printf("%s,%s,%s,%s,%.1f,%d,%s%n",
                            history.getUser() != null ? history.getUser().getUsername() : "",
                            history.getStartLocation() != null ? history.getStartLocation().getName() : "",
                            history.getEndLocation() != null ? history.getEndLocation().getName() : "",
                            history.getPathStrategy() != null ? history.getPathStrategy().getDisplayName() : "",
                            history.getTotalDistance() != null ? history.getTotalDistance() : 0.0,
                            history.getTotalTime() != null ? history.getTotalTime() : 0,
                            history.getCreatedAt() != null ? history.getCreatedAt().format(formatter) : ""
                    );
                } else {
                    writer.printf("%s,%s,%s,%.1f,%d,%s%n",
                            history.getStartLocation() != null ? history.getStartLocation().getName() : "",
                            history.getEndLocation() != null ? history.getEndLocation().getName() : "",
                            history.getPathStrategy() != null ? history.getPathStrategy().getDisplayName() : "",
                            history.getTotalDistance() != null ? history.getTotalDistance() : 0.0,
                            history.getTotalTime() != null ? history.getTotalTime() : 0,
                            history.getCreatedAt() != null ? history.getCreatedAt().format(formatter) : ""
                    );
                }
            }
        }
    }

    /**
     * 处理分页
     */
    private void handlePageChange(int delta) {
        int newPage = currentPage + delta;
        if (newPage < 1) {
            showWarningDialog("已经是第一页了");
            return;
        }

        currentPage = newPage;
        loadData();
    }

    /**
     * 处理查看详情
     */
    private void handleViewDetail(ActionEvent e) {
        if (selectedHistory == null) {
            showErrorDialog("请先选择一条记录");
            return;
        }

        StringBuilder detail = new StringBuilder();
        detail.append("════════════════════════════════════════════════════════════════\n");
        detail.append("                      导航记录详情                               \n");
        detail.append("════════════════════════════════════════════════════════════════\n\n");

        if (showUserFilter || selectedHistory.getUser() != null) {
            detail.append("用  户：      ").append(selectedHistory.getUser().getUsername()).append("\n");
            detail.append("────────────────────────────────────────────────────────────────\n");
        }

        if (selectedHistory.getStartLocation() != null) {
            detail.append("起  点：      ").append(selectedHistory.getStartLocation().getName()).append("\n");
        } else {
            detail.append("起  点：      [数据缺失]").append("\n");
        }

        if (selectedHistory.getEndLocation() != null) {
            detail.append("终  点：      ").append(selectedHistory.getEndLocation().getName()).append("\n");
        } else {
            detail.append("终  点：      [数据缺失]").append("\n");
        }

        detail.append("────────────────────────────────────────────────────────────────\n");
        detail.append("策  略：      ").append(selectedHistory.getPathStrategy().getDisplayName()).append("\n");

        if (selectedHistory.getTotalDistance() != null) {
            detail.append("距  离：      ").append(String.format("%.1f 米", selectedHistory.getTotalDistance())).append("\n");
        } else {
            detail.append("距  离：      [数据缺失]").append("\n");
        }

        if (selectedHistory.getTotalTime() != null) {
            detail.append("时  间：      ").append(selectedHistory.getTotalTime()).append(" 分钟\n");
        } else {
            detail.append("时  间：      [数据缺失]").append("\n");
        }

        // 显示路径详情
        if (selectedHistory.getPathLocations() != null && !selectedHistory.getPathLocations().isEmpty()) {
            detail.append("────────────────────────────────────────────────────────────────\n");
            detail.append("途经地点：    ").append(selectedHistory.getPathLocations().size()).append(" 个\n");
        }

        detail.append("────────────────────────────────────────────────────────────────\n");

        if (selectedHistory.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒");
            detail.append("导航时间：    ").append(selectedHistory.getCreatedAt().format(formatter)).append("\n");
        } else {
            detail.append("导航时间：    [数据缺失]").append("\n");
        }
        detail.append("════════════════════════════════════════════════════════════════");

        dialog.getDetailTextArea().setText(detail.toString());
        dialog.getDetailTextArea().setCaretPosition(0);
    }

    /**
     * 处理删除
     */
    private void handleDelete(ActionEvent e) {
        if (selectedHistory == null) {
            showErrorDialog("请先选择一条记录");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "<html><b>确定要删除这条导航记录吗？</b><br>" +
                        "记录ID: " + selectedHistory.getId() + "<br>" +
                        (selectedHistory.getUser() != null ? "用户: " + selectedHistory.getUser().getUsername() + "<br>" : "") +
                        (selectedHistory.getStartLocation() != null ? "起点: " + selectedHistory.getStartLocation().getName() + "<br>" : "") +
                        (selectedHistory.getEndLocation() != null ? "终点: " + selectedHistory.getEndLocation().getName() + "<br>" : "") +
                        "此操作无法撤销。</html>",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (historyDao.deleteById(selectedHistory.getId())) {
                    showSuccessDialog("记录删除成功");
                    clearSelection();
                    loadData();
                } else {
                    showErrorDialog("记录删除失败");
                }
            } catch (Exception ex) {
                logger.error("删除记录失败", ex);
                showErrorDialog("删除记录失败: " + ex.getMessage());
            }
        }
    }

    /**
     * 处理删除所有
     */
    private void handleDeleteAll(ActionEvent e) {
        if (currentHistories == null || currentHistories.isEmpty()) {
            showWarningDialog("当前没有可删除的记录");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "<html><b>确定要清空导航历史记录吗？</b><br>" +
                        "将删除 " + currentHistories.size() + " 条记录<br>" +
                        (showUserFilter ? "" : "(仅清空您自己的历史记录)<br>") +
                        "此操作无法撤销！</html>",
                "确认清空",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int deletedCount = 0;
                if (showUserFilter) {
                    // 管理员删除所有记录
                    List<NavigationHistory> allHistories = historyDao.findAll();
                    for (NavigationHistory history : allHistories) {
                        if (historyDao.deleteById(history.getId())) {
                            deletedCount++;
                        }
                    }
                } else {
                    // 普通用户删除自己的记录
                    List<NavigationHistory> histories = historyDao.findByUserId(currentUser.getId());
                    for (NavigationHistory history : histories) {
                        if (historyDao.deleteById(history.getId())) {
                            deletedCount++;
                        }
                    }

                }

                showSuccessDialog("成功清空 " + deletedCount + " 条历史记录");
                clearSelection();
                loadData();

            } catch (Exception ex) {
                logger.error("清空历史记录失败", ex);
                showErrorDialog("清空历史记录失败: " + ex.getMessage());
            }
        }
    }

    /**
     * 处理关闭
     */
    private void handleClose(ActionEvent e) {
        dialog.dispose();
    }

    /**
     * 处理表格选择
     */
    private void handleTableSelection() {
        int selectedRow = dialog.getHistoryTable().getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentHistories.size()) {
            selectedHistory = currentHistories.get(selectedRow);
            dialog.getViewDetailButton().setEnabled(true);
            dialog.getDeleteButton().setEnabled(true);
        } else {
            clearSelection();
        }
    }

    /**
     * 更新表格
     */
    private void updateTable() {
        String[] columns = showUserFilter ?
                new String[]{ "用户名", "起点", "终点", "策略", "距离(米)", "时间(分钟)", "导航时间"} :
                new String[]{"起点", "终点", "策略", "距离(米)", "时间(分钟)", "导航时间"};

        Object[][] data = new Object[currentHistories.size()][columns.length];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (int i = 0; i < currentHistories.size(); i++) {
            NavigationHistory history = currentHistories.get(i);
            int col = 0;


            if (showUserFilter) {
                // 用户名
                if (history.getUser() != null) {
                    data[i][col++] = history.getUser().getUsername();
                } else {
                    data[i][col++] = "[用户不存在]";
                    data[i][col++] = "-";
                }
            }

            // 起点
            if (history.getStartLocation() != null) {
                data[i][col++] = history.getStartLocation().getName();
            } else {
                data[i][col++] = "[起点缺失]";
            }

            // 终点
            if (history.getEndLocation() != null) {
                data[i][col++] = history.getEndLocation().getName();
            } else {
                data[i][col++] = "[终点缺失]";
            }

            // 策略
            if (history.getPathStrategy() != null) {
                data[i][col++] = history.getPathStrategy().getDisplayName();
            } else {
                data[i][col++] = "[策略缺失]";
            }

            // 距离
            if (history.getTotalDistance() != null) {
                data[i][col++] = String.format("%.1f", history.getTotalDistance());
            } else {
                data[i][col++] = "0.0";
            }

            // 时间
            if (history.getTotalTime() != null) {
                data[i][col++] = history.getTotalTime();
            } else {
                data[i][col++] = 0;
            }

            // 导航时间
            if (history.getCreatedAt() != null) {
                data[i][col] = history.getCreatedAt().format(formatter);
            } else {
                data[i][col] = "[时间缺失]";
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // ID列
                if (showUserFilter && (columnIndex == 1 || columnIndex == 2)) return String.class; // 用户名列
                if (!showUserFilter && columnIndex >= 4 && columnIndex <= 5) return Double.class; // 距离列
                return String.class; // 其他列
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        dialog.getHistoryTable().setModel(model);
    }

    /**
     * 更新分页信息
     */
    private void updatePagination(PageResult<NavigationHistory> pageResult) {
        dialog.getPageLabel().setText(
                String.format("第 %d/%d 页",
                        currentPage,
                        pageResult.getTotalPages())
        );

        // 显示搜索信息
        String searchInfo = "";
        String searchKeyword = dialog.getSearchKeyword();
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            searchInfo = " (搜索: \"" + searchKeyword + "\")";
        }

        dialog.getTotalLabel().setText(
                String.format("共 %d 条记录%s", currentHistories.size(), searchInfo)
        );

        dialog.getPrevButton().setEnabled(currentPage > 1);
        dialog.getNextButton().setEnabled(currentPage < pageResult.getTotalPages());
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        if (currentHistories == null || currentHistories.isEmpty()) {
            dialog.updateStatistics(0, 0, 0);
            return;
        }

        int totalCount = currentHistories.size();
        double totalDistance = 0;
        int totalTime = 0;

        for (NavigationHistory history : currentHistories) {
            if (history.getTotalDistance() != null) {
                totalDistance += history.getTotalDistance();
            }
            if (history.getTotalTime() != null) {
                totalTime += history.getTotalTime();
            }
        }

        double avgDistance = totalCount > 0 ? totalDistance / totalCount : 0;

        dialog.updateStatistics(totalCount, totalDistance, avgDistance);
    }

    /**
     * 清空选择
     */
    private void clearSelection() {
        selectedHistory = null;
        dialog.getHistoryTable().clearSelection();
        dialog.getDetailTextArea().setText("");
        dialog.getViewDetailButton().setEnabled(false);
        dialog.getDeleteButton().setEnabled(false);
    }

    /**
     * 更新UI
     */
    private void updateUI() {
        // 设置对话框标题
        String title = showUserFilter ? "所有导航历史" : "我的导航历史";
        dialog.setTitle(title + " - " + currentUser.getUsername());

        // 如果是普通用户，调整按钮文字
        if (!showUserFilter) {
            dialog.getDeleteAllButton().setText("清空我的历史");
            dialog.getDeleteAllButton().setToolTipText("仅清空您自己的历史记录");
        } else {
            dialog.getDeleteAllButton().setToolTipText("清空所有用户的历史记录");
        }

        // 设置搜索框提示
        dialog.getSearchField().setToolTipText("搜索用户名、起点、终点或策略");
    }
}