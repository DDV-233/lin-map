package com.campus.nav.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * 基础Controller类
 */
public abstract class BaseController {
    protected final Logger logger = LogManager.getLogger(getClass());
    
    /**
     * 显示错误信息对话框
     */
    protected void showErrorDialog(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(null, 
                message, "错误", JOptionPane.ERROR_MESSAGE));
        logger.error("操作错误: {}", message);
    }
    
    /**
     * 显示成功信息对话框
     */
    protected void showSuccessDialog(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(null, 
                message, "成功", JOptionPane.INFORMATION_MESSAGE));
        logger.info("操作成功: {}", message);
    }
    
    /**
     * 显示确认对话框
     */
    protected boolean showConfirmDialog(String message) {
        int result = JOptionPane.showConfirmDialog(null, 
            message, "确认", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * 显示警告对话框
     */
    protected void showWarningDialog(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(null, 
                message, "警告", JOptionPane.WARNING_MESSAGE));
        logger.warn("操作警告: {}", message);
    }
}