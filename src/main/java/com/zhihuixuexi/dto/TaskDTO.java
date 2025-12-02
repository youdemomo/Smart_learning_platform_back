package com.zhihuixuexi.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务DTO
 */
@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String content;
    private Long courseId;
    private String courseTitle;
    private Long chapterId;
    private String chapterTitle;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime deadline;
    private Integer maxScore;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

