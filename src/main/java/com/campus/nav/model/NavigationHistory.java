package com.campus.nav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导航历史实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationHistory {
    /**
     * 导航记录ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 用户（关联对象）
     */
    private User user;
    
    /**
     * 起点地点ID
     */
    private Integer startLocationId;
    
    /**
     * 起点地点（关联对象）
     */
    private Location startLocation;
    
    /**
     * 终点地点ID
     */
    private Integer endLocationId;
    
    /**
     * 终点地点（关联对象）
     */
    private Location endLocation;
    
    /**
     * 导航策略
     */
    private NavigationStrategy pathStrategy;
    
    /**
     * 总距离（米）
     */
    private Double totalDistance;
    
    /**
     * 总时间（分钟）
     */
    private Integer totalTime;
    
    /**
     * 路径详情（地点ID列表）
     */
    private List<Integer> pathLocations;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}