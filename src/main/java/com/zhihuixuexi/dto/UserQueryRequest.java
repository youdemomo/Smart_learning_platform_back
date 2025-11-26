package com.zhihuixuexi.dto;

import com.zhihuixuexi.enums.UserRole;
import lombok.Data;

/**
 * 用户查询请求DTO
 */
@Data
public class UserQueryRequest {
    
    private String username;
    
    private String email;
    
    private String organization;
    
    private UserRole role;
    
    private Boolean emailVerified;
    
    private Boolean enabled;
    
    private Boolean banned;
    
    private Integer page = 1;
    
    private Integer size = 10;
}
