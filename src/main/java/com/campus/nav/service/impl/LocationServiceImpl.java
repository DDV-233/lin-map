package com.campus.nav.service.impl;

import com.campus.nav.config.SystemConfig;
import com.campus.nav.dao.DaoFactory;
import com.campus.nav.dao.LocationDao;
import com.campus.nav.exception.ValidationException;
import com.campus.nav.model.Location;
import com.campus.nav.service.LocationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 地点Service实现类
 */
public class LocationServiceImpl extends AbstractBaseService<Location, Integer> implements LocationService {
    private static final Logger logger = LogManager.getLogger(LocationServiceImpl.class);
    
    private final LocationDao locationDao;
    
    public LocationServiceImpl() {
        this.locationDao = DaoFactory.getLocationDao();
    }
    
    @Override
    public Optional<Location> findByName(String name) {
        try {
            if (StringUtils.isBlank(name)) {
                return Optional.empty();
            }
            return locationDao.findByName(name.trim());
        } catch (Exception e) {
            logger.error("根据名称查找地点失败: {}", name, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<Location> findByType(Location.LocationType type) {
        try {
            if (type == null) {
                return List.of();
            }
            return locationDao.findByType(type);
        } catch (Exception e) {
            logger.error("根据类型查找地点失败: {}", type, e);
            return List.of();
        }
    }
    
    @Override
    public List<Location> findAccessibleLocations() {
        try {
            return locationDao.findAccessibleLocations();
        } catch (Exception e) {
            logger.error("查询可通行地点失败", e);
            return List.of();
        }
    }
    
    @Override
    public List<Location> findByCoordinateRange(double minX, double maxX, double minY, double maxY) {
        try {
            return locationDao.findByCoordinateRange(minX, maxX, minY, maxY);
        } catch (Exception e) {
            logger.error("根据坐标范围查询地点失败", e);
            return List.of();
        }
    }
    
    @Override
    public List<Location> search(String keyword) {
        try {
            if (StringUtils.isBlank(keyword)) {
                return List.of();
            }
            return locationDao.search(keyword.trim());
        } catch (Exception e) {
            logger.error("搜索地点失败: {}", keyword, e);
            return List.of();
        }
    }
    
    @Override
    public List<Location> getMapLocations() {
        try {
            // 获取地图边界
            int mapWidth = SystemConfig.getMapWidth();
            int mapHeight = SystemConfig.getMapHeight();
            
            // 返回所有在地图范围内的地点
            return findByCoordinateRange(0, mapWidth, 0, mapHeight);
        } catch (Exception e) {
            logger.error("获取地图地点失败", e);
            return List.of();
        }
    }
    
    @Override
    public boolean validateCoordinates(double x, double y) {
        int mapWidth = SystemConfig.getMapWidth();
        int mapHeight = SystemConfig.getMapHeight();
        
        return x >= 0 && x <= mapWidth && y >= 0 && y <= mapHeight;
    }
    
    @Override
    public boolean save(Location location) {
        try {
            if (!validateEntity(location)) {
                return false;
            }
            
            // 验证必要字段
            if (StringUtils.isBlank(location.getName())) {
                throw new ValidationException("地点名称不能为空");
            }
            
            if (location.getXCoordinate() == null || location.getYCoordinate() == null) {
                throw new ValidationException("地点坐标不能为空");
            }
            
            // 验证坐标合法性
            if (!validateCoordinates(location.getXCoordinate(), location.getYCoordinate())) {
                throw new ValidationException("地点坐标超出地图范围");
            }
            
            // 设置默认值
            if (location.getType() == null) {
                location.setType(Location.LocationType.OTHER);
            }
            
            if (location.getHasShade() == null) {
                location.setHasShade(false);
            }
            
            if (location.getScenicLevel() == null) {
                location.setScenicLevel(1);
            }
            
            if (location.getIsAccessible() == null) {
                location.setIsAccessible(true);
            }
            
            if (location.getCreatedAt() == null) {
                location.setCreatedAt(LocalDateTime.now());
            }
            
            return locationDao.save(location);
            
        } catch (ValidationException e) {
            logger.warn("保存地点验证失败", e);
            throw e;
        } catch (Exception e) {
            logger.error("保存地点失败: {}", location.getName(), e);
            return false;
        }
    }
    
    @Override
    public boolean update(Location location) {
        try {
            if (!validateEntity(location)) {
                return false;
            }
            
            if (location.getId() == null) {
                throw new ValidationException("地点ID不能为空");
            }
            
            // 验证必要字段
            if (StringUtils.isBlank(location.getName())) {
                throw new ValidationException("地点名称不能为空");
            }
            
            if (location.getXCoordinate() == null || location.getYCoordinate() == null) {
                throw new ValidationException("地点坐标不能为空");
            }
            
            // 验证坐标合法性
            if (!validateCoordinates(location.getXCoordinate(), location.getYCoordinate())) {
                throw new ValidationException("地点坐标超出地图范围");
            }
            
            return locationDao.update(location);
            
        } catch (ValidationException e) {
            logger.warn("更新地点验证失败: {}", location.getId(), e);
            throw e;
        } catch (Exception e) {
            logger.error("更新地点失败: {}", location.getId(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        try {
            if (id == null) {
                throw new ValidationException("地点ID不能为空");
            }
            
            return locationDao.deleteById(id);
            
        } catch (ValidationException e) {
            logger.warn("删除地点验证失败: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("删除地点失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public Optional<Location> findById(Integer id) {
        try {
            if (id == null) {
                return Optional.empty();
            }
            return locationDao.findById(id);
        } catch (Exception e) {
            logger.error("根据ID查找地点失败: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<Location> findAll() {
        try {
            return locationDao.findAll();
        } catch (Exception e) {
            logger.error("查询所有地点失败", e);
            return List.of();
        }
    }
    
    @Override
    public com.campus.nav.model.PageResult<Location> findByPage(com.campus.nav.model.PageQuery query) {
        try {
            return locationDao.findByPage(query);
        } catch (Exception e) {
            logger.error("分页查询地点失败", e);
            return com.campus.nav.model.PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public long count() {
        try {
            return locationDao.count();
        } catch (Exception e) {
            logger.error("统计地点总数失败", e);
            return 0;
        }
    }
}