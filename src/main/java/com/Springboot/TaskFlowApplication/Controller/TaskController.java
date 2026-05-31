package com.Springboot.TaskFlowApplication.Controller;

import com.Springboot.TaskFlowApplication.DTOs.ApiResponse;
import com.Springboot.TaskFlowApplication.DTOs.TaskDto;
import com.Springboot.TaskFlowApplication.DTOs.TaskSummaryDto;
import com.Springboot.TaskFlowApplication.Entity.Task;
import com.Springboot.TaskFlowApplication.Entity.TaskHistory;
import com.Springboot.TaskFlowApplication.Entity.User;
import com.Springboot.TaskFlowApplication.service.AuthService;
import com.Springboot.TaskFlowApplication.service.BlockchainService;
import com.Springboot.TaskFlowApplication.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {

    private final TaskService taskService;
    private final AuthService authService;
    private final BlockchainService blockchainService;  // ADD THIS

    // UPDATE CONSTRUCTOR to include BlockchainService
    public TaskController(TaskService taskService, AuthService authService, BlockchainService blockchainService) {
        this.taskService = taskService;
        this.authService = authService;
        this.blockchainService = blockchainService;  // ADD THIS
    }

    @GetMapping("/summary")
    @Operation(summary = "Get task summary with AI insights")
    public ResponseEntity<ApiResponse<TaskSummaryDto>> getTaskSummary() {
        User currentUser = authService.getCurrentUser();
        TaskSummaryDto summary = taskService.getTaskSummary(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Summary retrieved successfully", summary));
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskDto.Response>> createTask(
            @Valid @RequestBody TaskDto.CreateRequest request) {
        User currentUser = authService.getCurrentUser();
        TaskDto.Response response = taskService.createTask(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all tasks for current user with pagination and filters")
    public ResponseEntity<ApiResponse<TaskDto.PagedResponse>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Task.Status status,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(required = false) String search) {
        User currentUser = authService.getCurrentUser();
        TaskDto.PagedResponse response = taskService.getTasks(currentUser, page, size, status, priority, search);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", response));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get a specific task by ID")
    public ResponseEntity<ApiResponse<TaskDto.Response>> getTaskById(@PathVariable Long taskId) {
        User currentUser = authService.getCurrentUser();
        TaskDto.Response response = taskService.getTaskById(taskId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", response));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskDto.Response>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskDto.UpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        TaskDto.Response response = taskService.updateTask(taskId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {
        User currentUser = authService.getCurrentUser();
        taskService.deleteTask(taskId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Update task status")
    public ResponseEntity<ApiResponse<TaskDto.Response>> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody TaskDto.StatusUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        TaskDto.Response response = taskService.updateTaskStatus(taskId, request.getStatus(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", response));
    }
    
    @GetMapping("/{taskId}/history")
    @Operation(summary = "Get blockchain history for a task")
    public ResponseEntity<ApiResponse<List<TaskHistory>>> getTaskHistory(@PathVariable Long taskId) {
        User currentUser = authService.getCurrentUser();
        // Verify ownership first
        taskService.getTaskById(taskId, currentUser);
        List<TaskHistory> history = blockchainService.getTaskHistory(taskId);
        return ResponseEntity.ok(ApiResponse.success("History retrieved successfully", history));
    }
}