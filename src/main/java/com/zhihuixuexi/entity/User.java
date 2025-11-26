package com.zhihuixuexi.entity;

import com.zhihuixuexi.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（加密后）
     */
    @NotBlank(message = "密码不能为空")
    @Column(nullable = false)
    private String password;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 用户角色
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatar;

    /**
     * 联系方式
     */
    @Column(length = 20)
    private String phone;

    /**
     * 学校/机构名称
     */
    @Column(length = 100)
    private String organization;

    /**
     * 地址
     */
    @Column(length = 200)
    private String address;

    /**
     * 邮箱是否已验证
     */
    @Column(nullable = false)
    private Boolean emailVerified = false;

    /**
     * 邮箱验证码
     */
    @Column(length = 6)
    private String verificationCode;

    /**
     * 验证码过期时间
     */
    private LocalDateTime verificationCodeExpiry;

    /**
     * 账号是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 账号是否被封禁
     */
    @Column(nullable = false)
    private Boolean banned = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
