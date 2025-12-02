package com.zhihuixuexi.service;

import com.zhihuixuexi.dto.PageResponse;
import com.zhihuixuexi.dto.TaskDTO;
import com.zhihuixuexi.dto.TaskRequest;
import com.zhihuixuexi.entity.Chapter;
import com.zhihuixuexi.entity.Course;
import com.zhihuixuexi.entity.Task;
import com.zhihuixuexi.entity.User;
import com.zhihuixuexi.repository.ChapterRepository;
import com.zhihuixuexi.repository.CourseRepository;
import com.zhihuixuexi.repository.TaskRepository;
import com.zhihuixuexi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务服务
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;

    /**
     * 创建任务
     */
    @Transactional
    public TaskDTO createTask(TaskRequest request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 验证课程是否属于该机构
        if (!course.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("无权操作该课程");
        }

        Task task = new Task();
        BeanUtils.copyProperties(request, task);
        task.setCourse(course);
        task.setCreator(creator);

        // 如果指定了章节，验证章节是否属于该课程
        if (request.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new RuntimeException("章节不存在"));
            if (!chapter.getCourse().getId().equals(request.getCourseId())) {
                throw new RuntimeException("章节不属于该课程");
            }
            task.setChapter(chapter);
        }

        Task saved = taskRepository.save(task);
        return convertToDTO(saved);
    }

    /**
     * 更新任务
     */
    @Transactional
    public TaskDTO updateTask(Long taskId, TaskRequest request, Long creatorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        // 验证权限
        if (!task.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("无权操作该任务");
        }

        // 更新字段
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setContent(request.getContent());
        task.setDeadline(request.getDeadline());
        task.setMaxScore(request.getMaxScore());
        task.setPublished(request.getPublished());

        // 如果修改了章节
        if (request.getChapterId() != null && !request.getChapterId().equals(
                task.getChapter() != null ? task.getChapter().getId() : null)) {
            Chapter chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new RuntimeException("章节不存在"));
            if (!chapter.getCourse().getId().equals(task.getCourse().getId())) {
                throw new RuntimeException("章节不属于该课程");
            }
            task.setChapter(chapter);
        } else if (request.getChapterId() == null) {
            task.setChapter(null);
        }

        Task saved = taskRepository.save(task);
        return convertToDTO(saved);
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(Long taskId, Long creatorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        // 验证权限
        if (!task.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("无权操作该任务");
        }

        taskRepository.delete(task);
    }

    /**
     * 根据ID获取任务
     */
    public TaskDTO getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        return convertToDTO(task);
    }

    /**
     * 获取机构的任务列表
     */
    public PageResponse<TaskDTO> getMyTasks(Long creatorId, Long courseId, Boolean published, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> taskPage;

        if (published != null && courseId != null) {
            // 同时按课程ID和发布状态筛选
            taskPage = taskRepository.findByCreatorIdAndCourseIdAndPublished(creatorId, courseId, published, pageable);
        } else if (published != null) {
            // 只按发布状态筛选
            taskPage = taskRepository.findByCreatorIdAndPublished(creatorId, published, pageable);
        } else if (courseId != null) {
            // 只按课程ID筛选
            taskPage = taskRepository.findByCreatorIdAndCourseId(creatorId, courseId, pageable);
        } else {
            // 查询所有任务
            taskPage = taskRepository.findByCreatorId(creatorId, pageable);
        }

        List<TaskDTO> dtos = taskPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageResponse<TaskDTO> response = new PageResponse<>();
        response.setRecords(dtos);
        response.setTotal(taskPage.getTotalElements());
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages(taskPage.getTotalPages());

        return response;
    }

    /**
     * 获取课程的任务列表（只返回已发布的任务）
     */
    public List<TaskDTO> getCourseTasks(Long courseId) {
        List<Task> tasks = taskRepository.findByCourseIdAndPublishedTrue(courseId);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        BeanUtils.copyProperties(task, dto);
        dto.setCourseId(task.getCourse().getId());
        dto.setCourseTitle(task.getCourse().getTitle());
        dto.setCreatorId(task.getCreator().getId());
        dto.setCreatorName(task.getCreator().getUsername());
        if (task.getChapter() != null) {
            dto.setChapterId(task.getChapter().getId());
            dto.setChapterTitle(task.getChapter().getTitle());
        }
        return dto;
    }
}

