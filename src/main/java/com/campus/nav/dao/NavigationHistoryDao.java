package com.campus.nav.dao;

import com.campus.nav.model.NavigationHistory;

import java.util.List;

/**
 * 导航历史DAO接口
 */
public interface NavigationHistoryDao extends BaseDao<NavigationHistory, Integer> {
    
    /**
     * 根据用户ID查询导航历史
     */
    List<NavigationHistory> findByUserId(Integer userId);
    
    /**
     * 根据用户ID分页查询导航历史
     */
    com.campus.nav.model.PageResult<NavigationHistory> findByUserIdPage(Integer userId, 
                                                                       com.campus.nav.model.PageQuery query);
    
    /**
     * 删除用户的导航历史
     */
    boolean deleteByUserId(Integer userId);
    
    /**
     * 统计用户的导航次数
     */
    long countByUserId(Integer userId);
}