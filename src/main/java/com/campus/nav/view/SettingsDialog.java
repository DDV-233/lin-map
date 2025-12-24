package com.campus.nav.view;

import javax.swing.*;
import java.awt.*;

/**
 * 系统设置对话框（占位类）
 */
public class SettingsDialog extends JDialog {
    public SettingsDialog(JFrame parent) {
        super(parent, "系统设置", true);
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("系统设置功能正在开发中...", SwingConstants.CENTER);
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        
        panel.add(label, BorderLayout.CENTER);
        add(panel, BorderLayout.CENTER);
        
        // 确定按钮
        JButton okButton = new JButton("确定");
        okButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}