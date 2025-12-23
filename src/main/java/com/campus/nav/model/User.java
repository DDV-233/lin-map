package com.campus.nav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 用户ID
     */
    private Integer id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码（加密存储）
     */
    private String password;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 用户类型：ADMIN-管理员，USER-普通用户
     */
    private UserType userType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 是否激活
     */
    private Boolean isActive;
    
    /**
     * 用户类型枚举
     */
    public enum UserType {
        ADMIN("管理员"),
        USER("普通用户");
        
        private final String description;
        
        UserType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static UserType fromString(String type) {
            for (UserType userType : UserType.values()) {
                if (userType.name().equalsIgnoreCase(type)) {
                    return userType;
                }
            }
            return USER; // 默认普通用户
        }
    }
}