package com.Springboot.TaskFlowApplication.Controller;

import com.Springboot.TaskFlowApplication.DTOs.ApiResponse;
import com.Springboot.TaskFlowApplication.Entity.TaskHistory;
import com.Springboot.TaskFlowApplication.Entity.User;
import com.Springboot.TaskFlowApplication.service.AuthService;
import com.Springboot.TaskFlowApplication.service.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@Tag(name = "Blockchain", description = "APIs for blockchain verification and audit")
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final AuthService authService;

    public BlockchainController(BlockchainService blockchainService, AuthService authService) {
        this.blockchainService = blockchainService;
        this.authService = authService;
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify blockchain integrity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyBlockchain() {
        User currentUser = authService.getCurrentUser();
        
        boolean isValid = blockchainService.verifyChain();
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", isValid);
        result.put("message", isValid ? "Blockchain is valid and untampered" : "Blockchain integrity violation detected!");
        result.put("verifiedBy", currentUser.getUsername());
        result.put("verifiedAt", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(ApiResponse.success("Blockchain verification completed", result));
    }
    
    @GetMapping("/tasks/{taskId}/history")
    @Operation(summary = "Get task history from blockchain")
    public ResponseEntity<ApiResponse<List<TaskHistory>>> getTaskHistory(@PathVariable Long taskId) {
        User currentUser = authService.getCurrentUser();
        List<TaskHistory> history = blockchainService.getTaskHistory(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task history retrieved successfully", history));
    }
    
    @GetMapping("/tasks/{taskId}/block-count")
    @Operation(summary = "Get block count for a task")
    public ResponseEntity<ApiResponse<Integer>> getBlockCount(@PathVariable Long taskId) {
        int count = blockchainService.getBlockCount(taskId);
        return ResponseEntity.ok(ApiResponse.success("Block count retrieved", count));
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get blockchain statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBlockchainStats() {
        Map<String, Object> stats = blockchainService.getBlockchainStats();
        return ResponseEntity.ok(ApiResponse.success("Blockchain stats retrieved", stats));
    }
}