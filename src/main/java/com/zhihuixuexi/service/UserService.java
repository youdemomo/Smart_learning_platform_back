package com.zhihuixuexi.service;

import com.zhihuixuexi.dto.PageResponse;
import com.zhihuixuexi.dto.UserDTO;
import com.zhihuixuexi.dto.UserQueryRequest;
import com.zhihuixuexi.dto.UserUpdateRequest;
import com.zhihuixuexi.entity.User;
import com.zhihuixuexi.enums.UserRole;
import com.zhihuixuexi.repository.UserRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public PageResponse<UserDTO> getUserList(UserQueryRequest request) {
    try {
        // 参数校验
        validateRequest(request);

        // 构建查询条件
        Specification<User> spec = buildSpecification(request);

        // 构建分页和排序
        Pageable pageable = buildPageable(request);

        // 执行查询
        Page<User> page = userRepository.findAll(spec, pageable);

        // 转换为DTO
        List<UserDTO> userDTOs = convertToDTOList(page.getContent());

        // 构建响应
        return buildPageResponse(userDTOs, page, request);

    } catch (IllegalArgumentException e) {
        log.warn("用户列表查询参数错误: {}", e.getMessage());
        // 返回空结果而不是抛出异常
        return buildEmptyResponse(request);
    } catch (Exception e) {
        log.error("用户列表查询失败: {}", e.getMessage(), e);
        throw new RuntimeException("查询用户列表失败，请稍后重试", e);
    }
}

/**
 * 验证请求参数
 */
private void validateRequest(UserQueryRequest request) {
    if (request == null) {
        throw new IllegalArgumentException("用户查询参数不能为空");
    }

    // 分页参数校验（自动修正 + 提示友好）
    if (request.getPage() == null || request.getPage() < 1) {
        request.setPage(1);
    }

    if (request.getSize() == null || request.getSize() < 1) {
        request.setSize(10);
    }

    // 限制最大分页大小，避免过大请求导致性能问题
    if (request.getSize() > 100) {
        request.setSize(100);
    }

    // 字符串长度校验（支持 null 或空字符串）
    validateStringLength(cleanString(request.getUsername()), "用户名", 50);
    validateStringLength(cleanString(request.getEmail()), "邮箱", 100);
    validateStringLength(cleanString(request.getOrganization()), "机构名称", 100);
}

/**
 * 去除前后空白，如果全为空则返回 null
 */
private String cleanString(String value) {
    if (value == null) {
        return null;
    }
    String cleaned = value.trim();
    return cleaned.isEmpty() ? null : cleaned;
}


/**
 * 验证字符串长度
 */
private void validateStringLength(String value, String fieldName, int maxLength) {
    if (value != null && value.length() > maxLength) {
        throw new IllegalArgumentException(fieldName + "长度不能超过" + maxLength + "个字符");
    }
}

/**
 * 构建查询条件
 */
private Specification<User> buildSpecification(UserQueryRequest request) {
    return (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();

        // 字符串字段模糊查询（统一处理逻辑）
        processStringFieldQuery(predicates, request.getUsername(), "username", root, cb);
        processStringFieldQuery(predicates, request.getEmail(), "email", root, cb);
        processStringFieldQuery(predicates, request.getOrganization(), "organization", root, cb);

        // 枚举字段精确查询
        processEnumFieldQuery(predicates, request.getRole(), "role", root, cb);

        // 布尔字段精确查询
        processBooleanFieldQuery(predicates, request.getEmailVerified(), "emailVerified", root, cb);
        processBooleanFieldQuery(predicates, request.getEnabled(), "enabled", root, cb);
        processBooleanFieldQuery(predicates, request.getBanned(), "banned", root, cb);

        // 返回组合条件
        return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
    };
}

/**
 * 处理字符串字段的模糊查询（忽略大小写）
 */
private void processStringFieldQuery(List<Predicate> predicates, String fieldValue, String fieldName,
                                     Root<User> root, CriteriaBuilder cb) {
    if (StringUtils.hasText(fieldValue)) {
        String searchPattern = "%" + fieldValue.trim().toLowerCase() + "%";
        predicates.add(cb.like(cb.lower(root.get(fieldName)), searchPattern));
    }
}

/**
 * 处理枚举字段的精确查询
 */
private void processEnumFieldQuery(List<Predicate> predicates, Enum<?> fieldValue, String fieldName,
                                   Root<User> root, CriteriaBuilder cb) {
    if (fieldValue != null) {
        predicates.add(cb.equal(root.get(fieldName), fieldValue));
    }
}

/**
 * 处理布尔字段的精确查询
 */
private void processBooleanFieldQuery(List<Predicate> predicates, Boolean fieldValue, String fieldName,
                                      Root<User> root, CriteriaBuilder cb) {
    if (fieldValue != null) {
        predicates.add(cb.equal(root.get(fieldName), fieldValue));
    }
}

/**
 * 构建分页和排序
 */
private Pageable buildPageable(UserQueryRequest request) {
    // 默认按创建时间降序排列
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

    // 可以在这里添加其他排序逻辑，例如：
    // if (StringUtils.hasText(request.getSortBy())) {
    //     sort = Sort.by(Sort.Direction.fromString(request.getSortOrder()), request.getSortBy());
    // }

    return PageRequest.of(
            request.getPage() - 1, // Spring Data 页码从0开始
            request.getSize(),
            sort
    );
}

/**
 * 转换实体列表为DTO列表
 */
private List<UserDTO> convertToDTOList(List<User> users) {
    if (users == null || users.isEmpty()) {
        return Collections.emptyList();
    }

    return users.stream()
            .filter(Objects::nonNull) // 过滤空对象
            .map(this::convertToDTO)
            .collect(Collectors.toList());
}

/**
 * 构建分页响应
 */
private PageResponse<UserDTO> buildPageResponse(List<UserDTO> userDTOs, Page<User> page, UserQueryRequest request) {
    return new PageResponse<>(
            userDTOs,
            page.getTotalElements(),
            request.getPage(),
            request.getSize(),
            page.getTotalPages()
    );
}

/**
 * 构建空响应
 */
private PageResponse<UserDTO> buildEmptyResponse(UserQueryRequest request) {
    return new PageResponse<>(
            Collections.emptyList(),
            0L,
            request.getPage(),
            request.getSize(),
            0
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
