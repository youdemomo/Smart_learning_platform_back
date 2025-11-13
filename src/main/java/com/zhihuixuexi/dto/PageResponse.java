package com.zhihuixuexi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> records;
    
    private Long total;
    
    private Integer page;
    
    private Integer size;
    
    private Integer totalPages;
}
