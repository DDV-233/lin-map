package com.campus.nav.service;

import com.campus.nav.model.AuthInfo;
import com.campus.nav.model.User;

import java.util.Optional;

/**
 * 用户Service接口
 */
public interface UserService extends BaseService<User, Integer> {
    
    /**
     * 用户注册
     */
    AuthInfo register(String username, String password, String email);
    
    /**
     * 用户登录
     */
    AuthInfo login(String username, String password);
    
    /**
     * 用户注销
     */
    void logout(Integer userId);
    
    /**
     * 检查用户名是否存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean isEmailExists(String email);
    
    /**
     * 更新用户密码
     */
    boolean updatePassword(Integer userId, String oldPassword, String newPassword);
    
    /**
     * 重置用户密码（管理员操作）
     */
    boolean resetPassword(Integer userId, String newPassword);
    
    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Integer userId, boolean isActive);
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
}