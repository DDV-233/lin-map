package com.campus.nav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> data;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> data, Long total, PageQuery query) {
        if (query == null) {
            query = PageQuery.defaultQuery();
        }
        
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int totalPages = (int) Math.ceil((double) total / pageSize);
        
        return PageResult.<T>builder()
                .data(data)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .hasPrevious(pageNum > 1)
                .hasNext(pageNum < totalPages)
                .build();
    }
}