package com.zhihuixuexi.controller;

import com.zhihuixuexi.dto.ApiResponse;
import com.zhihuixuexi.dto.PageResponse;
import com.zhihuixuexi.dto.TaskDTO;
import com.zhihuixuexi.dto.TaskRequest;
import com.zhihuixuexi.service.TaskService;
import com.zhihuixuexi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/tasks")
@CrossOrigin
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final JwtUtil jwtUtil;

    /**
     * 创建任务
     */
    @PostMapping
    public ApiResponse<TaskDTO> createTask(
            @RequestBody TaskRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        try {
            TaskDTO dto = taskService.createTask(request, userId);
            return ApiResponse.success("创建成功", dto);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public ApiResponse<TaskDTO> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        try {
            TaskDTO dto = taskService.updateTask(id, request, userId);
            return ApiResponse.success("更新成功", dto);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTask(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        try {
            taskService.deleteTask(id, userId);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public ApiResponse<TaskDTO> getTaskById(@PathVariable Long id) {
        try {
            TaskDTO dto = taskService.getTaskById(id);
            return ApiResponse.success(dto);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取机构的任务列表
     */
    @GetMapping("/my")
    public ApiResponse<PageResponse<TaskDTO>> getMyTasks(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        PageResponse<TaskDTO> response = taskService.getMyTasks(userId, courseId, published, page, size);
        return ApiResponse.success(response);
    }

    /**
     * 获取课程的任务列表
     */
    @GetMapping("/course/{courseId}")
    public ApiResponse<List<TaskDTO>> getCourseTasks(@PathVariable Long courseId) {
        List<TaskDTO> tasks = taskService.getCourseTasks(courseId);
        return ApiResponse.success(tasks);
    }

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}

