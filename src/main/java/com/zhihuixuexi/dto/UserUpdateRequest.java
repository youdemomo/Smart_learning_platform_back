package com.zhihuixuexi.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * 用户更新请求DTO
 */
@Data
public class UserUpdateRequest {
    
    private String username;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String phone;
    
    private String organization;
    
    private String address;
    
    private Boolean enabled;
    
    private Boolean banned;
}
