package com.zhihuixuexi.controller;

import com.zhihuixuexi.dto.ApiResponse;
import com.zhihuixuexi.dto.AuthResponse;
import com.zhihuixuexi.dto.LoginRequest;
import com.zhihuixuexi.dto.RegisterRequest;
import com.zhihuixuexi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ApiResponse.error("邮箱不能为空");
        }
        authService.sendVerificationCode(email);
        return ApiResponse.success("验证码已发送", null);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String verificationCode = request.getVerificationCode();
        if (verificationCode == null || verificationCode.isEmpty()) {
            return ApiResponse.error("验证码不能为空");
        }
        try {
            AuthResponse response = authService.register(request, verificationCode);
            return ApiResponse.success("注册成功，验证邮件已发送", response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 验证邮箱
     */
    @GetMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(
            @RequestParam String email,
            @RequestParam String code) {
        try {
            authService.verifyEmail(email, code);
            return ApiResponse.success("邮箱验证成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 重新发送验证邮件
     */
    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(@RequestParam String email) {
        try {
            authService.resendVerificationEmail(email);
            return ApiResponse.success("验证邮件已重新发送", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
