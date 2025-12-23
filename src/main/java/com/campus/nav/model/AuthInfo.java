package com.campus.nav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户认证信息类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthInfo {
    /**
     * 是否认证成功
     */
    private boolean authenticated;
    
    /**
     * 认证用户
     */
    private User user;
    
    /**
     * 认证令牌（可用于后续API调用）
     */
    private String token;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建成功认证信息
     */
    public static AuthInfo success(User user) {
        return AuthInfo.builder()
                .authenticated(true)
                .user(user)
                .build();
    }
    
    /**
     * 创建失败认证信息
     */
    public static AuthInfo fail(String errorMessage) {
        return AuthInfo.builder()
                .authenticated(false)
                .errorMessage(errorMessage)
                .build();
    }
}