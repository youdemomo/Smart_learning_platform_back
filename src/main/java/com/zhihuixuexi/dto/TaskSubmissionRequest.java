package com.zhihuixuexi.dto;

import lombok.Data;

/**
 * 任务提交请求DTO
 */
@Data
public class TaskSubmissionRequest {
    private String content;
    private String attachmentUrls;
}

