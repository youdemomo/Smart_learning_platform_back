package com.zhihuixuexi.dto;

import com.zhihuixuexi.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    
    private String type = "Bearer";
    
    private Long userId;
    
    private String username;
    
    private String email;
    
    private UserRole role;
    
    private Boolean emailVerified;

    public AuthResponse(String token, Long userId, String username, String email, UserRole role, Boolean emailVerified) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
    }
}
