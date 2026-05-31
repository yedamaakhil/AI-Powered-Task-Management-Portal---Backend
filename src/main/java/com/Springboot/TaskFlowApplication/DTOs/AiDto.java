package com.Springboot.TaskFlowApplication.DTOs;

import java.util.List;

public class AiDto {
    
    // Request for generating task description
    public static class GenerateRequest {
        private String title;
        
        public GenerateRequest() {}
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
    
    // Response for task description generation
    public static class GenerateResponse {
        private String description;
        private String priority;
        private Double estimatedHours;
        private String estimatedTime;
        private boolean aiGenerated;
        private String fallbackReason;
        private List<String> steps;
        private List<String> topics;
        private List<String> resources;
        private List<String> tips;
        private List<String> prerequisites;
        
        public GenerateResponse() {}
        
        // Getters and Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
        
        public String getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
        
        public boolean isAiGenerated() { return aiGenerated; }
        public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
        
        public String getFallbackReason() { return fallbackReason; }
        public void setFallbackReason(String fallbackReason) { this.fallbackReason = fallbackReason; }
        
        public List<String> getSteps() { return steps; }
        public void setSteps(List<String> steps) { this.steps = steps; }
        
        public List<String> getTopics() { return topics; }
        public void setTopics(List<String> topics) { this.topics = topics; }
        
        public List<String> getResources() { return resources; }
        public void setResources(List<String> resources) { this.resources = resources; }
        
        public List<String> getTips() { return tips; }
        public void setTips(List<String> tips) { this.tips = tips; }
        
        public List<String> getPrerequisites() { return prerequisites; }
        public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    }
    
    // Request for task guidance
    public static class TaskGuidanceRequest {
        private String title;
        private String description;
        private String priority;
        
        public TaskGuidanceRequest() {}
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
    
    // Response for task guidance
    public static class TaskGuidanceResponse {
        private String howToComplete;
        private List<String> keyTopics;
        private List<Step> steps;
        private List<String> bestPractices;
        private List<String> commonPitfalls;
        private List<String> recommendedResources;
        private List<String> successMetrics;
        private String estimatedTotalTime;
        private boolean aiGenerated;
        
        public TaskGuidanceResponse() {}
        
        // Getters and Setters
        public String getHowToComplete() { return howToComplete; }
        public void setHowToComplete(String howToComplete) { this.howToComplete = howToComplete; }
        
        public List<String> getKeyTopics() { return keyTopics; }
        public void setKeyTopics(List<String> keyTopics) { this.keyTopics = keyTopics; }
        
        public List<Step> getSteps() { return steps; }
        public void setSteps(List<Step> steps) { this.steps = steps; }
        
        public List<String> getBestPractices() { return bestPractices; }
        public void setBestPractices(List<String> bestPractices) { this.bestPractices = bestPractices; }
        
        public List<String> getCommonPitfalls() { return commonPitfalls; }
        public void setCommonPitfalls(List<String> commonPitfalls) { this.commonPitfalls = commonPitfalls; }
        
        public List<String> getRecommendedResources() { return recommendedResources; }
        public void setRecommendedResources(List<String> recommendedResources) { this.recommendedResources = recommendedResources; }
        
        public List<String> getSuccessMetrics() { return successMetrics; }
        public void setSuccessMetrics(List<String> successMetrics) { this.successMetrics = successMetrics; }
        
        public String getEstimatedTotalTime() { return estimatedTotalTime; }
        public void setEstimatedTotalTime(String estimatedTotalTime) { this.estimatedTotalTime = estimatedTotalTime; }
        
        public boolean isAiGenerated() { return aiGenerated; }
        public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
    }
    
    // Step class for guidance
    public static class Step {
        private int step;
        private String action;
        private String estimatedTime;
        private List<String> tips;
        
        public Step() {}
        
        public int getStep() { return step; }
        public void setStep(int step) { this.step = step; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
        
        public List<String> getTips() { return tips; }
        public void setTips(List<String> tips) { this.tips = tips; }
    }
    
    // Summary Response
    public static class SummaryResponse {
        private int totalTasks;
        private int completedTasks;
        private int inProgressTasks;
        private int pendingTasks;
        private int overdueTasks;
        private int dueTodayTasks;
        private String productivityScore;
        private String aiSummary;
        private boolean aiGenerated;
        
        public SummaryResponse() {}
        
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
        
        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
        
        public int getInProgressTasks() { return inProgressTasks; }
        public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }
        
        public int getPendingTasks() { return pendingTasks; }
        public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }
        
        public int getOverdueTasks() { return overdueTasks; }
        public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }
        
        public int getDueTodayTasks() { return dueTodayTasks; }
        public void setDueTodayTasks(int dueTodayTasks) { this.dueTodayTasks = dueTodayTasks; }
        
        public String getProductivityScore() { return productivityScore; }
        public void setProductivityScore(String productivityScore) { this.productivityScore = productivityScore; }
        
        public String getAiSummary() { return aiSummary; }
        public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
        
        public boolean isAiGenerated() { return aiGenerated; }
        public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
    }
}