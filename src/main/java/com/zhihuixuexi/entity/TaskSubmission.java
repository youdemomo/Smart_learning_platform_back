package com.zhihuixuexi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 任务提交实体类
 */
@Entity
@Table(name = "task_submissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"task_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属任务
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /**
     * 提交者（学生）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 提交内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 附件URL（多个附件用逗号分隔）
     */
    @Column(length = 1000)
    private String attachmentUrls;

    /**
     * 得分
     */
    @Column
    private Integer score;

    /**
     * 批改反馈
     */
    @Column(columnDefinition = "TEXT")
    private String feedback;

    /**
     * 状态：SUBMITTED-已提交, GRADED-已批改
     */
    @Column(nullable = false, length = 20)
    private String status = "SUBMITTED";

    /**
     * 提交时间
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * 批改时间
     */
    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

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

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}

