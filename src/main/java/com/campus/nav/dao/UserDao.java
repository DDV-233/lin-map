package com.campus.nav.dao;

import com.campus.nav.model.User;

import java.util.Optional;

/**
 * 用户DAO接口
 */
public interface UserDao extends BaseDao<User, Integer> {
    
    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 验证用户名和密码
     */
    Optional<User> validateUser(String username, String password);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 更新用户密码
     */
    boolean updatePassword(Integer userId, String newPassword);
    
    /**
     * 更新用户状态
     */
    boolean updateStatus(Integer userId, boolean isActive);
}