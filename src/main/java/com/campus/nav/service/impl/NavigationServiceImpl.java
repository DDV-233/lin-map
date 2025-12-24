package com.campus.nav.service.impl;

import com.campus.nav.config.SystemConfig;
import com.campus.nav.dao.DaoFactory;
import com.campus.nav.dao.LocationDao;
import com.campus.nav.dao.NavigationHistoryDao;
import com.campus.nav.dao.PathDao;
import com.campus.nav.exception.ValidationException;
import com.campus.nav.model.*;
import com.campus.nav.service.NavigationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导航Service实现类
 */
public class NavigationServiceImpl implements NavigationService {
    private static final Logger logger = LogManager.getLogger(NavigationServiceImpl.class);
    
    private final PathDao pathDao;
    private final NavigationHistoryDao navigationHistoryDao;
    
    public NavigationServiceImpl() {
        this.pathDao = DaoFactory.getPathDao();
        this.navigationHistoryDao = DaoFactory.getNavigationHistoryDao();
    }
    
    @Override
    public NavigationResult navigate(Integer startLocationId, Integer endLocationId, 
                                    NavigationStrategy strategy, User user) {
        try {
            // 参数验证
            if (startLocationId == null || endLocationId == null) {
                return NavigationResult.fail("起点和终点不能为空");
            }
            
            if (startLocationId.equals(endLocationId)) {
                return NavigationResult.fail("起点和终点不能相同");
            }
            
            if (strategy == null) {
                strategy = NavigationStrategy.SHORTEST;
            }
            
            logger.info("开始导航计算: {} -> {}, 策略: {}", startLocationId, endLocationId, strategy);
            
            // 使用Dijkstra算法计算最短路径
            List<Location> pathLocations = calculatePath(startLocationId, endLocationId, strategy);
            
            if (pathLocations == null || pathLocations.size() < 2) {
                return NavigationResult.fail("无法找到从起点到终点的路径");
            }
            
            // 计算总距离和时间
            double totalDistance = 0.0;
            int totalTime = 0;
            List<Path> paths = new ArrayList<>();
            
            for (int i = 0; i < pathLocations.size() - 1; i++) {
                Location current = pathLocations.get(i);
                Location next = pathLocations.get(i + 1);
                
                Optional<Path> pathOpt = pathDao.findByStartAndEnd(current.getId(), next.getId());
                if (pathOpt.isPresent()) {
                    Path path = pathOpt.get();
                    totalDistance += path.getDistance();
                    totalTime += path.getTimeCost();
                    paths.add(path);
                }
            }
            
            // 保存导航历史
            if (user != null) {
                NavigationHistory history = NavigationHistory.builder()
                        .userId(user.getId())
                        .startLocationId(startLocationId)
                        .endLocationId(endLocationId)
                        .pathStrategy(strategy)
                        .totalDistance(totalDistance)
                        .totalTime(totalTime)
                        .createdAt(LocalDateTime.now())
                        .build();
                
                saveNavigationHistory(history);
            }
            
            // 返回导航结果
            NavigationResult result = NavigationResult.success(strategy, totalDistance, totalTime, 
                    pathLocations, paths);
            
            logger.info("导航计算完成: 距离={}米, 时间={}分钟", totalDistance, totalTime);
            return result;
            
        } catch (Exception e) {
            logger.error("导航计算失败: {} -> {}", startLocationId, endLocationId, e);
            return NavigationResult.fail("导航计算失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean saveNavigationHistory(NavigationHistory history) {
        try {
            if (history == null) {
                throw new ValidationException("导航历史不能为空");
            }
            
            if (history.getUserId() == null) {
                throw new ValidationException("用户ID不能为空");
            }
            
            if (history.getStartLocationId() == null || history.getEndLocationId() == null) {
                throw new ValidationException("起点和终点不能为空");
            }
            
            if (history.getTotalDistance() == null || history.getTotalDistance() <= 0) {
                throw new ValidationException("总距离必须大于0");
            }
            
            if (history.getCreatedAt() == null) {
                history.setCreatedAt(LocalDateTime.now());
            }
            
            return navigationHistoryDao.save(history);
            
        } catch (ValidationException e) {
            logger.warn("保存导航历史验证失败", e);
            throw e;
        } catch (Exception e) {
            logger.error("保存导航历史失败", e);
            return false;
        }
    }
    
    @Override
    public List<NavigationHistory> getUserNavigationHistory(Integer userId) {
        try {
            if (userId == null) {
                return List.of();
            }
            return navigationHistoryDao.findByUserId(userId);
        } catch (Exception e) {
            logger.error("获取用户导航历史失败: {}", userId, e);
            return List.of();
        }
    }
    
    @Override
    public PageResult<NavigationHistory> getUserNavigationHistoryPage(Integer userId, PageQuery query) {
        try {
            if (userId == null) {
                return PageResult.of(List.of(), 0L, query);
            }
            return navigationHistoryDao.findByUserIdPage(userId, query);
        } catch (Exception e) {
            logger.error("分页获取用户导航历史失败: {}", userId, e);
            return PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public boolean clearUserNavigationHistory(Integer userId) {
        try {
            if (userId == null) {
                throw new ValidationException("用户ID不能为空");
            }
            return navigationHistoryDao.deleteByUserId(userId);
        } catch (ValidationException e) {
            logger.warn("清除用户导航历史验证失败: {}", userId, e);
            throw e;
        } catch (Exception e) {
            logger.error("清除用户导航历史失败: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public List<Location> getRecommendedPath(Integer userId, Integer startLocationId, Integer endLocationId) {
        try {
            if (userId == null || startLocationId == null || endLocationId == null) {
                return List.of();
            }
            
            // 获取用户的历史导航记录
            List<NavigationHistory> userHistory = getUserNavigationHistory(userId);
            
            // 查找相似的历史路径
            for (NavigationHistory history : userHistory) {
                if (history.getStartLocationId().equals(startLocationId) && 
                    history.getEndLocationId().equals(endLocationId)) {
                    // 直接返回历史路径
                    return calculatePath(startLocationId, endLocationId, history.getPathStrategy());
                }
            }
            
            // 如果没有找到相同的历史路径，返回空
            return List.of();
            
        } catch (Exception e) {
            logger.error("获取推荐路径失败: {} -> {}", startLocationId, endLocationId, e);
            return List.of();
        }
    }
    
    @Override
    public List<Location> findNearbyLocations(Integer locationId, double radius) {
        try {
            if (locationId == null || radius <= 0) {
                return List.of();
            }
            
            // 获取所有从该地点出发的路径
            List<Path> paths = pathDao.findByStartLocation(locationId);
            
            // 过滤出距离在radius范围内的路径
            return paths.stream()
                    .filter(path -> path.getDistance() <= radius && path.getEndLocation() != null)
                    .map(Path::getEndLocation)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("查找附近地点失败: {}", locationId, e);
            return List.of();
        }
    }

    /**
     * 使用Dijkstra算法计算路径
     */
    private List<Location> calculatePath(Integer startId, Integer endId, NavigationStrategy strategy) {
        // 获取所有地点和路径
        List<Path> allPaths = pathDao.findActivePaths();

        // 获取所有地点信息（需要添加地点DAO）
        Map<Integer, Location> locationMap = getAllLocations();

        // 构建图
        Map<Integer, List<Edge>> graph = buildGraph(allPaths, strategy);

        // 检查起点和终点是否存在
        if (!graph.containsKey(startId) || !graph.containsKey(endId)) {
            logger.warn("起点或终点不存在于图中: startId={}, endId={}", startId, endId);
            return null;
        }

        // Dijkstra算法
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(Node::getDistance));

        // 初始化
        for (Integer locationId : graph.keySet()) {
            distances.put(locationId, Double.MAX_VALUE);
        }
        distances.put(startId, 0.0);
        pq.offer(new Node(startId, 0.0));

        // 算法主体
        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (current.getId().equals(endId)) {
                break;
            }

            if (current.getDistance() > distances.get(current.getId())) {
                continue;
            }

            List<Edge> edges = graph.get(current.getId());
            if (edges == null) continue;

            for (Edge edge : edges) {
                double newDist = current.getDistance() + edge.getWeight();
                if (newDist < distances.get(edge.getTo())) {
                    distances.put(edge.getTo(), newDist);
                    previous.put(edge.getTo(), current.getId());
                    pq.offer(new Node(edge.getTo(), newDist));
                }
            }
        }

        // 如果找不到路径
        if (!previous.containsKey(endId)) {
            logger.warn("找不到从起点到终点的路径: startId={}, endId={}", startId, endId);
            return null;
        }

        // 构建路径
        List<Location> path = new ArrayList<>();
        Integer current = endId;

        while (current != null) {
            // 从缓存中获取完整的地点信息
            Location location = locationMap.get(current);
            if (location == null) {
                logger.warn("地点信息缺失: locationId={}", current);
                return null;
            }
            path.add(0, location);
            current = previous.get(current);
        }

        // 验证路径的连续性
        for (int i = 0; i < path.size() - 1; i++) {
            Location currentLoc = path.get(i);
            Location nextLoc = path.get(i + 1);
            Optional<Path> pathOpt = pathDao.findByStartAndEnd(currentLoc.getId(), nextLoc.getId());
            if (!pathOpt.isPresent()) {
                logger.warn("路径不连续: {} -> {}", currentLoc.getId(), nextLoc.getId());
                return null;
            }
        }

        logger.debug("找到路径: {} -> {}, 包含 {} 个地点", startId, endId, path.size());
        return path;
    }

    /**
     * 获取所有地点信息
     */
    private Map<Integer, Location> getAllLocations() {
        // 这里需要实现地点DAO的方法，或者使用已有的方法
        // 假设有一个LocationDao
        try {
            // 临时方案：通过路径获取所有相关地点
            List<Path> allPaths = pathDao.findActivePaths();
            Set<Integer> locationIds = new HashSet<>();

            for (Path path : allPaths) {
                locationIds.add(path.getStartLocationId());
                locationIds.add(path.getEndLocationId());
            }

            // 这里需要调用LocationDao批量获取地点信息
            // 假设有一个方法：List<Location> findByIds(Collection<Integer> ids)
            LocationDao locationDao = DaoFactory.getLocationDao();
            List<Location> locations = locationDao.findAccessibleLocations();

            Map<Integer, Location> locationMap = new HashMap<>();
            for (Location location : locations) {
                locationMap.put(location.getId(), location);
            }

            return locationMap;

        } catch (Exception e) {
            logger.error("获取地点信息失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 构建图 - 改进版
     */
    private Map<Integer, List<Edge>> buildGraph(List<Path> paths, NavigationStrategy strategy) {
        Map<Integer, List<Edge>> graph = new HashMap<>();
        Map<String, Double> weights = SystemConfig.getPathWeights();

        for (Path path : paths) {
            // 检查路径是否有效
            if (!isPathValid(path)) {
                continue;
            }

            // 检查路径是否可用
            if (!Boolean.TRUE.equals(path.getIsActive())) {
                continue;
            }

            // 检查起点和终点地点是否可用
            if (path.getStartLocation() != null && Boolean.FALSE.equals(path.getStartLocation().getIsAccessible())) {
                continue;
            }
            if (path.getEndLocation() != null && Boolean.FALSE.equals(path.getEndLocation().getIsAccessible())) {
                continue;
            }

            // 计算边的权重
            double weight = path.calculateWeightedDistance(strategy, weights);

            // 添加正向边
            graph.computeIfAbsent(path.getStartLocationId(), k -> new ArrayList<>())
                    .add(new Edge(path.getEndLocationId(), weight, path));

            // 添加反向边（如果是双向路径）
            graph.computeIfAbsent(path.getEndLocationId(), k -> new ArrayList<>())
                    .add(new Edge(path.getStartLocationId(), weight, path));
        }

        return graph;
    }

    /**
     * 检查路径是否有效
     */
    private boolean isPathValid(Path path) {
        if (path == null) return false;
        if (path.getStartLocationId() == null || path.getEndLocationId() == null) return false;
        if (path.getStartLocationId().equals(path.getEndLocationId())) return false; // 自环路径
        if (path.getDistance() == null || path.getDistance() <= 0) return false;
        if (path.getTimeCost() == null || path.getTimeCost() <= 0) return false;
        return true;
    }

    /**
     * 图的边（增强版）
     */
    private static class Edge {
        private final Integer to;
        private final double weight;
        private final Path path; // 保留路径信息

        public Edge(Integer to, double weight, Path path) {
            this.to = to;
            this.weight = weight;
            this.path = path;
        }

        public Integer getTo() {
            return to;
        }

        public double getWeight() {
            return weight;
        }

        public Path getPath() {
            return path;
        }
    }

    
    /**
     * Dijkstra算法的节点
     */
    private static class Node {
        private final Integer id;
        private final double distance;
        
        public Node(Integer id, double distance) {
            this.id = id;
            this.distance = distance;
        }
        
        public Integer getId() {
            return id;
        }
        
        public double getDistance() {
            return distance;
        }
    }
}