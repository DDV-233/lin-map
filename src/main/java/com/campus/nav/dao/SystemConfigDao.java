package com.campus.nav.dao;

import com.campus.nav.model.SystemConfig;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置DAO接口
 */
public interface SystemConfigDao extends BaseDao<SystemConfig, Integer> {
    
    /**
     * 根据配置键查询配置
     */
    Optional<SystemConfig> findByKey(String configKey);
    
    /**
     * 更新配置值
     */
    boolean updateValue(String configKey, String configValue);
    
    /**
     * 批量更新配置
     */
    boolean updateBatch(List<SystemConfig> configs);
}