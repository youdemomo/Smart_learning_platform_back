package com.zhihuixuexi.dto;

import com.zhihuixuexi.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息DTO（不包含密码等敏感信息）
 */
@Data
public class UserDTO {
    
    private Long id;
    
    private String username;
    
    private String email;
    
    private UserRole role;
    
    private String phone;
    
    private String organization;
    
    private String address;
    
    private String avatar;
    
    private Boolean emailVerified;
    
    private Boolean enabled;
    
    private Boolean banned;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
