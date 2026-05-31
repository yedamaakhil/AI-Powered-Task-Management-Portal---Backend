package com.Springboot.TaskFlowApplication.service;

import com.Springboot.TaskFlowApplication.DTOs.TaskDto;
import com.Springboot.TaskFlowApplication.DTOs.TaskSummaryDto;
import com.Springboot.TaskFlowApplication.Entity.Task;
import com.Springboot.TaskFlowApplication.Entity.User;
import com.Springboot.TaskFlowApplication.Exception.ResourceNotFoundException;
import com.Springboot.TaskFlowApplication.Exception.UnauthorizedException;
import com.Springboot.TaskFlowApplication.Repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final BlockchainService blockchainService;

    public TaskService(TaskRepository taskRepository, BlockchainService blockchainService) {
        this.taskRepository = taskRepository;
        this.blockchainService = blockchainService;
    }

    @Transactional
    public TaskDto.Response createTask(TaskDto.CreateRequest request, User user) {
        log.info("Creating new task for user: {}", user.getUsername());
        
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM);
        task.setStatus(Task.Status.TODO);
        task.setDueDate(request.getDueDate());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setUser(user);

        task = taskRepository.save(task);
        log.info("Task saved with ID: {}", task.getId());

        // Record blockchain event - THIS IS CRITICAL
        try {
            blockchainService.recordTaskEvent(task, "CREATED", null, task.getStatus().name(), user.getUsername());
            log.info("✅ Blockchain CREATED record created for task: {}", task.getId());
        } catch (Exception e) {
            log.error("❌ Failed to record blockchain event: {}", e.getMessage());
        }

        log.info("Task created: {} by user: {}", task.getId(), user.getUsername());
        return TaskDto.Response.fromEntity(task);
    }

    @Transactional
    public TaskDto.Response updateTask(Long taskId, TaskDto.UpdateRequest request, User user) {
        Task task = getTaskAndVerifyOwnership(taskId, user);
        String previousStatus = task.getStatus().name();
        boolean statusChanged = false;

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getEstimatedHours() != null) task.setEstimatedHours(request.getEstimatedHours());
        
        if (request.getStatus() != null && !request.getStatus().equals(task.getStatus())) {
            task.setStatus(request.getStatus());
            statusChanged = true;
        }

        task = taskRepository.save(task);

        if (statusChanged) {
            try {
                blockchainService.recordTaskEvent(task, "STATUS_UPDATED", previousStatus, task.getStatus().name(), user.getUsername());
                log.info("✅ Blockchain STATUS_UPDATED record created for task: {}", taskId);
            } catch (Exception e) {
                log.error("❌ Failed to record blockchain event: {}", e.getMessage());
            }
        }

        log.info("Task updated: {} by user: {}", taskId, user.getUsername());
        return TaskDto.Response.fromEntity(task);
    }

    @Transactional
    public void deleteTask(Long taskId, User user) {
        Task task = getTaskAndVerifyOwnership(taskId, user);
        
        try {
            blockchainService.recordTaskEvent(task, "DELETED", task.getStatus().name(), null, user.getUsername());
            log.info("✅ Blockchain DELETED record created for task: {}", taskId);
        } catch (Exception e) {
            log.error("❌ Failed to record blockchain event: {}", e.getMessage());
        }
        
        taskRepository.delete(task);
        log.info("Task deleted: {} by user: {}", taskId, user.getUsername());
    }

    @Transactional(readOnly = true)
    public TaskDto.PagedResponse getTasks(User user, int page, int size, Task.Status status, Task.Priority priority, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> taskPage;

        if (status != null || priority != null || (search != null && !search.isBlank())) {
            taskPage = taskRepository.findByFilters(user.getId(), status, priority, search, pageable);
        } else {
            taskPage = taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        }

        List<TaskDto.Response> content = taskPage.getContent().stream()
                .map(TaskDto.Response::fromEntity)
                .collect(Collectors.toList());

        TaskDto.PagedResponse response = new TaskDto.PagedResponse();
        response.setContent(content);
        response.setPageNumber(taskPage.getNumber());
        response.setPageSize(taskPage.getSize());
        response.setTotalElements(taskPage.getTotalElements());
        response.setTotalPages(taskPage.getTotalPages());
        response.setLast(taskPage.isLast());

        return response;
    }

    @Transactional(readOnly = true)
    public TaskDto.Response getTaskById(Long taskId, User user) {
        Task task = getTaskAndVerifyOwnership(taskId, user);
        return TaskDto.Response.fromEntity(task);
    }

    @Transactional
    public TaskDto.Response updateTaskStatus(Long taskId, Task.Status newStatus, User user) {
        TaskDto.UpdateRequest request = new TaskDto.UpdateRequest();
        request.setStatus(newStatus);
        return updateTask(taskId, request, user);
    }

    @Transactional
    public TaskDto.Response applyAiDescription(Long taskId, String aiDescription, String priority, Double hours, User user) {
        Task task = getTaskAndVerifyOwnership(taskId, user);
        task.setAiGeneratedDescription(aiDescription);
        
        if (task.getDescription() == null || task.getDescription().isBlank()) {
            task.setDescription(aiDescription);
        }
        
        if (priority != null) {
            try {
                task.setPriority(Task.Priority.valueOf(priority.toUpperCase()));
            } catch (Exception ignored) {}
        }
        
        if (hours != null) {
            task.setEstimatedHours(hours);
        }
        
        task = taskRepository.save(task);
        return TaskDto.Response.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public TaskSummaryDto getTaskSummary(User user) {
        List<Task> userTasks = taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        long totalTasks = userTasks.size();
        long inProgressTasks = 0;
        long completedTasks = 0;
        long overdueTasks = 0;
        
        for (Task task : userTasks) {
            if (task.getStatus() == Task.Status.IN_PROGRESS) {
                inProgressTasks++;
            } else if (task.getStatus() == Task.Status.DONE) {
                completedTasks++;
            }
            
            if (task.getDueDate() != null && 
                task.getDueDate().isBefore(LocalDateTime.now()) && 
                task.getStatus() != Task.Status.DONE) {
                overdueTasks++;
            }
        }
        
        int productivityPercentage = totalTasks > 0 ? (int)((completedTasks * 100.0) / totalTasks) : 0;
        String productivityScore = productivityPercentage + "%";
        String aiSummary = generateAISummary(totalTasks, completedTasks, overdueTasks);
        
        TaskSummaryDto summary = new TaskSummaryDto();
        summary.setTotalTasks(totalTasks);
        summary.setInProgressTasks(inProgressTasks);
        summary.setCompletedTasks(completedTasks);
        summary.setOverdueTasks(overdueTasks);
        summary.setProductivityScore(productivityScore);
        summary.setAiSummary(aiSummary);
        summary.setAiGenerated(true);
        
        return summary;
    }

    private String generateAISummary(long totalTasks, long completedTasks, long overdueTasks) {
        if (totalTasks == 0) {
            return "Create your first task to get AI-powered insights!";
        }
        double completionRate = (completedTasks * 100.0) / totalTasks;
        if (completionRate == 100) {
            return "Excellent! You've completed all your tasks!";
        } else if (completionRate >= 75) {
            return "Great progress! Keep up the momentum!";
        } else if (completionRate >= 50) {
            return "Good progress! You're halfway there!";
        } else if (overdueTasks > 0) {
            return "You have " + overdueTasks + " overdue task(s). Prioritize these first!";
        } else {
            return "Keep going! Complete more tasks to improve your productivity score.";
        }
    }

    private Task getTaskAndVerifyOwnership(Long taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        
        if (!task.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to access this task");
        }
        return task;
    }

    public List<Task> getAllUserTasks(User user) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}