package com.zhihuixuexi.service;

import com.zhihuixuexi.dto.PageResponse;
import com.zhihuixuexi.dto.UserDTO;
import com.zhihuixuexi.dto.UserQueryRequest;
import com.zhihuixuexi.dto.UserUpdateRequest;
import com.zhihuixuexi.entity.User;
import com.zhihuixuexi.enums.UserRole;
import com.zhihuixuexi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     */
    public PageResponse<UserDTO> getUserList(UserQueryRequest request) {
        // 构建查询条件
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 用户名模糊查询
            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + request.getUsername() + "%"));
            }

            // 邮箱模糊查询
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                predicates.add(cb.like(root.get("email"), "%" + request.getEmail() + "%"));
            }

            // 机构名称模糊查询
            if (request.getOrganization() != null && !request.getOrganization().isEmpty()) {
                predicates.add(cb.like(root.get("organization"), "%" + request.getOrganization() + "%"));
            }

            // 角色精确查询
            if (request.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), request.getRole()));
            }

            // 邮箱验证状态
            if (request.getEmailVerified() != null) {
                predicates.add(cb.equal(root.get("emailVerified"), request.getEmailVerified()));
            }

            // 启用状态
            if (request.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), request.getEnabled()));
            }

            // 封禁状态
            if (request.getBanned() != null) {
                predicates.add(cb.equal(root.get("banned"), request.getBanned()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 分页和排序
        Pageable pageable = PageRequest.of(
            request.getPage() - 1,
            request.getSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> page = userRepository.findAll(spec, pageable);

        // 转换为DTO
        List<UserDTO> userDTOs = page.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return new PageResponse<>(
            userDTOs,
            page.getTotalElements(),
            request.getPage(),
            request.getSize(),
            page.getTotalPages()
        );
    }

    /**
     * 根据ID获取用户详情
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新字段
        if (request.getUsername() != null) {
            // 检查用户名是否已被使用
            if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            // 检查邮箱是否已被使用
            if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已被注册");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getOrganization() != null) {
            user.setOrganization(request.getOrganization());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getBanned() != null) {
            user.setBanned(request.getBanned());
        }

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    /**
     * 封禁用户
     */
    @Transactional
    public void banUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setBanned(true);
        userRepository.save(user);
    }

    /**
     * 解封用户
     */
    @Transactional
    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setBanned(false);
        userRepository.save(user);
    }

    /**
     * 创建用户
     */
    @Transactional
    public UserDTO createUser(UserUpdateRequest request, UserRole role) {
        // 检查用户名是否已存在
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setPhone(request.getPhone());
        user.setOrganization(request.getOrganization());
        user.setAddress(request.getAddress());
        user.setEmailVerified(true); // 管理员添加的用户默认邮箱已验证
        user.setEnabled(true);
        user.setBanned(false);

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 转换为DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
