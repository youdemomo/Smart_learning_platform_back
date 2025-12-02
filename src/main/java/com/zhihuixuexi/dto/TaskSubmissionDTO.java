package com.zhihuixuexi.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务提交DTO
 */
@Data
public class TaskSubmissionDTO {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long userId;
    private String username;
    private String content;
    private String attachmentUrls;
    private Integer score;
    private String feedback;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;
}

