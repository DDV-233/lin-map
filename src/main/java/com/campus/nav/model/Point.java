package com.campus.nav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 坐标点类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    /**
     * X坐标
     */
    private double x;
    
    /**
     * Y坐标
     */
    private double y;
    
    /**
     * 标签
     */
    private String label;
    
    /**
     * 计算两点间的距离
     */
    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 转换为Location坐标
     */
    public static Point fromLocation(Location location) {
        if (location == null) {
            return null;
        }
        return Point.builder()
                .x(location.getXCoordinate())
                .y(location.getYCoordinate())
                .label(location.getName())
                .build();
    }
}