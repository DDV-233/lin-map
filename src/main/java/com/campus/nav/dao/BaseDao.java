package com.campus.nav.dao;

import com.campus.nav.model.PageQuery;
import com.campus.nav.model.PageResult;

import java.util.List;
import java.util.Optional;

/**
 * 基础DAO接口
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface BaseDao<T, ID> {
    
    /**
     * 保存实体
     */
    boolean save(T entity);
    
    /**
     * 更新实体
     */
    boolean update(T entity);
    
    /**
     * 根据ID删除
     */
    boolean deleteById(ID id);
    
    /**
     * 根据ID查询
     */
    Optional<T> findById(ID id);
    
    /**
     * 查询所有
     */
    List<T> findAll();
    
    /**
     * 分页查询
     */
    PageResult<T> findByPage(PageQuery query);
    
    /**
     * 统计总数
     */
    long count();
    
    /**
     * 批量保存
     */
    boolean saveBatch(List<T> entities);
}