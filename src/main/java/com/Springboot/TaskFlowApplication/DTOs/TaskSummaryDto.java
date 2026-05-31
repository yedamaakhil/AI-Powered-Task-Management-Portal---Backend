package com.Springboot.TaskFlowApplication.DTOs;

public class TaskSummaryDto {
    private long totalTasks;
    private long inProgressTasks;
    private long completedTasks;
    private long overdueTasks;
    private String productivityScore;
    private String aiSummary;
    private boolean aiGenerated;
    
    public TaskSummaryDto() {}
    
    // Getters and Setters
    public long getTotalTasks() { return totalTasks; }
    public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
    
    public long getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(long inProgressTasks) { this.inProgressTasks = inProgressTasks; }
    
    public long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
    
    public long getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(long overdueTasks) { this.overdueTasks = overdueTasks; }
    
    public String getProductivityScore() { return productivityScore; }
    public void setProductivityScore(String productivityScore) { this.productivityScore = productivityScore; }
    
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    
    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
}