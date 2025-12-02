package com.zhihuixuexi.service;

import com.zhihuixuexi.dto.GradeTaskRequest;
import com.zhihuixuexi.dto.PageResponse;
import com.zhihuixuexi.dto.TaskSubmissionDTO;
import com.zhihuixuexi.dto.TaskSubmissionRequest;
import com.zhihuixuexi.entity.Task;
import com.zhihuixuexi.entity.TaskSubmission;
import com.zhihuixuexi.entity.User;
import com.zhihuixuexi.repository.TaskRepository;
import com.zhihuixuexi.repository.TaskSubmissionRepository;
import com.zhihuixuexi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务提交服务
 */
@Service
@RequiredArgsConstructor
public class TaskSubmissionService {

    private final TaskSubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * 提交任务
     */
    @Transactional
    public TaskSubmissionDTO submitTask(Long taskId, TaskSubmissionRequest request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        // 检查任务是否已发布
        if (!task.getPublished()) {
            throw new RuntimeException("任务尚未发布");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查是否已提交
        TaskSubmission submission = submissionRepository.findByTaskIdAndUserId(taskId, userId)
                .orElse(null);

        if (submission == null) {
            submission = new TaskSubmission();
            submission.setTask(task);
            submission.setUser(user);
        }

        submission.setContent(request.getContent());
        submission.setAttachmentUrls(request.getAttachmentUrls());
        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(LocalDateTime.now());

        TaskSubmission saved = submissionRepository.save(submission);
        return convertToDTO(saved);
    }

    /**
     * 批改任务
     */
    @Transactional
    public TaskSubmissionDTO gradeTask(Long submissionId, GradeTaskRequest request, Long creatorId) {
        TaskSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交记录不存在"));

        // 验证权限：只有任务创建者可以批改
        if (!submission.getTask().getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("无权批改该任务");
        }

        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus("GRADED");
        submission.setGradedAt(LocalDateTime.now());

        TaskSubmission saved = submissionRepository.save(submission);
        return convertToDTO(saved);
    }

    /**
     * 获取任务的提交列表
     */
    public PageResponse<TaskSubmissionDTO> getTaskSubmissions(Long taskId, Long creatorId, int page, int size) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        // 验证权限
        if (!task.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("无权查看该任务的提交");
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<TaskSubmission> submissionPage = submissionRepository.findByTaskId(taskId, pageable);

        List<TaskSubmissionDTO> dtos = submissionPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageResponse<TaskSubmissionDTO> response = new PageResponse<>();
        response.setRecords(dtos);
        response.setTotal(submissionPage.getTotalElements());
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages(submissionPage.getTotalPages());

        return response;
    }

    /**
     * 获取用户的提交列表
     */
    public PageResponse<TaskSubmissionDTO> getUserSubmissions(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<TaskSubmission> submissionPage = submissionRepository.findByUserId(userId, pageable);

        List<TaskSubmissionDTO> dtos = submissionPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageResponse<TaskSubmissionDTO> response = new PageResponse<>();
        response.setRecords(dtos);
        response.setTotal(submissionPage.getTotalElements());
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages(submissionPage.getTotalPages());

        return response;
    }

    /**
     * 获取提交详情
     */
    public TaskSubmissionDTO getSubmissionById(Long submissionId) {
        TaskSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交记录不存在"));
        return convertToDTO(submission);
    }

    /**
     * 转换为DTO
     */
    private TaskSubmissionDTO convertToDTO(TaskSubmission submission) {
        TaskSubmissionDTO dto = new TaskSubmissionDTO();
        BeanUtils.copyProperties(submission, dto);
        dto.setTaskId(submission.getTask().getId());
        dto.setTaskTitle(submission.getTask().getTitle());
        dto.setUserId(submission.getUser().getId());
        dto.setUsername(submission.getUser().getUsername());
        return dto;
    }
}

