package com.zhihuixuexi.service;

import com.zhihuixuexi.dto.AuthResponse;
import com.zhihuixuexi.dto.LoginRequest;
import com.zhihuixuexi.dto.RegisterRequest;
import com.zhihuixuexi.entity.User;
import com.zhihuixuexi.repository.UserRepository;
import com.zhihuixuexi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 认证服务类
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    // 简单的内存缓存存储验证码（生产环境应该用Redis）
    private final java.util.Map<String, VerificationCodeData> verificationCodeCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    private static class VerificationCodeData {
        String code;
        LocalDateTime expiry;
        
        VerificationCodeData(String code, LocalDateTime expiry) {
            this.code = code;
            this.expiry = expiry;
        }
    }

    /**
     * 发送验证码（不创建用户）
     */
    public String sendVerificationCode(String email) {
        // 检查邮箱是否已注册
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("该邮箱已被注册");
        }
        
        // 生成验证码
        String verificationCode = generateVerificationCode();
        
        // 存储到缓存中（24小时过期）
        verificationCodeCache.put(email, new VerificationCodeData(
            verificationCode,
            LocalDateTime.now().plusHours(24)
        ));
        
        // 发送验证邮件
        emailService.sendVerificationEmail(email, "用户", verificationCode);
        
        return verificationCode;
    }

    /**
     * 用户注册（带验证码验证）
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, String verificationCode) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 验证邮箱验证码
        VerificationCodeData codeData = verificationCodeCache.get(request.getEmail());
        if (codeData == null) {
            throw new RuntimeException("请先获取验证码");
        }
        
        if (!codeData.code.equals(verificationCode)) {
            throw new RuntimeException("验证码错误");
        }
        
        if (codeData.expiry.isBefore(LocalDateTime.now())) {
            verificationCodeCache.remove(request.getEmail());
            throw new RuntimeException("验证码已过期");
        }

        // 验证通过，删除缓存中的验证码
        verificationCodeCache.remove(request.getEmail());

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setOrganization(request.getOrganization());
        user.setAddress(request.getAddress());
        user.setEmailVerified(true); // 验证码验证通过，直接标记为已验证
        user.setEnabled(true);
        user.setBanned(false);

        // 保存用户
        user = userRepository.save(user);

        // 生成JWT Token
        String token = jwtUtil.generateToken(
            user.getUsername(),
            user.getId(),
            user.getRole().name()
        );

        return new AuthResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getEmailVerified()
        );
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("用户名不存在"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查账号是否被封禁
        if (user.getBanned()) {
            throw new RuntimeException("账号已被封禁，请联系管理员");
        }

        // 检查账号是否启用
        if (!user.getEnabled()) {
            throw new RuntimeException("账号未启用");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(
            user.getUsername(),
            user.getId(),
            user.getRole().name()
        );

        return new AuthResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getEmailVerified()
        );
    }

    /**
     * 验证邮箱
     */
    @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmailAndVerificationCode(email, code)
            .orElseThrow(() -> new RuntimeException("验证码无效"));

        // 检查验证码是否过期
        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("验证码已过期");
        }

        // 更新邮箱验证状态
        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);
    }

    /**
     * 重新发送验证邮件
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("邮箱不存在"));

        if (user.getEmailVerified()) {
            throw new RuntimeException("邮箱已验证");
        }

        // 生成新的验证码
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // 发送验证邮件
        emailService.sendVerificationEmail(email, user.getUsername(), verificationCode);
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
