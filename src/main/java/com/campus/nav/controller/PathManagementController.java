package com.campus.nav.controller;

import com.campus.nav.dao.DaoFactory;
import com.campus.nav.dao.LocationDao;
import com.campus.nav.model.*;
import com.campus.nav.service.ServiceFactory;
import com.campus.nav.service.PathService;
import com.campus.nav.view.PathManagementDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * 路径管理控制器
 */
public class PathManagementController extends AdminBaseController {
    private static final Logger logger = LogManager.getLogger(PathManagementController.class);
    
    private final PathManagementDialog dialog;
    private final PathService pathService;
    private final LocationDao locationDao;
    private List<Path> currentPaths;
    private Path selectedPath;
    private int currentPage = 1;
    
    public PathManagementController(User currentUser, JFrame parentFrame) {
        super(currentUser, parentFrame);
        this.dialog = new PathManagementDialog(parentFrame);
        this.pathService = ServiceFactory.getPathService();
        this.locationDao = DaoFactory.getLocationDao();
        
        initListeners();
        loadLocations();
        loadData();
        updateUI();
        dialog.setVisible(true);
    }
    

    protected void initListeners() {
        // 按钮事件
        dialog.getAddButton().addActionListener(this::handleAdd);
        dialog.getEditButton().addActionListener(this::handleEdit);
        dialog.getDeleteButton().addActionListener(this::handleDelete);
        dialog.getSaveButton().addActionListener(this::handleSave);
        dialog.getCancelButton().addActionListener(this::handleCancel);
        dialog.getCloseButton().addActionListener(this::handleClose);
        dialog.getToggleStatusButton().addActionListener(this::handleToggleStatus);
        dialog.getSearchButton().addActionListener(this::handleSearch);
        
        // 表格选择事件
        dialog.getPathTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });
        
        // 分页事件
        dialog.getPrevButton().addActionListener(e -> handlePageChange(-1));
        dialog.getNextButton().addActionListener(e -> handlePageChange(1));
        
        // 仅显示可用路径过滤
        dialog.getActiveOnlyCheckBox().addActionListener(e -> handleActiveOnlyFilter());
    }
    
    /**
     * 加载地点数据到下拉框
     */
    private void loadLocations() {
        try {
            List<Location> locations = locationDao.findAll();
            
            DefaultComboBoxModel<Location> model = new DefaultComboBoxModel<>();
            for (Location location : locations) {
                model.addElement(location);
            }
            
            dialog.getStartLocationComboBox().setModel(model);
            dialog.getEndLocationComboBox().setModel(new DefaultComboBoxModel<Location>(
                    locations.toArray(new Location[0])
            ){});
            
        } catch (Exception e) {
            logger.error("加载地点数据失败", e);
            showErrorDialog("加载地点数据失败: " + e.getMessage());
        }
    }
    
    @Override
    protected void loadData() {
        try {
            PageQuery query = PageQuery.builder()
                    .pageNum(currentPage)
                    .pageSize(15)
                    .keyword(dialog.getSearchKeyword())
                    .build();
            
            PageResult<Path> pageResult = pathService.findByPage(query);
            currentPaths = pageResult.getData();
            
            // 应用可用路径过滤
            if (dialog.isActiveOnly()) {
                currentPaths.removeIf(path -> !Boolean.TRUE.equals(path.getIsActive()));
            }
            
            // 更新表格
            updateTable();
            
            // 更新分页信息
            updatePagination(pageResult);
            
            logger.info("加载路径数据，第 {} 页，共 {} 条记录", 
                    currentPage, pageResult.getTotal());
            
        } catch (Exception e) {
            logger.error("加载路径数据失败", e);
            showErrorDialog("加载路径数据失败: " + e.getMessage());
        }
    }
    
    @Override
    protected boolean saveData() {
        try {
            // 获取表单数据
            Location startLocation = (Location) dialog.getStartLocationComboBox().getSelectedItem();
            Location endLocation = (Location) dialog.getEndLocationComboBox().getSelectedItem();
            String distanceText = dialog.getDistanceField().getText().trim();
            String timeCostText = dialog.getTimeCostField().getText().trim();
            boolean hasShade = dialog.getHasShadeCheckBox().isSelected();
            int scenicLevel = (int) dialog.getScenicLevelSpinner().getValue();
            boolean isIndoor = dialog.getIsIndoorCheckBox().isSelected();
            boolean isActive = dialog.getIsActiveCheckBox().isSelected();
            
            // 验证输入
            if (startLocation == null || endLocation == null) {
                showErrorDialog("请选择起点和终点");
                return false;
            }
            
            if (startLocation.getId().equals(endLocation.getId())) {
                showErrorDialog("起点和终点不能相同");
                return false;
            }
            
            double distance;
            try {
                distance = Double.parseDouble(distanceText);
                if (distance <= 0) {
                    showErrorDialog("距离必须大于0");
                    return false;
                }
            } catch (NumberFormatException e) {
                showErrorDialog("距离必须是有效数字");
                return false;
            }
            
            Integer timeCost = null;
            if (!timeCostText.isEmpty()) {
                try {
                    timeCost = Integer.parseInt(timeCostText);
                    if (timeCost <= 0) {
                        showErrorDialog("时间必须大于0");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("时间必须是有效整数");
                    return false;
                }
            }
            
            // 创建或更新路径
            Path path;
            if (selectedPath != null && selectedPath.getId() != null) {
                // 更新现有路径
                path = selectedPath;
                path.setDistance(distance);
                path.setTimeCost(timeCost);
                path.setHasShade(hasShade);
                path.setScenicLevel(scenicLevel);
                path.setIsIndoor(isIndoor);
                path.setIsActive(isActive);
                
                if (pathService.update(path)) {
                    showSuccessDialog("路径更新成功");
                    clearForm();
                    loadData();
                    return true;
                } else {
                    showErrorDialog("路径更新失败");
                    return false;
                }
            } else {
                // 新增路径
                path = Path.builder()
                        .startLocationId(startLocation.getId())
                        .endLocationId(endLocation.getId())
                        .distance(distance)
                        .timeCost(timeCost)
                        .hasShade(hasShade)
                        .scenicLevel(scenicLevel)
                        .isIndoor(isIndoor)
                        .isActive(isActive)
                        .build();
                
                // 同时创建反向路径
                boolean createBidirectional = JOptionPane.showConfirmDialog(
                    dialog,
                    "是否同时创建反向路径？",
                    "创建双向路径",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION;
                
                if (createBidirectional) {
                    if (pathService.createBidirectionalPath(
                            startLocation.getId(), endLocation.getId(),
                            distance, timeCost, hasShade, scenicLevel)) {
                        showSuccessDialog("双向路径创建成功");
                        clearForm();
                        loadData();
                        return true;
                    } else {
                        showErrorDialog("双向路径创建失败");
                        return false;
                    }
                } else {
                    if (pathService.save(path)) {
                        showSuccessDialog("路径添加成功");
                        clearForm();
                        loadData();
                        return true;
                    } else {
                        showErrorDialog("路径添加失败");
                        return false;
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("保存路径数据失败", e);
            showErrorDialog("保存路径数据失败: " + e.getMessage());
            return false;
        }
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
     * 处理添加
     */
    private void handleAdd(ActionEvent e) {
        clearForm();
        dialog.setFormEditable(true);
        dialog.getSaveButton().setEnabled(true);
        dialog.getCancelButton().setEnabled(true);
        dialog.getToggleStatusButton().setEnabled(false);
    }
    
    /**
     * 处理编辑
     */
    private void handleEdit(ActionEvent e) {
        if (selectedPath == null) {
            showErrorDialog("请先选择一条路径");
            return;
        }
        
        // 填充表单
        fillForm(selectedPath);
        dialog.setFormEditable(true);
        dialog.getSaveButton().setEnabled(true);
        dialog.getCancelButton().setEnabled(true);
        dialog.getToggleStatusButton().setEnabled(true);
    }
    
    /**
     * 处理删除
     */
    private void handleDelete(ActionEvent e) {
        if (selectedPath == null) {
            showErrorDialog("请先选择一条路径");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            dialog,
            "确定要删除路径 '" + 
            selectedPath.getStartLocation().getName() + " -> " + 
            selectedPath.getEndLocation().getName() + "' 吗？\n此操作无法撤销。",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (pathService.deleteById(selectedPath.getStartLocationId())){
                showSuccessDialog("路径删除成功");
                clearForm();
                loadData();
            } else {
                showErrorDialog("路径删除失败");
            }
        }
    }
    
    /**
     * 处理保存
     */
    private void handleSave(ActionEvent e) {
        if (saveData()) {
            dialog.setFormEditable(false);
            dialog.getSaveButton().setEnabled(false);
            dialog.getCancelButton().setEnabled(false);
            dialog.getToggleStatusButton().setEnabled(false);
        }
    }
    
    /**
     * 处理取消
     */
    private void handleCancel(ActionEvent e) {
        clearForm();
        dialog.setFormEditable(false);
        dialog.getSaveButton().setEnabled(false);
        dialog.getCancelButton().setEnabled(false);
        dialog.getToggleStatusButton().setEnabled(false);
    }
    
    /**
     * 处理关闭
     */
    private void handleClose(ActionEvent e) {
        dialog.dispose();
    }
    
    /**
     * 处理切换状态
     */
    private void handleToggleStatus(ActionEvent e) {
        if (selectedPath == null) {
            showErrorDialog("请先选择一条路径");
            return;
        }
        
        boolean newStatus = !selectedPath.getIsActive();
        String statusText = newStatus ? "启用" : "禁用";
        
        int confirm = JOptionPane.showConfirmDialog(
            dialog,
            "确定要" + statusText + "这条路径吗？",
            "确认" + statusText,
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (pathService.updateStatus(selectedPath.getId(), newStatus)) {
                showSuccessDialog("路径" + statusText + "成功");
                loadData();
                // 重新选中并填充表单
                if (selectedPath != null) {
                    selectedPath.setIsActive(newStatus);
                    fillForm(selectedPath);
                }
            } else {
                showErrorDialog("路径状态更新失败");
            }
        }
    }
    
    /**
     * 处理表格选择
     */
    private void handleTableSelection() {
        int selectedRow = dialog.getPathTable().getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentPaths.size()) {
            selectedPath = currentPaths.get(selectedRow);
            fillForm(selectedPath);
            dialog.setFormEditable(false);
            dialog.getToggleStatusButton().setEnabled(true);
        }
    }
    
    /**
     * 处理搜索
     */
    private void handleSearch(ActionEvent e) {
        currentPage = 1;
        loadData();
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
     * 处理可用路径过滤
     */
    private void handleActiveOnlyFilter() {
        currentPage = 1;
        loadData();
    }
    
    /**
     * 更新表格
     */
    private void updateTable() {
        String[] columns = {"ID", "起点", "终点", "距离(米)", "时间(分钟)", "绿荫", "景色", "室内", "状态"};
        Object[][] data = new Object[currentPaths.size()][columns.length];
        
        for (int i = 0; i < currentPaths.size(); i++) {
            Path path = currentPaths.get(i);
            data[i][0] = path.getId();
            data[i][1] = path.getStartLocation() != null ? path.getStartLocation().getName() : "未知";
            data[i][2] = path.getEndLocation() != null ? path.getEndLocation().getName() : "未知";
            data[i][3] = String.format("%.1f", path.getDistance());
            data[i][4] = path.getTimeCost() != null ? path.getTimeCost() : "";
            data[i][5] = Boolean.TRUE.equals(path.getHasShade()) ? "有" : "无";
            data[i][6] = path.getScenicLevel();
            data[i][7] = Boolean.TRUE.equals(path.getIsIndoor()) ? "是" : "否";
            data[i][8] = Boolean.TRUE.equals(path.getIsActive()) ? "可用" : "禁用";
        }
        
        dialog.getPathTable().setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
    
    /**
     * 更新分页信息
     */
    private void updatePagination(PageResult<Path> pageResult) {
        dialog.getPageLabel().setText(
                String.format("第 %d/%d 页，共 %d 条记录",
                        pageResult.getPageNum(),
                        pageResult.getTotalPages(),
                        pageResult.getTotal())
        );
        
        dialog.getPrevButton().setEnabled(pageResult.getHasPrevious());
        dialog.getNextButton().setEnabled(pageResult.getHasNext());
    }
    
    /**
     * 填充表单
     */
    private void fillForm(Path path) {
        // 设置起点和终点
        for (int i = 0; i < dialog.getStartLocationComboBox().getItemCount(); i++) {
            Location loc = dialog.getStartLocationComboBox().getItemAt(i);
            if (loc != null && loc.getId().equals(path.getStartLocationId())) {
                dialog.getStartLocationComboBox().setSelectedIndex(i);
                break;
            }
        }
        
        for (int i = 0; i < dialog.getEndLocationComboBox().getItemCount(); i++) {
            Location loc = dialog.getEndLocationComboBox().getItemAt(i);
            if (loc != null && loc.getId().equals(path.getEndLocationId())) {
                dialog.getEndLocationComboBox().setSelectedIndex(i);
                break;
            }
        }
        
        dialog.getDistanceField().setText(String.valueOf(path.getDistance()));
        dialog.getTimeCostField().setText(path.getTimeCost() != null ? 
                String.valueOf(path.getTimeCost()) : "");
        dialog.getHasShadeCheckBox().setSelected(Boolean.TRUE.equals(path.getHasShade()));
        dialog.getScenicLevelSpinner().setValue(path.getScenicLevel());
        dialog.getIsIndoorCheckBox().setSelected(Boolean.TRUE.equals(path.getIsIndoor()));
        dialog.getIsActiveCheckBox().setSelected(Boolean.TRUE.equals(path.getIsActive()));
    }
    
    /**
     * 清空表单
     */
    private void clearForm() {
        selectedPath = null;
        dialog.clearForm();
        dialog.getPathTable().clearSelection();
        dialog.getToggleStatusButton().setEnabled(false);
    }
    
    /**
     * 更新UI
     */
    private void updateUI() {
        // 设置对话框标题
        dialog.setTitle("路径管理 - " + currentUser.getUsername());
        
        // 初始禁用保存和取消按钮
        dialog.getSaveButton().setEnabled(false);
        dialog.getCancelButton().setEnabled(false);
        dialog.getToggleStatusButton().setEnabled(false);
    }
}