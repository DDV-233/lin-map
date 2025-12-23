package com.campus.nav.service;

import com.campus.nav.model.*;

import java.util.List;

/**
 * 导航Service接口
 */
public interface NavigationService {
    
    /**
     * 计算导航路径
     */
    NavigationResult navigate(Integer startLocationId, Integer endLocationId, 
                            NavigationStrategy strategy, User user);
    
    /**
     * 保存导航历史
     */
    boolean saveNavigationHistory(NavigationHistory history);
    
    /**
     * 获取用户的导航历史
     */
    List<NavigationHistory> getUserNavigationHistory(Integer userId);
    
    /**
     * 分页获取用户的导航历史
     */
    PageResult<NavigationHistory> getUserNavigationHistoryPage(Integer userId, PageQuery query);
    
    /**
     * 清除用户的导航历史
     */
    boolean clearUserNavigationHistory(Integer userId);
    
    /**
     * 获取推荐路径（基于历史记录）
     */
    List<Location> getRecommendedPath(Integer userId, Integer startLocationId, Integer endLocationId);
    
    /**
     * 查找附近的地点
     */
    List<Location> findNearbyLocations(Integer locationId, double radius);
}