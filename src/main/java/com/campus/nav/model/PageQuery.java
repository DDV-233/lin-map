package com.campus.nav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询参数类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery {
    /**
     * 页码（从1开始）
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 排序字段
     */
    private String sortBy;
    
    /**
     * 排序方向：ASC/DESC
     */
    private String sortOrder;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 获取偏移量
     */
    public Integer getOffset() {
        if (pageNum == null || pageSize == null) {
            return 0;
        }
        return (pageNum - 1) * pageSize;
    }
    
    /**
     * 创建默认分页参数
     */
    public static PageQuery defaultQuery() {
        return PageQuery.builder()
                .pageNum(1)
                .pageSize(10)
                .sortOrder("DESC")
                .build();
    }
}