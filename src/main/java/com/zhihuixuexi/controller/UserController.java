package com.zhihuixuexi.controller;

import com.zhihuixuexi.dto.*;
import com.zhihuixuexi.enums.UserRole;
import com.zhihuixuexi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public ApiResponse<PageResponse<UserDTO>> getUserList(UserQueryRequest request) {
        PageResponse<UserDTO> response = userService.getUserList(request);
        return ApiResponse.success(response);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ApiResponse<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        UserDTO user = userService.updateUser(id, request);
        return ApiResponse.success("更新成功", user);
    }

    /**
     * 封禁用户
     */
    @PutMapping("/{id}/ban")
    public ApiResponse<Void> banUser(@PathVariable Long id) {
        userService.banUser(id);
        return ApiResponse.success("封禁成功", null);
    }

    /**
     * 解封用户
     */
    @PutMapping("/{id}/unban")
    public ApiResponse<Void> unbanUser(@PathVariable Long id) {
        userService.unbanUser(id);
        return ApiResponse.success("解封成功", null);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<UserDTO> createUser(@RequestBody UserUpdateRequest request, @RequestParam UserRole role) {
        UserDTO user = userService.createUser(request, role);
        return ApiResponse.success("创建成功", user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功", null);
    }
}
