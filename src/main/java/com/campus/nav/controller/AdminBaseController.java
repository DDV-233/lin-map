package com.campus.nav.controller;

import com.campus.nav.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * 管理员基础控制器
 */
public abstract class AdminBaseController extends BaseController {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final User currentUser;
    protected JFrame parentFrame;
    
    public AdminBaseController(User currentUser, JFrame parentFrame) {
        this.currentUser = currentUser;
        this.parentFrame = parentFrame;
    }
    
    /**
     * 检查管理员权限
     */
    protected boolean checkAdminPermission() {
        if (currentUser.getUserType() != User.UserType.ADMIN) {
            showErrorDialog("需要管理员权限才能执行此操作");
            return false;
        }
        return true;
    }
    
    /**
     * 显示管理对话框
     */
    protected abstract void showManagementDialog();
    
    /**
     * 加载数据
     */
    protected abstract void loadData();
    
    /**
     * 保存数据
     */
    protected abstract boolean saveData();
    
    /**
     * 验证输入
     */
    protected abstract boolean validateInput();
}