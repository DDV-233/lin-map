package com.campus.nav.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置管理类
 */
public class SystemConfig {
    private static final Map<String, String> configMap = new HashMap<>();
    
    static {
        // 加载系统配置
        loadDefaultConfig();
    }
    
    private SystemConfig() {
    }
    
    /**
     * 加载默认配置
     */
    private static void loadDefaultConfig() {
        configMap.put("map.width", DatabaseConfig.getProperty("system.map.width", "800"));
        configMap.put("map.height", DatabaseConfig.getProperty("system.map.height", "600"));
        configMap.put("ui.theme", DatabaseConfig.getProperty("system.ui.theme", "FlatLaf Light"));
        configMap.put("path.weight.shortest", DatabaseConfig.getProperty("path.weight.shortest", "1.0"));
        configMap.put("path.weight.shade", DatabaseConfig.getProperty("path.weight.shade", "1.5"));
        configMap.put("path.weight.scenic", DatabaseConfig.getProperty("path.weight.scenic", "1.3"));
    }
    
    /**
     * 获取配置值
     */
    public static String getConfig(String key) {
        return configMap.get(key);
    }
    
    /**
     * 获取配置值（整数类型）
     */
    public static int getIntConfig(String key) {
        try {
            return Integer.parseInt(configMap.get(key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 获取配置值（浮点数类型）
     */
    public static double getDoubleConfig(String key) {
        try {
            return Double.parseDouble(configMap.get(key));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * 设置配置值
     */
    public static void setConfig(String key, String value) {
        configMap.put(key, value);
    }
    
    /**
     * 获取地图宽度
     */
    public static int getMapWidth() {
        return getIntConfig("map.width");
    }
    
    /**
     * 获取地图高度
     */
    public static int getMapHeight() {
        return getIntConfig("map.height");
    }
    
    /**
     * 获取UI主题
     */
    public static String getUITheme() {
        return getConfig("ui.theme");
    }
    
    /**
     * 获取路径权重配置
     */
    public static Map<String, Double> getPathWeights() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("shortest", getDoubleConfig("path.weight.shortest"));
        weights.put("shade", getDoubleConfig("path.weight.shade"));
        weights.put("scenic", getDoubleConfig("path.weight.scenic"));
        return weights;
    }
}