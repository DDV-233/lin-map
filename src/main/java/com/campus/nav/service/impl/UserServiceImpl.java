package com.campus.nav.service.impl;

import com.campus.nav.dao.DaoFactory;
import com.campus.nav.dao.UserDao;
import com.campus.nav.exception.ValidationException;
import com.campus.nav.model.AuthInfo;
import com.campus.nav.model.User;
import com.campus.nav.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户Service实现类
 */
public class UserServiceImpl extends AbstractBaseService<User, Integer> implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    
    private final UserDao userDao;
    
    public UserServiceImpl() {
        this.userDao = DaoFactory.getUserDao();
    }
    
    @Override
    public AuthInfo register(String username, String password, String email) {
        try {
            // 参数验证
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(email)) {
                return AuthInfo.fail("用户名、密码和邮箱不能为空");
            }
            
            if (username.length() < 3 || username.length() > 50) {
                return AuthInfo.fail("用户名长度必须在3-50个字符之间");
            }
            
            if (password.length() < 6) {
                return AuthInfo.fail("密码长度不能少于6个字符");
            }
            
            if (!email.contains("@")) {
                return AuthInfo.fail("邮箱格式不正确");
            }
            
            // 检查用户名是否已存在
            if (userDao.existsByUsername(username)) {
                return AuthInfo.fail("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (userDao.existsByEmail(email)) {
                return AuthInfo.fail("邮箱已被注册");
            }
            
            // 创建用户对象
            User newUser = User.builder()
                    .username(username.trim())
                    .password(hashPassword(password)) // 实际项目中应该使用加密
                    .email(email.trim())
                    .userType(User.UserType.USER)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            
            // 保存用户
            if (userDao.save(newUser)) {
                logger.info("用户注册成功: {}", username);
                
                // 查询保存后的用户（包含ID）
                Optional<User> savedUser = userDao.findByUsername(username);
                return savedUser.map(AuthInfo::success)
                        .orElseGet(() -> AuthInfo.fail("用户注册成功，但登录失败"));
            } else {
                return AuthInfo.fail("用户注册失败，请稍后重试");
            }
            
        } catch (Exception e) {
            logger.error("用户注册失败: {}", username, e);
            return AuthInfo.fail("注册过程中出现错误: " + e.getMessage());
        }
    }
    
    @Override
    public AuthInfo login(String username, String password) {
        try {
            // 参数验证
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                return AuthInfo.fail("用户名和密码不能为空");
            }
            
            // 验证用户（这里使用明文密码验证，实际项目中应该使用加密验证）
            Optional<User> userOptional = userDao.validateUser(username, password);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // 检查用户是否激活
                if (Boolean.FALSE.equals(user.getIsActive())) {
                    return AuthInfo.fail("用户已被禁用，请联系管理员");
                }
                
                logger.info("用户登录成功: {}", username);
                return AuthInfo.success(user);
            } else {
                return AuthInfo.fail("用户名或密码错误");
            }
            
        } catch (Exception e) {
            logger.error("用户登录失败: {}", username, e);
            return AuthInfo.fail("登录过程中出现错误: " + e.getMessage());
        }
    }
    
    @Override
    public void logout(Integer userId) {
        // 这里可以添加清理session、token等操作
        logger.info("用户登出: {}", userId);
    }
    
    @Override
    public boolean isUsernameExists(String username) {
        try {
            return userDao.existsByUsername(username);
        } catch (Exception e) {
            logger.error("检查用户名是否存在失败: {}", username, e);
            return false;
        }
    }
    
    @Override
    public boolean isEmailExists(String email) {
        try {
            return userDao.existsByEmail(email);
        } catch (Exception e) {
            logger.error("检查邮箱是否存在失败: {}", email, e);
            return false;
        }
    }
    
    @Override
    public boolean updatePassword(Integer userId, String oldPassword, String newPassword) {
        try {
            // 参数验证
            if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
                throw new ValidationException("原密码和新密码不能为空");
            }
            
            if (newPassword.length() < 6) {
                throw new ValidationException("新密码长度不能少于6个字符");
            }
            
            // 验证原密码
            Optional<User> userOptional = userDao.findById(userId);
            if (userOptional.isEmpty()) {
                throw new ValidationException("用户不存在");
            }
            
            User user = userOptional.get();
            if (!user.getPassword().equals(oldPassword)) {
                throw new ValidationException("原密码错误");
            }
            
            // 更新密码
            return userDao.updatePassword(userId, hashPassword(newPassword));
            
        } catch (ValidationException e) {
            logger.warn("修改密码验证失败: {}", userId, e);
            throw e;
        } catch (Exception e) {
            logger.error("修改密码失败: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public boolean resetPassword(Integer userId, String newPassword) {
        try {
            // 参数验证
            if (StringUtils.isBlank(newPassword)) {
                throw new ValidationException("新密码不能为空");
            }
            
            if (newPassword.length() < 6) {
                throw new ValidationException("新密码长度不能少于6个字符");
            }
            
            // 检查用户是否存在
            Optional<User> userOptional = userDao.findById(userId);
            if (userOptional.isEmpty()) {
                throw new ValidationException("用户不存在");
            }
            
            // 重置密码
            return userDao.updatePassword(userId, hashPassword(newPassword));
            
        } catch (ValidationException e) {
            logger.warn("重置密码验证失败: {}", userId, e);
            throw e;
        } catch (Exception e) {
            logger.error("重置密码失败: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public boolean updateUserStatus(Integer userId, boolean isActive) {
        try {
            // 检查用户是否存在
            Optional<User> userOptional = userDao.findById(userId);
            if (userOptional.isEmpty()) {
                throw new ValidationException("用户不存在");
            }
            
            // 更新状态
            return userDao.updateStatus(userId, isActive);
            
        } catch (ValidationException e) {
            logger.warn("更新用户状态验证失败: {}", userId, e);
            throw e;
        } catch (Exception e) {
            logger.error("更新用户状态失败: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        try {
            return userDao.findByUsername(username);
        } catch (Exception e) {
            logger.error("根据用户名查找用户失败: {}", username, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        try {
            return userDao.findByEmail(email);
        } catch (Exception e) {
            logger.error("根据邮箱查找用户失败: {}", email, e);
            return Optional.empty();
        }
    }
    
    @Override
    public boolean save(User user) {
        try {
            if (!validateEntity(user)) {
                return false;
            }
            
            // 验证必要字段
            if (StringUtils.isBlank(user.getUsername())) {
                throw new ValidationException("用户名不能为空");
            }
            
            if (StringUtils.isBlank(user.getPassword())) {
                throw new ValidationException("密码不能为空");
            }
            
            if (StringUtils.isBlank(user.getEmail())) {
                throw new ValidationException("邮箱不能为空");
            }
            
            // 加密密码
            user.setPassword(hashPassword(user.getPassword()));
            
            return userDao.save(user);
            
        } catch (ValidationException e) {
            logger.warn("保存用户验证失败", e);
            throw e;
        } catch (Exception e) {
            logger.error("保存用户失败", e);
            return false;
        }
    }
    
    @Override
    public boolean update(User user) {
        try {
            if (!validateEntity(user)) {
                return false;
            }
            
            if (user.getId() == null) {
                throw new ValidationException("用户ID不能为空");
            }
            
            return userDao.update(user);
            
        } catch (ValidationException e) {
            logger.warn("更新用户验证失败: {}", user.getId(), e);
            throw e;
        } catch (Exception e) {
            logger.error("更新用户失败: {}", user.getId(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        try {
            if (id == null) {
                throw new ValidationException("用户ID不能为空");
            }
            
            return userDao.deleteById(id);
            
        } catch (ValidationException e) {
            logger.warn("删除用户验证失败: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("删除用户失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public Optional<User> findById(Integer id) {
        try {
            if (id == null) {
                return Optional.empty();
            }
            return userDao.findById(id);
        } catch (Exception e) {
            logger.error("根据ID查找用户失败: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<User> findAll() {
        try {
            return userDao.findAll();
        } catch (Exception e) {
            logger.error("查询所有用户失败", e);
            return List.of();
        }
    }
    
    @Override
    public com.campus.nav.model.PageResult<User> findByPage(com.campus.nav.model.PageQuery query) {
        try {
            return userDao.findByPage(query);
        } catch (Exception e) {
            logger.error("分页查询用户失败", e);
            return com.campus.nav.model.PageResult.of(List.of(), 0L, query);
        }
    }
    
    @Override
    public long count() {
        try {
            return userDao.count();
        } catch (Exception e) {
            logger.error("统计用户总数失败", e);
            return 0;
        }
    }
    
    /**
     * 密码哈希处理（实际项目中应该使用BCrypt等安全哈希算法）
     */
    private String hashPassword(String password) {
        // 这里简单返回明文，实际项目应该加密
        // 例如: return BCrypt.hashpw(password, BCrypt.gensalt());
        return password;
    }
}