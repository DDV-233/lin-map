package com.campus.nav.controller;

import com.campus.nav.model.AuthInfo;
import com.campus.nav.model.User;
import com.campus.nav.service.ServiceFactory;
import com.campus.nav.service.UserService;
import com.campus.nav.view.LoginFrame;
import com.campus.nav.view.MainFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * 登录控制器
 */
public class LoginController extends BaseController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    
    private final LoginFrame loginFrame;
    private final UserService userService;
    private User currentUser;
    
    public LoginController(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        this.userService = ServiceFactory.getUserService();
        initListeners();
    }
    
    /**
     * 初始化事件监听器
     */
    private void initListeners() {
        // 登录按钮事件
        loginFrame.getLoginButton().addActionListener(this::handleLogin);
        
        // 注册按钮事件
        loginFrame.getRegisterButton().addActionListener(this::handleRegister);
        
        // 取消按钮事件
        loginFrame.getCancelButton().addActionListener(e -> handleCancel());
        
        // 回车键事件
        loginFrame.getPasswordField().addActionListener(this::handleLogin);
    }
    
    /**
     * 处理登录
     */
    private void handleLogin(ActionEvent e) {
        String username = loginFrame.getUsernameField().getText().trim();
        String password = new String(loginFrame.getPasswordField().getPassword());
        
        // 输入验证
        if (username.isEmpty() || password.isEmpty()) {
            showErrorDialog("用户名和密码不能为空");
            return;
        }
        
        // 执行登录
        try {
            AuthInfo authInfo = userService.login(username, password);
            
            if (authInfo.isAuthenticated()) {
                this.currentUser = authInfo.getUser();
                logger.info("用户登录成功: {} ({})", username, currentUser.getUserType());
                
                // 登录成功，跳转到主界面
                SwingUtilities.invokeLater(() -> {
                    loginFrame.setVisible(false);
                    loginFrame.dispose();
                    
                    MainFrame mainFrame = new MainFrame(currentUser);
                    MainController mainController = new MainController(mainFrame, currentUser);
                    mainFrame.setController(mainController);
                    mainFrame.setVisible(true);
                });
                
            } else {
                showErrorDialog(authInfo.getErrorMessage());
            }
            
        } catch (Exception ex) {
            logger.error("登录过程出错", ex);
            showErrorDialog("登录过程中出现错误: " + ex.getMessage());
        }
    }
    
    /**
     * 处理注册
     */
    private void handleRegister(ActionEvent e) {
        String username = loginFrame.getUsernameField().getText().trim();
        String password = new String(loginFrame.getPasswordField().getPassword());
        
        // 弹出注册对话框获取邮箱
        String email = JOptionPane.showInputDialog(
            loginFrame,
            "请输入邮箱地址:",
            "用户注册",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (email == null || email.trim().isEmpty()) {
            return; // 用户取消
        }
        
        email = email.trim();
        
        // 输入验证
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showErrorDialog("用户名、密码和邮箱都不能为空");
            return;
        }
        
        // 执行注册
        try {
            AuthInfo authInfo = userService.register(username, password, email);
            
            if (authInfo.isAuthenticated()) {
                this.currentUser = authInfo.getUser();
                logger.info("用户注册成功: {} ({})", username, currentUser.getUserType());
                showSuccessDialog("注册成功！请使用新账户登录。");
                
                // 清空输入框
                loginFrame.getUsernameField().setText("");
                loginFrame.getPasswordField().setText("");
                
            } else {
                showErrorDialog(authInfo.getErrorMessage());
            }
            
        } catch (Exception ex) {
            logger.error("注册过程出错", ex);
            showErrorDialog("注册过程中出现错误: " + ex.getMessage());
        }
    }
    
    /**
     * 处理取消
     */
    private void handleCancel() {
        int confirm = JOptionPane.showConfirmDialog(
            loginFrame,
            "确定要退出系统吗？",
            "确认退出",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            logger.info("用户取消登录，退出系统");
            System.exit(0);
        }
    }
    
    /**
     * 获取当前用户
     */
    public User getCurrentUser() {
        return currentUser;
    }
}