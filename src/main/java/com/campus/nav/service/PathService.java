package com.campus.nav.service;

import com.campus.nav.model.Path;

import java.util.List;
import java.util.Optional;

/**
 * 路径Service接口
 */
public interface PathService extends BaseService<Path, Integer> {
    
    /**
     * 根据起点和终点查询路径
     */
    Optional<Path> findByStartAndEnd(Integer startLocationId, Integer endLocationId);
    
    /**
     * 查询所有从指定地点出发的路径
     */
    List<Path> findByStartLocation(Integer startLocationId);
    
    /**
     * 查询所有到达指定地点的路径
     */
    List<Path> findByEndLocation(Integer endLocationId);
    
    /**
     * 查询地点之间的所有路径（双向）
     */
    List<Path> findPathsBetween(Integer locationId1, Integer locationId2);
    
    /**
     * 查询所有可用路径
     */
    List<Path> findActivePaths();
    
    /**
     * 更新路径状态
     */
    boolean updateStatus(Integer pathId, boolean isActive);
    
    /**
     * 计算两点间的实际距离
     */
    double calculateDistance(Integer startLocationId, Integer endLocationId);
    
    /**
     * 批量创建双向路径
     */
    boolean createBidirectionalPath(Integer locationId1, Integer locationId2, 
                                   double distance, Integer timeCost, 
                                   boolean hasShade, Integer scenicLevel);
}