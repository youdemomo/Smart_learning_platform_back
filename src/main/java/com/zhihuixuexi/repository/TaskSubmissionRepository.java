package com.zhihuixuexi.repository;

import com.zhihuixuexi.entity.TaskSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务提交Repository
 */
@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    /**
     * 根据任务ID查询提交列表（分页）
     */
    Page<TaskSubmission> findByTaskId(Long taskId, Pageable pageable);

    /**
     * 根据用户ID查询提交列表（分页）
     */
    Page<TaskSubmission> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据任务ID和用户ID查询提交记录
     */
    Optional<TaskSubmission> findByTaskIdAndUserId(Long taskId, Long userId);

    /**
     * 检查用户是否已提交任务
     */
    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    /**
     * 根据任务ID和状态查询提交列表
     */
    List<TaskSubmission> findByTaskIdAndStatus(Long taskId, String status);

    /**
     * 根据创建者ID统计待批改任务数
     */
    @Query("SELECT COUNT(ts) FROM TaskSubmission ts WHERE ts.task.creator.id = :creatorId AND ts.status = 'SUBMITTED'")
    Long countPendingGradingByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * 删除任务的所有提交记录
     */
    void deleteByTaskId(Long taskId);

    /**
     * 删除课程的所有任务提交（通过任务关联）
     */
    @Modifying
    @Query("DELETE FROM TaskSubmission ts WHERE ts.task.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);

    /**
     * 删除章节的所有任务提交（通过任务关联）
     */
    @Modifying
    @Query("DELETE FROM TaskSubmission ts WHERE ts.task.chapter.id = :chapterId")
    void deleteByChapterId(@Param("chapterId") Long chapterId);

    /**
     * 删除用户的所有任务提交
     */
    void deleteByUserId(Long userId);
}

