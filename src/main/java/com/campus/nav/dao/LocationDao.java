package com.campus.nav.dao;

import com.campus.nav.model.Location;

import java.util.List;
import java.util.Optional;

/**
 * 地点DAO接口
 */
public interface LocationDao extends BaseDao<Location, Integer> {
    
    /**
     * 根据名称查询地点
     */
    Optional<Location> findByName(String name);
    
    /**
     * 根据类型查询地点列表
     */
    List<Location> findByType(Location.LocationType type);
    
    /**
     * 查询所有可通行的地点
     */
    List<Location> findAccessibleLocations();
    
    /**
     * 根据坐标范围查询地点
     */
    List<Location> findByCoordinateRange(double minX, double maxX, double minY, double maxY);
    
    /**
     * 搜索地点（根据名称或描述）
     */
    List<Location> search(String keyword);
}