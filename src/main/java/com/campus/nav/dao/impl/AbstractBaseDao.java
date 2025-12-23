package com.campus.nav.dao.impl;

import com.campus.nav.dao.BaseDao;
import com.campus.nav.model.PageQuery;
import com.campus.nav.model.PageResult;
import com.campus.nav.utils.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

/**
 * 抽象基础DAO实现类
 */
public abstract class AbstractBaseDao<T, ID> implements BaseDao<T, ID> {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final Class<T> entityClass;
    
    @SuppressWarnings("unchecked")
    public AbstractBaseDao() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    @Override
    public boolean save(T entity) {
        throw new UnsupportedOperationException("请在各具体DAO中实现");
    }
    
    @Override
    public boolean update(T entity) {
        throw new UnsupportedOperationException("请在各具体DAO中实现");
    }
    
    @Override
    public boolean deleteById(ID id) {
        throw new UnsupportedOperationException("请在各具体DAO中实现");
    }
    
    @Override
    public Optional<T> findById(ID id) {
        throw new UnsupportedOperationException("请在各具体DAO中实现");
    }
    
    @Override
    public List<T> findAll() {
        throw new UnsupportedOperationException("请在各具体DAO中实现");
    }
    
    @Override
    public PageResult<T> findByPage(PageQuery query) {
        throw new UnsupportedOperationException("请在各具体DAO中实现");
    }
    
    @Override
    public long count() {
        throw new UnsupportedOperationException("请在各具体DAO中实现");
    }
    
    @Override
    public boolean saveBatch(List<T> entities) {
        throw new UnsupportedOperationException("请在各具体DAO中实现");
    }
    
    /**
     * 执行查询并返回单个结果
     */
    protected Optional<T> queryForObject(String sql, Object... params) {
        List<T> results = DatabaseUtil.executeQuery(sql, getRowMapper(), params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * 执行查询并返回列表
     */
    protected List<T> queryForList(String sql, Object... params) {
        return DatabaseUtil.executeQuery(sql, getRowMapper(), params);
    }
    
    /**
     * 获取行映射器
     */
    protected abstract DatabaseUtil.RowMapper<T> getRowMapper();
}