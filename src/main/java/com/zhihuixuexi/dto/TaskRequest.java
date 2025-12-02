package com.zhihuixuexi.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务请求DTO
 */
@Data
public class TaskRequest {
    private String title;
    private String description;
    private String content;
    private Long courseId;
    private Long chapterId;
    private LocalDateTime deadline;
    private Integer maxScore;
    private Boolean published;
}

