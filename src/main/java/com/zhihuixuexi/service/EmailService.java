package com.zhihuixuexi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 邮件服务类
 */
@Service
@Slf4j
public class EmailService {

    /**
     * 发送验证邮件（开发环境：直接在控制台输出验证码）
     */
    public void sendVerificationEmail(String to, String username, String verificationCode) {
        log.info("=".repeat(60));
        log.info("📧 邮箱验证码");
        log.info("=".repeat(60));
        log.info("收件人: {}", to);
        log.info("用户名: {}", username);
        log.info("验证码: {}", verificationCode);
        log.info("有效期: 24小时");
        log.info("=".repeat(60));
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 邮箱验证码");
        System.out.println("=".repeat(60));
        System.out.println("收件人: " + to);
        System.out.println("用户名: " + username);
        System.out.println("验证码: " + verificationCode);
        System.out.println("有效期: 24小时");
        System.out.println("=".repeat(60) + "\n");
    }
}
