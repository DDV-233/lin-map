package com.campus.nav.service.impl;

import com.campus.nav.dao.DaoFactory;
import com.campus.nav.dao.LocationDao;
import com.campus.nav.dao.PathDao;
import com.campus.nav.exception.ValidationException;
import com.campus.nav.model.Location;
import com.campus.nav.model.Path;
import com.campus.nav.service.PathService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 路径Service实现类
 */
public class PathServiceImpl extends AbstractBaseService<Path, Integer> implements PathService {
    private static final Logger logger = LogManager.getLogger(PathServiceImpl.class);
    
    private final PathDao pathDao;
    private final LocationDao locationDao;
    
    public PathServiceImpl() {
        this.pathDao = DaoFactory.getPathDao();
        this.locationDao = DaoFactory.getLocationDao();
    }
    
    @Override
    public Optional<Path> findByStartAndEnd(Integer startLocationId, Integer endLocationId) {
        try {
            if (startLocationId == null || endLocationId == null) {
                return Optional.empty();
            }
            
            if (startLocationId.equals(endLocationId)) {
                throw new ValidationException("起点和终点不能相同");
            }
            
            return pathDao.findByStartAndEnd(startLocationId, endLocationId);
        } catch (ValidationException e) {
            logger.warn("查询路径验证失败: {} -> {}", startLocationId, endLocationId, e);
            throw e;
        } catch (Exception e) {
            logger.error("查询路径失败: {} -> {}", startLocationId, endLocationId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<Path> findByStartLocation(Integer startLocationId) {
        try {
            if (startLocationId == null) {
                return List.of();
            }
            return pathDao.findByStartLocation(startLocationId);
        } catch (Exception e) {
            logger.error("根据起点查询路径失败: {}", startLocationId, e);
            return List.of();
        }
    }
    
    @Override
    public List<Path> findByEndLocation(Integer endLocationId) {
        try {
            if (endLocationId == null) {
                return List.of();
            }
            return pathDao.findByEndLocation(endLocationId);
        } catch (Exception e) {
            logger.error("根据终点查询路径失败: {}", endLocationId, e);
            return List.of();
        }
    }
    
    @Override
    public List<Path> findPathsBetween(Integer locationId1, Integer locationId2) {
        try {
            if (locationId1 == null || locationId2 == null) {
                return List.of();
            }
            
            if (locationId1.equals(locationId2)) {
                throw new ValidationException("两个地点不能相同");
            }
            
            return pathDao.findPathsBetween(locationId1, locationId2);
        } catch (ValidationException e) {
            logger.warn("查询双向路径验证失败: {} <-> {}", locationId1, locationId2, e);
            throw e;
        } catch (Exception e) {
            logger.error("查询双向路径失败: {} <-> {}", locationId1, locationId2, e);
            return List.of();
        }
    }
    
    @Override
    public List<Path> findActivePaths() {
        try {
            return pathDao.findActivePaths();
        } catch (Exception e) {
            logger.error("查询可用路径失败", e);
            return List.of();
        }
    }
    
    @Override
    public boolean updateStatus(Integer pathId, boolean isActive) {
        try {
            if (pathId == null) {
                throw new ValidationException("路径ID不能为空");
            }
            
            return pathDao.updateStatus(pathId, isActive);
        } catch (ValidationException e) {
            logger.warn("更新路径状态验证失败: {}", pathId, e);
            throw e;
        } catch (Exception e) {
            logger.error("更新路径状态失败: {}", pathId, e);
            return false;
        }
    }
    
    @Override
    public double calculateDistance(Integer startLocationId, Integer endLocationId) {
        try {
            if (startLocationId == null || endLocationId == null) {
                throw new ValidationException("起点和终点ID不能为空");
            }
            
            if (startLocationId.equals(endLocationId)) {
                return 0.0;
            }
            
            Optional<Location> startOpt = locationDao.findById(startLocationId);
            Optional<Location> endOpt = locationDao.findById(endLocationId);
            
            if (startOpt.isEmpty() || endOpt.isEmpty()) {
                throw new ValidationException("起点或终点不存在");
            }
            
            Location start = startOpt.get();
            Location end = endOpt.get();
            
            // 使用欧几里得距离计算
            double dx = end.getXCoordinate() - start.getXCoordinate();
            double dy = end.getYCoordinate() - start.getYCoordinate();
            
            return Math.sqrt(dx * dx + dy * dy);
            
        } catch (ValidationException e) {
            logger.warn("计算距离验证失败: {} -> {}", startLocationId, endLocationId, e);
            throw e;
        } catch (Exception e) {
            logger.error("计算距离失败: {} -> {}", startLocationId, endLocationId, e);
            return 0.0;
        }
    }
    
    @Override
    public boolean createBidirectionalPath(Integer locationId1, Integer locationId2, 
                                          double distance, Integer timeCost, 
                                          boolean hasShade, Integer scenicLevel) {
        try {
            if (locationId1 == null || locationId2 == null) {
                throw new ValidationException("地点ID不能为空");
            }
            
            if (locationId1.equals(locationId2)) {
                throw new ValidationException("两个地点不能相同");
            }
            
            if (distance <= 0) {
                throw new ValidationException("距离必须大于0");
            }
            
            // 检查地点是否存在
            Optional<Location> loc1Opt = locationDao.findById(locationId1);
            Optional<Location> loc2Opt = locationDao.findById(locationId2);
            
            if (loc1Opt.isEmpty() || loc2Opt.isEmpty()) {
                throw new ValidationException("地点不存在");
            }
            
            // 检查路径是否已存在
            Optional<Path> existingPath = pathDao.findByStartAndEnd(locationId1, locationId2);
            if (existingPath.isPresent()) {
                throw new ValidationException("路径已存在");
            }
            
            // 创建正向路径
            Path forwardPath = Path.builder()
                    .startLocationId(locationId1)
                    .endLocationId(locationId2)
                    .distance(distance)
                    .timeCost(timeCost != null ? timeCost : (int)(distance / 1.4)) // 假设步行速度1.4m/s
                    .hasShade(hasShade)
                    .scenicLevel(scenicLevel != null ? scenicLevel : 1)
                    .isIndoor(false)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // 创建反向路径
            Path backwardPath = Path.builder()
                    .startLocationId(locationId2)
                    .endLocationId(locationId1)
                    .distance(distance)
                    .timeCost(timeCost != null ? timeCost : (int)(distance / 1.4))
                    .hasShade(hasShade)
                    .scenicLevel(scenicLevel != null ? scenicLevel : 1)
                    .isIndoor(false)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // 保存两条路径
            boolean forwardSuccess = pathDao.save(forwardPath);
            boolean backwardSuccess = pathDao.save(backwardPath);
            
            return forwardSuccess && backwardSuccess;
            
        } catch (ValidationException e) {
            logger.warn("创建双向路径验证失败: {} <-> {}", locationId1, locationId2, e);
            throw e;
        } catch (Exception e) {
            logger.error("创建双向路径失败: {} <-> {}", locationId1, locationId2, e);
            return false;
        }
    }
    
    @Override
    public boolean save(Path path) {
        try {
            if (!validateEntity(path)) {
                return false;
            }
            
            // 验证必要字段
            if (path.getStartLocationId() == null || path.getEndLocationId() == null) {
                throw new ValidationException("起点和终点ID不能为空");
            }
            
            if (path.getStartLocationId().equals(path.getEndLocationId())) {
                throw new ValidationException("起点和终点不能相同");
            }
            
            if (path.getDistance() == null || path.getDistance() <= 0) {
                throw new ValidationException("距离必须大于0");
            }
            
            // 检查地点是否存在
            Optional<Location> startOpt = locationDao.findById(path.getStartLocationId());
            Optional<Location> endOpt = locationDao.findById(path.getEndLocationId());
            
            if (startOpt.isEmpty() || endOpt.isEmpty()) {
                throw new ValidationException("起点或终点不存在");
            }
            
            // 检查路径是否已存在
            Optional<Path> existingPath = pathDao.findByStartAndEnd(
                    path.getStartLocationId(), path.getEndLocationId());
            if (existingPath.isPresent()) {
                throw new ValidationException("路径已存在");
            }
            
            // 设置默认值
            if (path.getTimeCost() == null) {
                path.setTimeCost((int)(path.getDistance() / 1.4)); // 假设步行速度1.4m/s
            }
            
            if (path.getHasShade() == null) {
                path.setHasShade(false);
            }
            
            if (path.getScenicLevel() == null) {
                path.setScenicLevel(1);
            }
            
            if (path.getIsIndoor() == null) {
                path.setIsIndoor(false);
            }
            
            if (path.getIsActive() == null) {
                path.setIsActive(true);
            }
            
            if (path.getCreatedAt() == null) {
                path.setCreatedAt(LocalDateTime.now());
            }
            
            return pathDao.save(path);
            
        } catch (ValidationException e) {
            logger.warn("保存路径验证失败: {} -> {}", 
                    path.getStartLocationId(), path.getEndLocationId(), e);
            throw e;
        } catch (Exception e) {
            logger.error("保存路径失败: {} -> {}", 
                    path.getStartLocationId(), path.getEndLocationId(), e);
            return false;
        }
    }
    
    @Override
    public boolean update(Path path) {
        try {
            if (!validateEntity(path)) {
                return false;
            }
            
            if (path.getId() == null) {
                throw new ValidationException("路径ID不能为空");
            }
            
            // 验证必要字段
            if (path.getDistance() == null || path.getDistance() <= 0) {
                throw new ValidationException("距离必须大于0");
            }
            
            return pathDao.update(path);
            
        } catch (ValidationException e) {
            logger.warn("更新路径验证失败: {}", path.getId(), e);
            throw e;
        } catch (Exception e) {
            logger.error("更新路径失败: {}", path.getId(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        try {
            if (id == null) {
                throw new ValidationException("路径ID不能为空");
            }
            
            return pathDao.deleteById(id);
            
        } catch (ValidationException e) {
            logger.warn("删除路径验证失败: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("删除路径失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public Optional<Path> findById(Integer id) {
        try {
            if (id == null) {
                return Optional.empty();
            }
            return pathDao.findById(id);
        } catch (Exception e) {
            logger.error("根据ID查找路径失败: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<Path> findAll() {
        try {
            return pathDao.findAll();
        } catch (Exception e) {
            logger.error("查询所有路径失败", e);
            return List.of();
        }
    }
    
    @Override
    public com.campus.nav.model.PageResult<Path> findByPage(com.campus.nav.model.PageQuery query) {
        try {
            return pathDao.findByPage(query);
        } catch (Exception e) {
            logger.error("分页查询路径失败", e);
            return com.campus.nav.model.PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public long count() {
        try {
            return pathDao.count();
        } catch (Exception e) {
            logger.error("统计路径总数失败", e);
            return 0;
        }
    }
}