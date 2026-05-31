package com.Springboot.TaskFlowApplication.DTOs;

import com.Springboot.TaskFlowApplication.Entity.Task;
import java.time.LocalDateTime;
import java.util.List;

public class TaskDto {
    
    // Create Request
    public static class CreateRequest {
        private String title;
        private String description;
        private Task.Priority priority;
        private LocalDateTime dueDate;
        private Double estimatedHours;
        
        public CreateRequest() {}
        
        public CreateRequest(String title, String description, Task.Priority priority, LocalDateTime dueDate, Double estimatedHours) {
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.dueDate = dueDate;
            this.estimatedHours = estimatedHours;
        }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Task.Priority getPriority() { return priority; }
        public void setPriority(Task.Priority priority) { this.priority = priority; }
        
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
        
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
    }
    
    // Update Request
    public static class UpdateRequest {
        private String title;
        private String description;
        private Task.Priority priority;
        private Task.Status status;
        private LocalDateTime dueDate;
        private Double estimatedHours;
        
        public UpdateRequest() {}
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Task.Priority getPriority() { return priority; }
        public void setPriority(Task.Priority priority) { this.priority = priority; }
        
        public Task.Status getStatus() { return status; }
        public void setStatus(Task.Status status) { this.status = status; }
        
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
        
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
    }
    
    // Status Update Request
    public static class StatusUpdateRequest {
        private Task.Status status;
        
        public StatusUpdateRequest() {}
        
        public Task.Status getStatus() { return status; }
        public void setStatus(Task.Status status) { this.status = status; }
    }
    
    // Response
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private Task.Priority priority;
        private Task.Status status;
        private LocalDateTime dueDate;
        private Double estimatedHours;
        private String aiGeneratedDescription;
        private String taskHash;
        private Long userId;
        private String username;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int blockCount;
        
        public Response() {}
        
        public static Response fromEntity(Task task) {
            Response response = new Response();
            response.setId(task.getId());
            response.setTitle(task.getTitle());
            response.setDescription(task.getDescription());
            response.setPriority(task.getPriority());
            response.setStatus(task.getStatus());
            response.setDueDate(task.getDueDate());
            response.setEstimatedHours(task.getEstimatedHours());
            response.setAiGeneratedDescription(task.getAiGeneratedDescription());
            response.setTaskHash(task.getTaskHash());
            if (task.getUser() != null) {
                response.setUserId(task.getUser().getId());
                response.setUsername(task.getUser().getUsername());
            }
            response.setCreatedAt(task.getCreatedAt());
            response.setUpdatedAt(task.getUpdatedAt());
            response.setBlockCount(0);
            return response;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Task.Priority getPriority() { return priority; }
        public void setPriority(Task.Priority priority) { this.priority = priority; }
        
        public Task.Status getStatus() { return status; }
        public void setStatus(Task.Status status) { this.status = status; }
        
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
        
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
        
        public String getAiGeneratedDescription() { return aiGeneratedDescription; }
        public void setAiGeneratedDescription(String aiGeneratedDescription) { this.aiGeneratedDescription = aiGeneratedDescription; }
        
        public String getTaskHash() { return taskHash; }
        public void setTaskHash(String taskHash) { this.taskHash = taskHash; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public int getBlockCount() { return blockCount; }
        public void setBlockCount(int blockCount) { this.blockCount = blockCount; }
    }
    
    // Paged Response
    public static class PagedResponse {
        private List<Response> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean last;
        
        public PagedResponse() {}
        
        public List<Response> getContent() { return content; }
        public void setContent(List<Response> content) { this.content = content; }
        
        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
        
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        
        public boolean isLast() { return last; }
        public void setLast(boolean last) { this.last = last; }
    }
}