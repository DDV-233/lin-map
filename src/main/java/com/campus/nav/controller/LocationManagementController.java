package com.campus.nav.controller;

import com.campus.nav.model.Location;
import com.campus.nav.model.PageQuery;
import com.campus.nav.model.PageResult;
import com.campus.nav.model.User;
import com.campus.nav.service.ServiceFactory;
import com.campus.nav.service.LocationService;
import com.campus.nav.view.LocationManagementDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * 地点管理控制器
 */
public class LocationManagementController extends AdminBaseController {
    private static final Logger logger = LogManager.getLogger(LocationManagementController.class);
    
    private final LocationManagementDialog dialog;
    private final LocationService locationService;
    private List<Location> currentLocations;
    private Location selectedLocation;
    private int currentPage = 1;
    
    public LocationManagementController(User currentUser, JFrame parentFrame) {
        super(currentUser, parentFrame);
        this.dialog = new LocationManagementDialog(parentFrame);
        this.locationService = ServiceFactory.getLocationService();
        
        initListeners();

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
        
        // 表格选择事件
        dialog.getLocationTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });
        
        // 搜索事件
        dialog.getSearchButton().addActionListener(this::handleSearch);
        dialog.getSearchField().addActionListener(this::handleSearch);
        
        // 分页事件
        dialog.getPrevButton().addActionListener(e -> handlePageChange(-1));
        dialog.getNextButton().addActionListener(e -> handlePageChange(1));
        
        // 类型过滤
        dialog.getTypeFilterComboBox().addActionListener(this::handleTypeFilter);
    }
    
    @Override
    protected void loadData() {
        try {
            PageQuery query = PageQuery.builder()
                    .pageNum(currentPage)
                    .pageSize(10)
                    .keyword(dialog.getSearchKeyword())
                    .build();
            
            PageResult<Location> pageResult = locationService.findByPage(query);
            currentLocations = pageResult.getData();
            
            // 更新表格
            updateTable();
            
            // 更新分页信息
            updatePagination(pageResult);
            
            logger.info("加载地点数据，第 {} 页，共 {} 条记录", 
                    currentPage, pageResult.getTotal());
            
        } catch (Exception e) {
            logger.error("加载地点数据失败", e);
            showErrorDialog("加载地点数据失败: " + e.getMessage());
        }
    }
    
    @Override
    protected boolean saveData() {
        try {
            // 获取表单数据
            String name = dialog.getNameField().getText().trim();
            String description = dialog.getDescriptionArea().getText().trim();
            Location.LocationType type = (Location.LocationType) dialog.getTypeComboBox().getSelectedItem();
            Double x = parseDouble(dialog.getXField().getText());
            Double y = parseDouble(dialog.getYField().getText());
            Boolean hasShade = dialog.getHasShadeCheckBox().isSelected();
            Integer scenicLevel = parseInt(dialog.getScenicLevelField().getText());
            Boolean isAccessible = dialog.getIsAccessibleCheckBox().isSelected();
            
            // 验证输入
            if (name.isEmpty()) {
                showErrorDialog("地点名称不能为空");
                return false;
            }
            
            if (x == null || y == null) {
                showErrorDialog("坐标必须为有效数字");
                return false;
            }
            
            if (scenicLevel == null || scenicLevel < 1 || scenicLevel > 5) {
                showErrorDialog("景色等级必须是1-5的整数");
                return false;
            }
            
            // 创建或更新地点
            Location location = Location.builder()
                    .name(name)
                    .description(description)
                    .type(type != null ? type : Location.LocationType.OTHER)
                    .xCoordinate(x)
                    .yCoordinate(y)
                    .hasShade(hasShade != null ? hasShade : false)
                    .scenicLevel(scenicLevel != null ? scenicLevel : 1)
                    .isAccessible(isAccessible != null ? isAccessible : true)
                    .build();
            
            if (selectedLocation != null && selectedLocation.getId() != null) {
                // 更新现有地点
                location.setId(selectedLocation.getId());
                if (locationService.update(location)) {
                    showSuccessDialog("地点更新成功");
                    clearForm();
                    loadData();
                    return true;
                } else {
                    showErrorDialog("地点更新失败");
                    return false;
                }
            } else {
                // 新增地点
                if (locationService.save(location)) {
                    showSuccessDialog("地点添加成功");
                    clearForm();
                    loadData();
                    return true;
                } else {
                    showErrorDialog("地点添加失败");
                    return false;
                }
            }
            
        } catch (Exception e) {
            logger.error("保存地点数据失败", e);
            showErrorDialog("保存地点数据失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    protected boolean validateInput() {
        // 验证逻辑在saveData中实现
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
    }
    
    /**
     * 处理编辑
     */
    private void handleEdit(ActionEvent e) {
        if (selectedLocation == null) {
            showErrorDialog("请先选择一个地点");
            return;
        }
        
        // 填充表单
        fillForm(selectedLocation);
        dialog.setFormEditable(true);
        dialog.getSaveButton().setEnabled(true);
        dialog.getCancelButton().setEnabled(true);
    }
    
    /**
     * 处理删除
     */
    private void handleDelete(ActionEvent e) {
        if (selectedLocation == null) {
            showErrorDialog("请先选择一个地点");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            dialog,
            "确定要删除地点 '" + selectedLocation.getName() + "' 吗？\n此操作无法撤销。",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (locationService.deleteById(selectedLocation.getId())) {
                showSuccessDialog("地点删除成功");
                clearForm();
                loadData();
            } else {
                showErrorDialog("地点删除失败");
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
        int selectedRow = dialog.getLocationTable().getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentLocations.size()) {
            selectedLocation = currentLocations.get(selectedRow);
            fillForm(selectedLocation);
            dialog.setFormEditable(false);
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
     * 处理类型过滤
     */
    private void handleTypeFilter(ActionEvent e) {
        // TODO: 实现按类型过滤
        showWarningDialog("类型过滤功能正在开发中...");
    }
    
    /**
     * 更新表格
     */
    private void updateTable() {
        String[] columns = {"ID", "名称", "类型", "坐标", "绿荫", "景色", "可通行"};
        Object[][] data = new Object[currentLocations.size()][columns.length];
        
        for (int i = 0; i < currentLocations.size(); i++) {
            Location loc = currentLocations.get(i);
            data[i][0] = loc.getId();
            data[i][1] = loc.getName();
            data[i][2] = loc.getType().getDescription();
            data[i][3] = String.format("(%.0f, %.0f)", loc.getXCoordinate(), loc.getYCoordinate());
            data[i][4] = loc.getHasShade() ? "有" : "无";
            data[i][5] = loc.getScenicLevel();
            data[i][6] = loc.getIsAccessible() ? "是" : "否";
        }
        
        dialog.getLocationTable().setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
    
    /**
     * 更新分页信息
     */
    private void updatePagination(PageResult<Location> pageResult) {
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
    private void fillForm(Location location) {
        dialog.getNameField().setText(location.getName());
        dialog.getDescriptionArea().setText(
                location.getDescription() != null ? location.getDescription() : "");
        dialog.getTypeComboBox().setSelectedItem(location.getType());
        dialog.getXField().setText(String.valueOf(location.getXCoordinate()));
        dialog.getYField().setText(String.valueOf(location.getYCoordinate()));
        dialog.getHasShadeCheckBox().setSelected(Boolean.TRUE.equals(location.getHasShade()));
        dialog.getScenicLevelField().setText(String.valueOf(location.getScenicLevel()));
        dialog.getIsAccessibleCheckBox().setSelected(Boolean.TRUE.equals(location.getIsAccessible()));
    }
    
    /**
     * 清空表单
     */
    private void clearForm() {
        selectedLocation = null;
        dialog.getNameField().setText("");
        dialog.getDescriptionArea().setText("");
        dialog.getTypeComboBox().setSelectedIndex(0);
        dialog.getXField().setText("");
        dialog.getYField().setText("");
        dialog.getHasShadeCheckBox().setSelected(false);
        dialog.getScenicLevelField().setText("1");
        dialog.getIsAccessibleCheckBox().setSelected(true);
        dialog.getLocationTable().clearSelection();
    }
    
    /**
     * 更新UI
     */
    private void updateUI() {
        // 根据权限设置按钮状态
        boolean isAdmin = currentUser.getUserType() == User.UserType.ADMIN;
        dialog.getAddButton().setEnabled(isAdmin);
        dialog.getEditButton().setEnabled(isAdmin);
        dialog.getDeleteButton().setEnabled(isAdmin);
        dialog.getSaveButton().setEnabled(false);
        dialog.getCancelButton().setEnabled(false);
        
        // 设置对话框标题
        dialog.setTitle("地点管理 - " + currentUser.getUsername());
    }
    
    /**
     * 解析整数
     */
    private Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 解析浮点数
     */
    private Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}