package com.campus.nav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导航结果包装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationResult {
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 导航策略
     */
    private NavigationStrategy strategy;
    
    /**
     * 总距离（米）
     */
    private double totalDistance;
    
    /**
     * 总时间（分钟）
     */
    private int totalTime;
    
    /**
     * 路径上的地点列表（按顺序）
     */
    private List<Location> pathLocations;
    
    /**
     * 路径详情（路径列表）
     */
    private List<Path> paths;
    
    /**
     * 是否有绿荫覆盖
     */
    private boolean hasShadeCoverage;
    
    /**
     * 平均景色等级
     */
    private double averageScenicLevel;
    
    /**
     * 创建成功结果
     */
    public static NavigationResult success(NavigationStrategy strategy, 
                                         double totalDistance, 
                                         int totalTime,
                                         List<Location> pathLocations,
                                         List<Path> paths) {
        NavigationResult result = NavigationResult.builder()
                .success(true)
                .strategy(strategy)
                .totalDistance(totalDistance)
                .totalTime(totalTime)
                .pathLocations(pathLocations)
                .paths(paths)
                .build();
        
        // 计算附加信息
        if (paths != null && !paths.isEmpty()) {
            long shadeCount = paths.stream()
                    .filter(path -> Boolean.TRUE.equals(path.getHasShade()))
                    .count();
            result.setHasShadeCoverage(shadeCount > paths.size() / 2);
            
            double scenicSum = paths.stream()
                    .mapToInt(path -> path.getScenicLevel() != null ? path.getScenicLevel() : 1)
                    .sum();
            result.setAverageScenicLevel(scenicSum / paths.size());
        }
        
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static NavigationResult fail(String errorMessage) {
        return NavigationResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}