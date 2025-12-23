package com.campus.nav;

import com.campus.nav.config.DatabaseConfig;
import com.campus.nav.controller.LoginController;
import com.campus.nav.view.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // 设置Swing外观
        SwingUtilities.invokeLater(() -> {
            try {
                // 使用FlatLaf现代化外观
                FlatLightLaf.setup();
                UIManager.setLookAndFeel(new FlatLightLaf());
                
                // 初始化数据库连接
                DatabaseConfig.initialize();
                
                logger.info("校园导航系统启动...");
                
                // 显示登录界面
                LoginFrame loginFrame = new LoginFrame();
                LoginController loginController = new LoginController(loginFrame);
                loginFrame.setController(loginController);
                loginFrame.setVisible(true);
                
            } catch (Exception e) {
                logger.error("系统启动失败", e);
                JOptionPane.showMessageDialog(null,
                        "系统启动失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}