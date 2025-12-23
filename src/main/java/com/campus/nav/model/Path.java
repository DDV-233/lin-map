package com.campus.nav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 路径实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Path {
    /**
     * 路径ID
     */
    private Integer id;

    /**
     * 起点地点ID
     */
    private Integer startLocationId;

    /**
     * 终点地点ID
     */
    private Integer endLocationId;

    /**
     * 起点地点（关联对象）
     */
    private Location startLocation;

    /**
     * 终点地点（关联对象）
     */
    private Location endLocation;

    /**
     * 路径距离（米）
     */
    private Double distance;

    /**
     * 预估时间（分钟）
     */
    private Integer timeCost;

    /**
     * 路径是否有绿荫
     */
    private Boolean hasShade;

    /**
     * 路径景色等级（1-5）
     */
    private Integer scenicLevel;

    /**
     * 是否为室内路径
     */
    private Boolean isIndoor;

    /**
     * 是否可用
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 计算加权距离（用于不同导航策略）
     * @param strategy 导航策略
     * @param weights 权重配置
     * @return 加权后的距离
     */
    public double calculateWeightedDistance(NavigationStrategy strategy, java.util.Map<String, Double> weights) {
        double baseDistance = this.distance != null ? this.distance : 0;
        double weight = 1.0;

        switch (strategy) {
            case SHADIEST:
                weight = Boolean.TRUE.equals(this.hasShade) ?
                        weights.getOrDefault("shade", 1.5) : 1.0;
                break;
            case MOST_SCENIC:
                int scenic = this.scenicLevel != null ? this.scenicLevel : 1;
                // 景色越好，权重越小（距离"更短"）
                weight = 1.0 / (scenic * weights.getOrDefault("scenic", 1.3));
                break;
            case SHORTEST:
            default:
                weight = weights.getOrDefault("shortest", 1.0);
        }

        return baseDistance * weight;
    }
}