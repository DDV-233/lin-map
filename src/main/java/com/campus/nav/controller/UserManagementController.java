package com.campus.nav.controller;

import com.campus.nav.model.PageQuery;
import com.campus.nav.model.PageResult;
import com.campus.nav.model.User;
import com.campus.nav.service.ServiceFactory;
import com.campus.nav.service.UserService;
import com.campus.nav.view.UserManagementDialog;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户管理控制器
 */
public class UserManagementController extends AdminBaseController {
    private static final Logger logger = LogManager.getLogger(UserManagementController.class);
    
    private final UserManagementDialog dialog;
    private final UserService userService;
    private List<User> currentUsers;
    private User selectedUser;
    private int currentPage = 1;
    
    public UserManagementController(User currentUser, JFrame parentFrame) {
        super(currentUser, parentFrame);
        this.dialog = new UserManagementDialog(parentFrame);
        this.userService = ServiceFactory.getUserService();
        
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
        dialog.getResetPasswordButton().addActionListener(this::handleResetPassword);
        dialog.getSearchButton().addActionListener(this::handleSearch);
        dialog.getResetButton().addActionListener(this::handleResetFilter);
        
        // 表格选择事件
        dialog.getUserTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });
        
        // 分页事件
        dialog.getPrevButton().addActionListener(e -> handlePageChange(-1));
        dialog.getNextButton().addActionListener(e -> handlePageChange(1));
        
        // 用户类型过滤
        dialog.getUserTypeFilterComboBox().addActionListener(this::handleUserTypeFilter);
    }
    
    @Override
    protected void loadData() {
        try {
            PageQuery query = PageQuery.builder()
                    .pageNum(currentPage)
                    .pageSize(15)
                    .keyword(dialog.getSearchKeyword())
                    .build();
            
            PageResult<User> pageResult = userService.findByPage(query);
            currentUsers = pageResult.getData();
            
            // 应用用户类型过滤
            String userTypeFilter = dialog.getUserTypeFilter();
            if (userTypeFilter != null) {
                currentUsers.removeIf(user -> !user.getUserType().name().equals(userTypeFilter));
            }
            
            // 更新表格
            updateTable();
            
            // 更新分页信息
            updatePagination(pageResult);
            
            logger.info("加载用户数据，第 {} 页，共 {} 条记录", 
                    currentPage, pageResult.getTotal());
            
        } catch (Exception e) {
            logger.error("加载用户数据失败", e);
            showErrorDialog("加载用户数据失败: " + e.getMessage());
        }
    }
    
    @Override
    protected boolean saveData() {
        try {
            // 获取表单数据
            String username = dialog.getUsernameField().getText().trim();
            String password = new String(dialog.getPasswordField().getPassword());
            String email = dialog.getEmailField().getText().trim();
            User.UserType userType = (User.UserType) dialog.getUserTypeComboBox().getSelectedItem();
            boolean isActive = dialog.getIsActiveCheckBox().isSelected();
            
            // 验证输入
            if (StringUtils.isBlank(username)) {
                showErrorDialog("用户名不能为空");
                return false;
            }
            
            if (selectedUser == null && StringUtils.isBlank(password)) {
                showErrorDialog("新增用户时密码不能为空");
                return false;
            }
            
            if (StringUtils.isBlank(email)) {
                showErrorDialog("邮箱不能为空");
                return false;
            }
            
            if (!email.contains("@")) {
                showErrorDialog("邮箱格式不正确");
                return false;
            }
            
            // 检查用户名是否已存在（新增时）
            if (selectedUser == null && userService.isUsernameExists(username)) {
                showErrorDialog("用户名已存在");
                return false;
            }
            
            // 检查邮箱是否已存在（新增时）
            if (selectedUser == null && userService.isEmailExists(email)) {
                showErrorDialog("邮箱已被注册");
                return false;
            }
            
            // 创建或更新用户
            User user;
            if (selectedUser != null && selectedUser.getId() != null) {
                // 更新现有用户
                user = selectedUser;
                user.setUsername(username);
                user.setEmail(email);
                user.setUserType(userType != null ? userType : User.UserType.USER);
                user.setIsActive(isActive);
                
                // 如果密码不为空，更新密码
                if (StringUtils.isNotBlank(password)) {
                    user.setPassword(password); // 实际应该加密
                }
                
                if (userService.update(user)) {
                    showSuccessDialog("用户更新成功");
                    clearForm();
                    loadData();
                    return true;
                } else {
                    showErrorDialog("用户更新失败");
                    return false;
                }
            } else {
                // 新增用户
                user = User.builder()
                        .username(username)
                        .password(password) // 实际应该加密
                        .email(email)
                        .userType(userType != null ? userType : User.UserType.USER)
                        .isActive(isActive)
                        .build();
                
                if (userService.save(user)) {
                    showSuccessDialog("用户添加成功");
                    clearForm();
                    loadData();
                    return true;
                } else {
                    showErrorDialog("用户添加失败");
                    return false;
                }
            }
            
        } catch (Exception e) {
            logger.error("保存用户数据失败", e);
            showErrorDialog("保存用户数据失败: " + e.getMessage());
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
        dialog.getResetPasswordButton().setEnabled(false);
    }
    
    /**
     * 处理编辑
     */
    private void handleEdit(ActionEvent e) {
        if (selectedUser == null) {
            showErrorDialog("请先选择一个用户");
            return;
        }
        
        // 不能编辑当前登录用户（防止误操作）
        if (selectedUser.getId().equals(currentUser.getId())) {
            showErrorDialog("不能编辑当前登录的用户");
            return;
        }
        
        // 填充表单
        fillForm(selectedUser);
        dialog.setFormEditable(true);
        dialog.getSaveButton().setEnabled(true);
        dialog.getCancelButton().setEnabled(true);
        dialog.getResetPasswordButton().setEnabled(true);
    }
    
    /**
     * 处理删除
     */
    private void handleDelete(ActionEvent e) {
        if (selectedUser == null) {
            showErrorDialog("请先选择一个用户");
            return;
        }
        
        // 不能删除当前登录用户
        if (selectedUser.getId().equals(currentUser.getId())) {
            showErrorDialog("不能删除当前登录的用户");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            dialog,
            "确定要删除用户 '" + selectedUser.getUsername() + "' 吗？\n此操作无法撤销。",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (userService.deleteById(selectedUser.getId())) {
                showSuccessDialog("用户删除成功");
                clearForm();
                loadData();
            } else {
                showErrorDialog("用户删除失败");
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
            dialog.getResetPasswordButton().setEnabled(false);
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
        dialog.getResetPasswordButton().setEnabled(false);
    }
    
    /**
     * 处理关闭
     */
    private void handleClose(ActionEvent e) {
        dialog.dispose();
    }
    
    /**
     * 处理重置密码
     */
    private void handleResetPassword(ActionEvent e) {
        if (selectedUser == null) {
            showErrorDialog("请先选择一个用户");
            return;
        }
        
        String newPassword = JOptionPane.showInputDialog(
            dialog,
            "请输入新密码（至少6位）:",
            "重置密码",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 6) {
                showErrorDialog("密码长度不能少于6位");
                return;
            }
            
            if (userService.resetPassword(selectedUser.getId(), newPassword)) {
                showSuccessDialog("密码重置成功");
            } else {
                showErrorDialog("密码重置失败");
            }
        }
    }
    
    /**
     * 处理表格选择
     */
    private void handleTableSelection() {
        int selectedRow = dialog.getUserTable().getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentUsers.size()) {
            selectedUser = currentUsers.get(selectedRow);
            fillForm(selectedUser);
            dialog.setFormEditable(false);
            dialog.getResetPasswordButton().setEnabled(true);
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
     * 处理重置过滤
     */
    private void handleResetFilter(ActionEvent e) {
        dialog.getSearchField().setText("");
        dialog.getUserTypeFilterComboBox().setSelectedIndex(0);
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
     * 处理用户类型过滤
     */
    private void handleUserTypeFilter(ActionEvent e) {
        currentPage = 1;
        loadData();
    }
    
    /**
     * 更新表格
     */
    private void updateTable() {
        String[] columns = {"ID", "用户名", "邮箱", "用户类型", "状态", "创建时间"};
        Object[][] data = new Object[currentUsers.size()][columns.length];
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (int i = 0; i < currentUsers.size(); i++) {
            User user = currentUsers.get(i);
            data[i][0] = user.getId();
            data[i][1] = user.getUsername();
            data[i][2] = user.getEmail();
            data[i][3] = user.getUserType().getDescription();
            data[i][4] = Boolean.TRUE.equals(user.getIsActive()) ? "激活" : "禁用";
            data[i][5] = user.getCreatedAt() != null ? 
                    user.getCreatedAt().format(formatter) : "";
        }
        
        dialog.getUserTable().setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
    
    /**
     * 更新分页信息
     */
    private void updatePagination(PageResult<User> pageResult) {
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
    private void fillForm(User user) {
        dialog.getUsernameField().setText(user.getUsername());
        dialog.getPasswordField().setText("");
        dialog.getEmailField().setText(user.getEmail());
        dialog.getUserTypeComboBox().setSelectedItem(user.getUserType());
        dialog.getIsActiveCheckBox().setSelected(Boolean.TRUE.equals(user.getIsActive()));
    }
    
    /**
     * 清空表单
     */
    private void clearForm() {
        selectedUser = null;
        dialog.clearForm();
        dialog.getUserTable().clearSelection();
        dialog.getResetPasswordButton().setEnabled(false);
    }
    
    /**
     * 更新UI
     */
    private void updateUI() {
        // 设置对话框标题
        dialog.setTitle("用户管理 - " + currentUser.getUsername());
        
        // 初始禁用保存和取消按钮
        dialog.getSaveButton().setEnabled(false);
        dialog.getCancelButton().setEnabled(false);
        dialog.getResetPasswordButton().setEnabled(false);
    }
}