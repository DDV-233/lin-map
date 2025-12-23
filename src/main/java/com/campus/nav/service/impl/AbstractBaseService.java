package com.campus.nav.service.impl;

import com.campus.nav.service.BaseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * 抽象基础Service类
 */
public abstract class AbstractBaseService<T, ID> implements BaseService<T, ID> {
    protected final Logger logger = LogManager.getLogger(getClass());
    
    @Override
    public boolean save(T entity) {
        throw new UnsupportedOperationException("请在各具体Service中实现");
    }
    
    @Override
    public boolean update(T entity) {
        throw new UnsupportedOperationException("请在各具体Service中实现");
    }
    
    @Override
    public boolean deleteById(ID id) {
        throw new UnsupportedOperationException("请在各具体Service中实现");
    }
    
    @Override
    public Optional<T> findById(ID id) {
        throw new UnsupportedOperationException("请在各具体Service中实现");
    }
    
    @Override
    public List<T> findAll() {
        throw new UnsupportedOperationException("请在各具体Service中实现");
    }
    
    @Override
    public com.campus.nav.model.PageResult<T> findByPage(com.campus.nav.model.PageQuery query) {
        throw new UnsupportedOperationException("请在各具体Service中实现");
    }
    
    @Override
    public long count() {
        throw new UnsupportedOperationException("请在各具体Service中实现");
    }
    
    /**
     * 验证实体是否有效
     */
    protected boolean validateEntity(T entity) {
        return entity != null;
    }
}