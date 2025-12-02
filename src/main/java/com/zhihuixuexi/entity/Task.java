package com.zhihuixuexi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 任务/作业实体类
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务标题
     */
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题长度不能超过200个字符")
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 任务描述
     */
    @Size(max = 500, message = "任务描述长度不能超过500个字符")
    @Column(length = 500)
    private String description;

    /**
     * 任务内容/要求（支持富文本）
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 所属课程
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * 所属章节（可选，如果为空则表示是课程级别的任务）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    /**
     * 创建者（机构）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    /**
     * 截止时间
     */
    @Column(name = "deadline")
    private LocalDateTime deadline;

    /**
     * 满分
     */
    @Column(nullable = false)
    private Integer maxScore = 100;

    /**
     * 是否已发布
     */
    @Column(nullable = false)
    private Boolean published = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

