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
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 导航历史控制器
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
        // 按钮事件
        dialog.getFilterButton().addActionListener(this::handleFilter);
        dialog.getResetButton().addActionListener(this::handleReset);
        dialog.getExportButton().addActionListener(this::handleExport);
        dialog.getPrevButton().addActionListener(e -> handlePageChange(-1));
        dialog.getNextButton().addActionListener(e -> handlePageChange(1));
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
        
        // 日期过滤变化
        dialog.getDateFromSpinner().addChangeListener(e -> handleDateFilterChange());
        dialog.getDateToSpinner().addChangeListener(e -> handleDateFilterChange());
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
            PageQuery query = PageQuery.builder()
                    .pageNum(currentPage)
                    .pageSize(pageSize)
                    .build();
            
            PageResult<NavigationHistory> pageResult;
            
            if (showUserFilter) {
                // 管理员查看所有用户的历史
                pageResult = historyDao.findByPage(query);
            } else {
                // 普通用户查看自己的历史
                pageResult = navigationService.getUserNavigationHistoryPage(
                        currentUser.getId(), query);
            }
            
            currentHistories = pageResult.getData();
            
            // 应用过滤条件
            applyFilters();
            
            // 更新表格
            updateTable();
            
            // 更新分页信息
            updatePagination(pageResult);
            
            // 更新统计信息
            updateStatistics();
            
            logger.info("加载导航历史数据，第 {} 页，共 {} 条记录", 
                    currentPage, pageResult.getTotal());
            
        } catch (Exception e) {
            logger.error("加载导航历史数据失败", e);
            showErrorDialog("加载导航历史数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 应用过滤条件
     */
    private void applyFilters() {
        if (currentHistories == null) return;
        
        // 策略过滤
        NavigationStrategy strategyFilter = dialog.getSelectedStrategy();
        if (strategyFilter != null) {
            currentHistories.removeIf(history -> 
                    !history.getPathStrategy().equals(strategyFilter));
        }
        
        // 用户过滤（仅管理员）
        if (showUserFilter) {
            Integer userId = dialog.getSelectedUserId();
            if (userId != null) {
                currentHistories.removeIf(history -> 
                        !history.getUserId().equals(userId));
            }
        }
        
        // 日期过滤
        LocalDateTime dateFrom = dialog.getDateFrom();
        LocalDateTime dateTo = dialog.getDateTo();
        if (dateFrom != null && dateTo != null) {
            currentHistories.removeIf(history -> 
                    history.getCreatedAt() == null ||
                    history.getCreatedAt().isBefore(dateFrom) ||
                    history.getCreatedAt().isAfter(dateTo));
        }
    }
    
    @Override
    protected boolean saveData() {
        // 导航历史不需要保存操作
        return true;
    }
    
    @Override
    protected boolean validateInput() {
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
        currentPage = 1;
        loadData();
    }
    
    /**
     * 处理重置
     */
    private void handleReset(ActionEvent e) {
        dialog.resetFilters();
        currentPage = 1;
        loadData();
    }
    
    /**
     * 处理导出
     */
    private void handleExport(ActionEvent e) {
        // TODO: 实现导出功能
        showWarningDialog("导出功能正在开发中...");
    }
    
    /**
     * 处理分页
     */
    private void handlePageChange(int delta) {
        currentPage += delta;
        if (currentPage < 1) {
            currentPage = 1;
        }
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
        detail.append("导航记录详情\n");
        detail.append("====================\n");
        detail.append("记录ID: ").append(selectedHistory.getId()).append("\n");
        detail.append("用 户: ").append(selectedHistory.getUser().getUsername()).append("\n");
        detail.append("起 点: ").append(selectedHistory.getStartLocation().getName()).append("\n");
        detail.append("终 点: ").append(selectedHistory.getEndLocation().getName()).append("\n");
        detail.append("策 略: ").append(selectedHistory.getPathStrategy().getDisplayName()).append("\n");
        detail.append("距 离: ").append(String.format("%.1f", selectedHistory.getTotalDistance())).append(" 米\n");
        detail.append("时 间: ").append(selectedHistory.getTotalTime()).append(" 分钟\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        detail.append("时 间: ").append(selectedHistory.getCreatedAt().format(formatter)).append("\n");
        
        dialog.getDetailTextArea().setText(detail.toString());
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
            "确定要删除这条导航记录吗？\n此操作无法撤销。",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (historyDao.deleteById(selectedHistory.getId())) {
                showSuccessDialog("记录删除成功");
                clearSelection();
                loadData();
            } else {
                showErrorDialog("记录删除失败");
            }
        }
    }
    
    /**
     * 处理删除所有
     */
    private void handleDeleteAll(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
            dialog,
            "确定要清空所有导航历史记录吗？\n此操作无法撤销。",
            "确认清空",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (showUserFilter) {
                    // 管理员删除所有记录
                    List<NavigationHistory> allHistories = historyDao.findAll();
                    for (NavigationHistory history : allHistories) {
                        historyDao.deleteById(history.getId());
                    }
                } else {
                    // 普通用户删除自己的记录
                    navigationService.clearUserNavigationHistory(currentUser.getId());
                }
                
                showSuccessDialog("历史记录已清空");
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
        }
    }
    
    /**
     * 处理日期过滤变化
     */
    private void handleDateFilterChange() {
        // 可以添加实时过滤，这里暂时不实现
        // loadData();
    }
    
    /**
     * 更新表格
     */
    private void updateTable() {
        String[] columns = showUserFilter ? 
                new String[]{"ID", "用户", "起点", "终点", "策略", "距离(米)", "时间(分钟)", "导航时间"} :
                new String[]{"ID", "起点", "终点", "策略", "距离(米)", "时间(分钟)", "导航时间"};
        
        Object[][] data = new Object[currentHistories.size()][columns.length];
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        
        for (int i = 0; i < currentHistories.size(); i++) {
            NavigationHistory history = currentHistories.get(i);
            int col = 0;
            
            data[i][col++] = history.getId();
            
            if (showUserFilter) {
                data[i][col++] = history.getUser().getUsername();
            }
            
            data[i][col++] = history.getStartLocation().getName();
            data[i][col++] = history.getEndLocation().getName();
            data[i][col++] = history.getPathStrategy().getDisplayName();
            data[i][col++] = String.format("%.1f", history.getTotalDistance());
            data[i][col++] = history.getTotalTime();
            data[i][col] = history.getCreatedAt().format(formatter);
        }
        
        dialog.getHistoryTable().setModel(new DefaultTableModel(data, columns));
    }
    
    /**
     * 更新分页信息
     */
    private void updatePagination(PageResult<NavigationHistory> pageResult) {
        dialog.getPageLabel().setText(
                String.format("第 %d/%d 页",
                        pageResult.getPageNum(),
                        pageResult.getTotalPages())
        );
        
        dialog.getTotalLabel().setText(
                String.format("共 %d 条记录", pageResult.getTotal())
        );
        
        dialog.getPrevButton().setEnabled(pageResult.getHasPrevious());
        dialog.getNextButton().setEnabled(pageResult.getHasNext());
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
        
        for (NavigationHistory history : currentHistories) {
            totalDistance += history.getTotalDistance();
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
        
        // 如果是普通用户，隐藏用户过滤
        if (!showUserFilter) {
            dialog.getDeleteAllButton().setText("清空我的历史");
        }
    }
}