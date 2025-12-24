package com.campus.nav;

import com.campus.nav.config.DatabaseConfig;
import com.campus.nav.model.User;
import com.campus.nav.view.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class TestRunner {
    public static void main(String[] args) {
        try {
            // 设置UI外观
            FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // 初始化数据库
            DatabaseConfig.initialize();
            
            // 创建测试用户
            User testUser = User.builder()
                    .id(1)
                    .username("admin")
                    .userType(User.UserType.ADMIN)
                    .build();
            
            // 显示主界面
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(testUser);
                mainFrame.setVisible(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}