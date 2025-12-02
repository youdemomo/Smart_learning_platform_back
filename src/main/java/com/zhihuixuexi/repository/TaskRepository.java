package com.zhihuixuexi.repository;

import com.zhihuixuexi.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务Repository
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * 根据课程ID查询任务列表（分页）
     */
    Page<Task> findByCourseId(Long courseId, Pageable pageable);

    /**
     * 根据课程ID查询已发布的任务列表
     */
    List<Task> findByCourseIdAndPublishedTrue(Long courseId);

    /**
     * 根据课程ID和章节ID查询任务列表
     */
    List<Task> findByCourseIdAndChapterId(Long courseId, Long chapterId);

    /**
     * 根据创建者ID查询任务列表（分页）
     */
    Page<Task> findByCreatorId(Long creatorId, Pageable pageable);

    /**
     * 根据创建者ID和课程ID查询任务列表（分页）
     */
    Page<Task> findByCreatorIdAndCourseId(Long creatorId, Long courseId, Pageable pageable);

    /**
     * 根据创建者ID和发布状态查询任务列表（分页）
     */
    Page<Task> findByCreatorIdAndPublished(Long creatorId, Boolean published, Pageable pageable);

    /**
     * 根据创建者ID、课程ID和发布状态查询任务列表（分页）
     */
    Page<Task> findByCreatorIdAndCourseIdAndPublished(Long creatorId, Long courseId, Boolean published, Pageable pageable);

    /**
     * 统计创建者的任务数量
     */
    Long countByCreatorId(Long creatorId);

    /**
     * 删除课程的所有任务
     */
    void deleteByCourseId(Long courseId);

    /**
     * 删除章节的所有任务
     */
    void deleteByChapterId(Long chapterId);

    /**
     * 删除创建者的所有任务
     */
    void deleteByCreatorId(Long creatorId);
}

