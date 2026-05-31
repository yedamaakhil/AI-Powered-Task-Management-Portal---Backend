package com.Springboot.TaskFlowApplication.Controller;

import com.Springboot.TaskFlowApplication.DTOs.AiDto;
import com.Springboot.TaskFlowApplication.DTOs.ApiResponse;
import com.Springboot.TaskFlowApplication.Entity.User;
import com.Springboot.TaskFlowApplication.service.AiService;
import com.Springboot.TaskFlowApplication.service.AuthService;
import com.Springboot.TaskFlowApplication.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Features", description = "APIs for AI-powered features")
public class AiController {

    private final AiService aiService;
    private final AuthService authService;
    private final TaskService taskService;

    public AiController(AiService aiService, AuthService authService, TaskService taskService) {
        this.aiService = aiService;
        this.authService = authService;
        this.taskService = taskService;
    }

    @PostMapping("/generate-description")
    @Operation(summary = "Generate task description using AI")
    public ResponseEntity<ApiResponse<AiDto.GenerateResponse>> generateDescription(
            @RequestBody AiDto.GenerateRequest request) {
        AiDto.GenerateResponse response = aiService.generateTaskDescription(request.getTitle());
        return ResponseEntity.ok(ApiResponse.success("Description generated successfully", response));
    }

    @PostMapping("/task-guidance")
    @Operation(summary = "Get detailed AI guidance for completing a task")
    public ResponseEntity<ApiResponse<AiDto.TaskGuidanceResponse>> getTaskGuidance(
            @RequestBody AiDto.TaskGuidanceRequest request) {
        AiDto.TaskGuidanceResponse response = aiService.getTaskGuidance(
            request.getTitle(), 
            request.getDescription(), 
            request.getPriority()
        );
        return ResponseEntity.ok(ApiResponse.success("Task guidance generated successfully", response));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get AI-powered productivity summary")
    public ResponseEntity<ApiResponse<AiDto.SummaryResponse>> getProductivitySummary() {
        User currentUser = authService.getCurrentUser();
        AiDto.SummaryResponse response = aiService.generateProductivitySummary(
            taskService.getAllUserTasks(currentUser), 
            currentUser.getUsername()
        );
        return ResponseEntity.ok(ApiResponse.success("Summary generated successfully", response));
    }
}