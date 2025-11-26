package com.zhihuixuexi.repository;

import com.zhihuixuexi.entity.User;
import com.zhihuixuexi.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    Boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    Boolean existsByEmail(String email);

    /**
     * 根据角色查找用户列表
     */
    List<User> findByRole(UserRole role);

    /**
     * 根据邮箱和验证码查找用户
     */
    Optional<User> findByEmailAndVerificationCode(String email, String verificationCode);
}
