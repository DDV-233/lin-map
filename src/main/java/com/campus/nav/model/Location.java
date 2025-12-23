package com.campus.nav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 地点实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    /**
     * 地点ID
     */
    private Integer id;
    
    /**
     * 地点名称
     */
    private String name;
    
    /**
     * 地点描述
     */
    private String description;
    
    /**
     * 地点类型
     */
    private LocationType type;
    
    /**
     * X坐标（用于地图显示）
     */
    private Double xCoordinate;
    
    /**
     * Y坐标（用于地图显示）
     */
    private Double yCoordinate;
    
    /**
     * 是否有绿荫
     */
    private Boolean hasShade;
    
    /**
     * 景色等级（1-5）
     */
    private Integer scenicLevel;
    
    /**
     * 是否可通行
     */
    private Boolean isAccessible;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 地点类型枚举
     */
    public enum LocationType {
        BUILDING("教学楼"),
        GARDEN("花园"),
        CAFETERIA("食堂"),
        LIBRARY("图书馆"),
        SPORTS("体育场馆"),
        GATE("校门"),
        DORMITORY("宿舍"),
        LABORATORY("实验室"),
        OFFICE("办公楼"),
        PARKING("停车场"),
        OTHER("其他");
        
        private final String description;
        
        LocationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static LocationType fromString(String type) {
            for (LocationType locationType : LocationType.values()) {
                if (locationType.name().equalsIgnoreCase(type)) {
                    return locationType;
                }
            }
            return OTHER;
        }
        
        /**
         * 获取所有类型的描述数组
         */
        public static String[] getAllDescriptions() {
            LocationType[] types = values();
            String[] descriptions = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                descriptions[i] = types[i].getDescription();
            }
            return descriptions;
        }
    }
}