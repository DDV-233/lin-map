package com.campus.nav.model;

/**
 * 导航策略枚举
 */
public enum NavigationStrategy {
    SHORTEST("最短路径", "优先选择距离最短的路径"),
    SHADIEST("绿荫最多", "优先选择有绿荫覆盖的路径"),
    MOST_SCENIC("景色最美", "优先选择景色优美的路径");
    
    private final String displayName;
    private final String description;
    
    NavigationStrategy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 从字符串获取枚举值
     */
    public static NavigationStrategy fromString(String strategy) {
        for (NavigationStrategy navStrategy : NavigationStrategy.values()) {
            if (navStrategy.name().equalsIgnoreCase(strategy) || 
                navStrategy.displayName.equals(strategy)) {
                return navStrategy;
            }
        }
        return SHORTEST; // 默认最短路径
    }
    
    /**
     * 获取所有策略的显示名称
     */
    public static String[] getAllDisplayNames() {
        NavigationStrategy[] strategies = values();
        String[] displayNames = new String[strategies.length];
        for (int i = 0; i < strategies.length; i++) {
            displayNames[i] = strategies[i].getDisplayName();
        }
        return displayNames;
    }
}