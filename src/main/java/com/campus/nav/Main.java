package com.campus.nav;

import com.campus.nav.config.DatabaseConfig;
import com.campus.nav.controller.LoginController;
import com.campus.nav.view.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // 设置Swing外观
        SwingUtilities.invokeLater(() -> {
            try {
                // 使用FlatLaf现代化外观
                FlatLightLaf.setup();
                UIManager.setLookAndFeel(new FlatLightLaf());

                // 设置UI默认字体
                setUIFont();

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

    /**
     * 设置UI默认字体
     */
    private static void setUIFont() {
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 12);

        // 设置所有UI组件的字体
        UIManager.put("Button.font", font);
        UIManager.put("ToggleButton.font", font);
        UIManager.put("RadioButton.font", font);
        UIManager.put("CheckBox.font", font);
        UIManager.put("ColorChooser.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("List.font", font);
        UIManager.put("MenuBar.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("RadioButtonMenuItem.font", font);
        UIManager.put("CheckBoxMenuItem.font", font);
        UIManager.put("Menu.font", font);
        UIManager.put("PopupMenu.font", font);
        UIManager.put("OptionPane.font", font);
        UIManager.put("Panel.font", font);
        UIManager.put("ProgressBar.font", font);
        UIManager.put("ScrollPane.font", font);
        UIManager.put("Viewport.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("Table.font", font);
        UIManager.put("TableHeader.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("PasswordField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("TextPane.font", font);
        UIManager.put("EditorPane.font", font);
        UIManager.put("TitledBorder.font", font);
        UIManager.put("ToolBar.font", font);
        UIManager.put("ToolTip.font", font);
        UIManager.put("Tree.font", font);

        // 设置UI颜色
        UIManager.put("Panel.background", new Color(248, 249, 250));
        UIManager.put("OptionPane.background", new Color(248, 249, 250));
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextArea.background", Color.WHITE);
    }
}