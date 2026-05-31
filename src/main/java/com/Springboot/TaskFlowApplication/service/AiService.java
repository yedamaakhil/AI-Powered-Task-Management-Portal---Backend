package com.Springboot.TaskFlowApplication.service;

import com.Springboot.TaskFlowApplication.DTOs.AiDto;
import com.Springboot.TaskFlowApplication.Entity.Task;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${app.ai.gemini.api-key}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.api-url}")
    private String geminiApiUrl;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public AiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    // ==================== PUBLIC METHODS ====================

    public AiDto.GenerateResponse generateTaskDescription(String taskTitle) {
        if (isApiKeyMissing()) {
            log.warn("Gemini API key not configured, using fallback");
            return getDynamicFallbackDescription(taskTitle);
        }

        try {
            String prompt = buildRealAIPrompt(taskTitle);
            log.info("Calling Gemini API for task: {}", taskTitle);
            String aiResponse = callGeminiApi(prompt);
            log.info("Received response from Gemini API");
            return parseRealAIResponse(aiResponse, taskTitle);
        } catch (Exception e) {
            log.error("AI API call failed: {}", e.getMessage());
            return getDynamicFallbackDescription(taskTitle);
        }
    }

    public AiDto.TaskGuidanceResponse getTaskGuidance(String taskTitle, String taskDescription, String priority) {
        if (isApiKeyMissing()) {
            return getDynamicFallbackGuidance(taskTitle);
        }

        try {
            String prompt = buildRealGuidancePrompt(taskTitle, taskDescription, priority);
            log.info("Calling Gemini API for guidance on: {}", taskTitle);
            String aiResponse = callGeminiApi(prompt);
            return parseRealGuidanceResponse(aiResponse, taskTitle);
        } catch (Exception e) {
            log.error("AI guidance generation failed: {}", e.getMessage());
            return getDynamicFallbackGuidance(taskTitle);
        }
    }

    public AiDto.SummaryResponse generateProductivitySummary(List<Task> tasks, String username) {
        long total = tasks.size();
        long done = tasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
        long pending = tasks.stream().filter(t -> t.getStatus() == Task.Status.TODO).count();
        
        LocalDate today = LocalDate.now();
        
        long overdue = tasks.stream()
                .filter(t -> t.getDueDate() != null 
                        && t.getDueDate().toLocalDate().isBefore(today)
                        && t.getStatus() != Task.Status.DONE)
                .count();
        
        long dueToday = tasks.stream()
                .filter(t -> t.getDueDate() != null 
                        && t.getDueDate().toLocalDate().isEqual(today)
                        && t.getStatus() != Task.Status.DONE)
                .count();

        int productivityPct = total == 0 ? 0 : (int) ((done * 100) / total);
        String productivityScore = productivityPct >= 75 ? "Excellent" 
                : productivityPct >= 50 ? "Good" 
                : productivityPct >= 25 ? "Average" 
                : "Needs Improvement";

        String aiSummary;
        boolean aiGenerated = false;

        if (!isApiKeyMissing() && total > 0) {
            try {
                String prompt = buildRealSummaryPrompt(username, total, done, inProgress, pending, overdue, dueToday, productivityPct);
                aiSummary = callGeminiApi(prompt);
                aiGenerated = true;
            } catch (Exception e) {
                log.error("AI summary call failed: {}", e.getMessage());
                aiSummary = buildDynamicFallbackSummary(username, total, done, pending, overdue, productivityScore);
            }
        } else {
            aiSummary = buildDynamicFallbackSummary(username, total, done, pending, overdue, productivityScore);
        }

        AiDto.SummaryResponse response = new AiDto.SummaryResponse();
        response.setTotalTasks((int) total);
        response.setCompletedTasks((int) done);
        response.setInProgressTasks((int) inProgress);
        response.setPendingTasks((int) pending);
        response.setOverdueTasks((int) overdue);
        response.setDueTodayTasks((int) dueToday);
        response.setProductivityScore(productivityScore);
        response.setAiSummary(aiSummary);
        response.setAiGenerated(aiGenerated);

        return response;
    }

    // ==================== REAL GEMINI API METHODS ====================

    private String callGeminiApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 800,
                    "topP", 0.95,
                    "topK", 40
                )
        );

        try {
            String response = webClientBuilder.build()
                    .post()
                    .uri(geminiApiUrl + "?key=" + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Gemini API error status: {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Gemini API error: " + errorBody)));
                    })
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            log.debug("Raw Gemini response: {}", response);
            return extractTextFromGeminiResponse(response);
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Gemini API error: " + e.getMessage());
        }
    }

    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            if (root.has("error")) {
                String errorMessage = root.path("error").path("message").asText();
                throw new RuntimeException("Gemini API error: " + errorMessage);
            }
            
            String text = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
            
            log.info("Successfully extracted text from Gemini response");
            return text;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

    // ==================== REAL AI PROMPTS ====================

    private String buildRealAIPrompt(String taskTitle) {
        return """
            You are an expert task management AI assistant. For the task titled "%s", provide a detailed, unique, and task-specific response.

            IMPORTANT: Return ONLY valid JSON, no markdown formatting, no explanations before or after.

            Return this exact JSON structure:
            {
                "description": "A detailed 2-3 sentence description specific to this task",
                "steps": ["Step 1", "Step 2", "Step 3", "Step 4", "Step 5"],
                "topics": ["Topic 1", "Topic 2", "Topic 3", "Topic 4"],
                "resources": ["Resource 1", "Resource 2", "Resource 3"],
                "estimatedHours": number (1-40),
                "priority": "LOW" or "MEDIUM" or "HIGH" or "CRITICAL",
                "tips": ["Tip 1", "Tip 2", "Tip 3"],
                "prerequisites": ["Prerequisite 1", "Prerequisite 2"]
            }

            Make every response unique and specifically tailored to "%s". Be practical and actionable.
            """.formatted(taskTitle, taskTitle);
    }

    private String buildRealGuidancePrompt(String taskTitle, String taskDescription, String priority) {
        return """
            You are an expert task completion coach. For the task "%s" (Priority: %s), provide detailed, SPECIFIC guidance.

            Task Description: %s

            IMPORTANT: Return ONLY valid JSON, no markdown formatting.

            Return this exact JSON structure:
            {
                "howToComplete": "A comprehensive 2-3 paragraph guide specific to this task",
                "keyTopics": ["Topic 1", "Topic 2", "Topic 3", "Topic 4"],
                "steps": [
                    {"step": 1, "action": "Specific action", "estimatedTime": "X hours", "tips": ["Tip 1", "Tip 2"]},
                    {"step": 2, "action": "Specific action", "estimatedTime": "X hours", "tips": ["Tip 1", "Tip 2"]},
                    {"step": 3, "action": "Specific action", "estimatedTime": "X hours", "tips": ["Tip 1", "Tip 2"]},
                    {"step": 4, "action": "Specific action", "estimatedTime": "X hours", "tips": ["Tip 1", "Tip 2"]},
                    {"step": 5, "action": "Specific action", "estimatedTime": "X hours", "tips": ["Tip 1", "Tip 2"]}
                ],
                "bestPractices": ["Practice 1", "Practice 2", "Practice 3"],
                "commonPitfalls": ["Pitfall 1 - how to avoid", "Pitfall 2 - how to avoid", "Pitfall 3 - how to avoid"],
                "recommendedResources": ["Resource 1", "Resource 2", "Resource 3"],
                "successMetrics": ["Metric 1", "Metric 2", "Metric 3"],
                "estimatedTotalTime": "Total time estimate"
            }

            Make every response unique and specific to the task "%s".
            """.formatted(taskTitle, priority, taskDescription != null ? taskDescription : "No additional description", taskTitle);
    }

    private String buildRealSummaryPrompt(String username, long total, long done, long inProgress, 
                                            long pending, long overdue, long dueToday, int productivityPct) {
        return """
            You are a productivity coach. Provide a motivational, personalized summary for %s.

            Task Statistics:
            - Total Tasks: %d
            - Completed: %d
            - In Progress: %d
            - Pending: %d
            - Overdue: %d
            - Due Today: %d
            - Productivity Score: %d%%

            Provide a 2-3 sentence motivational response with specific, actionable advice based on these exact numbers.
            Be encouraging and practical. Return ONLY the text, no JSON formatting.
            """.formatted(username, total, done, inProgress, pending, overdue, dueToday, productivityPct);
    }

    // ==================== REAL RESPONSE PARSERS ====================

    private AiDto.GenerateResponse parseRealAIResponse(String aiResponse, String taskTitle) {
        try {
            String cleanedResponse = cleanJsonResponse(aiResponse);
            log.debug("Cleaned JSON response: {}", cleanedResponse);
            
            JsonNode json = objectMapper.readTree(cleanedResponse);
            
            AiDto.GenerateResponse response = new AiDto.GenerateResponse();
            response.setDescription(json.path("description").asText("Complete the task: " + taskTitle));
            response.setPriority(json.path("priority").asText("MEDIUM"));
            response.setEstimatedHours(json.path("estimatedHours").asDouble(2.0));
            response.setEstimatedTime(formatEstimatedTime(response.getEstimatedHours()));
            response.setAiGenerated(true);
            
            response.setSteps(parseJsonArray(json, "steps"));
            response.setTopics(parseJsonArray(json, "topics"));
            response.setResources(parseJsonArray(json, "resources"));
            response.setTips(parseJsonArray(json, "tips"));
            response.setPrerequisites(parseJsonArray(json, "prerequisites"));
            
            return response;
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            return getDynamicFallbackDescription(taskTitle);
        }
    }

    private AiDto.TaskGuidanceResponse parseRealGuidanceResponse(String aiResponse, String taskTitle) {
        try {
            String cleanedResponse = cleanJsonResponse(aiResponse);
            JsonNode json = objectMapper.readTree(cleanedResponse);
            
            AiDto.TaskGuidanceResponse response = new AiDto.TaskGuidanceResponse();
            response.setHowToComplete(json.path("howToComplete").asText());
            response.setEstimatedTotalTime(json.path("estimatedTotalTime").asText());
            response.setAiGenerated(true);
            
            response.setKeyTopics(parseJsonArray(json, "keyTopics"));
            response.setBestPractices(parseJsonArray(json, "bestPractices"));
            response.setCommonPitfalls(parseJsonArray(json, "commonPitfalls"));
            response.setRecommendedResources(parseJsonArray(json, "recommendedResources"));
            response.setSuccessMetrics(parseJsonArray(json, "successMetrics"));
            
            List<AiDto.Step> steps = new ArrayList<>();
            JsonNode stepsNode = json.path("steps");
            if (stepsNode.isArray()) {
                for (JsonNode stepNode : stepsNode) {
                    AiDto.Step step = new AiDto.Step();
                    step.setStep(stepNode.path("step").asInt());
                    step.setAction(stepNode.path("action").asText());
                    step.setEstimatedTime(stepNode.path("estimatedTime").asText());
                    step.setTips(parseJsonArray(stepNode, "tips"));
                    steps.add(step);
                }
            }
            response.setSteps(steps);
            
            return response;
        } catch (Exception e) {
            log.error("Failed to parse guidance response: {}", e.getMessage());
            return getDynamicFallbackGuidance(taskTitle);
        }
    }

    // ==================== HELPER METHODS ====================

    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private List<String> parseJsonArray(JsonNode node, String fieldName) {
        List<String> list = new ArrayList<>();
        JsonNode arrayNode = node.path(fieldName);
        if (arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                String text = item.asText();
                if (text != null && !text.isEmpty()) {
                    list.add(text);
                }
            }
        }
        return list;
    }

    private String formatEstimatedTime(double hours) {
        if (hours < 1) return "< 1 hour";
        if (hours == 1) return "1 hour";
        if (hours < 24) return (int) hours + " hours";
        int days = (int) (hours / 8);
        int remaining = (int) (hours % 8);
        if (remaining > 0) return days + " day(s) and " + remaining + " hours";
        return days + " day(s)";
    }

    // ==================== FALLBACK METHODS ====================

    private AiDto.GenerateResponse getDynamicFallbackDescription(String taskTitle) {
        AiDto.GenerateResponse response = new AiDto.GenerateResponse();
        response.setDescription(getTaskSpecificDescription(taskTitle));
        response.setPriority(getTaskSpecificPriority(taskTitle));
        response.setEstimatedHours(getTaskSpecificHours(taskTitle));
        response.setEstimatedTime(formatEstimatedTime(response.getEstimatedHours()));
        response.setSteps(getTaskSpecificSteps(taskTitle));
        response.setTopics(getTaskSpecificTopics(taskTitle));
        response.setResources(getTaskSpecificResources(taskTitle));
        response.setTips(getTaskSpecificTips(taskTitle));
        response.setPrerequisites(getTaskSpecificPrerequisites(taskTitle));
        response.setAiGenerated(false);
        response.setFallbackReason("Using smart fallback - Connect to Gemini API for better results");
        return response;
    }

    private AiDto.TaskGuidanceResponse getDynamicFallbackGuidance(String taskTitle) {
        AiDto.TaskGuidanceResponse response = new AiDto.TaskGuidanceResponse();
        response.setHowToComplete(getTaskSpecificGuide(taskTitle));
        response.setKeyTopics(getTaskSpecificTopics(taskTitle));
        response.setBestPractices(getTaskSpecificBestPractices(taskTitle));
        response.setCommonPitfalls(getTaskSpecificPitfalls(taskTitle));
        response.setRecommendedResources(getTaskSpecificResources(taskTitle));
        response.setSuccessMetrics(getTaskSpecificMetrics(taskTitle));
        response.setEstimatedTotalTime(getTaskSpecificTotalTime(taskTitle));
        response.setSteps(getTaskSpecificGuidanceSteps(taskTitle));
        response.setAiGenerated(false);
        return response;
    }

    private String getTaskType(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("code") || lower.contains("develop") || lower.contains("build")) return "coding";
        if (lower.contains("design") || lower.contains("ui")) return "design";
        if (lower.contains("presentation") || lower.contains("slide")) return "presentation";
        if (lower.contains("report") || lower.contains("document")) return "report";
        if (lower.contains("meeting")) return "meeting";
        return "general";
    }

    private String getTaskSpecificDescription(String title) {
        String type = getTaskType(title);
        switch(type) {
            case "coding": return "Develop a robust solution for '" + title + "'. Write clean, maintainable code.";
            case "design": return "Create an engaging design for '" + title + "'. Focus on visual hierarchy.";
            case "presentation": return "Create a compelling presentation for '" + title + "'.";
            case "report": return "Prepare a comprehensive report for '" + title + "'.";
            default: return "Complete '" + title + "' efficiently.";
        }
    }

    private String getTaskSpecificPriority(String title) {
        String type = getTaskType(title);
        if (title.toLowerCase().contains("urgent")) return "CRITICAL";
        switch(type) {
            case "coding": return "HIGH";
            case "report": return "HIGH";
            case "meeting": return "LOW";
            default: return "MEDIUM";
        }
    }

    private double getTaskSpecificHours(String title) {
        String type = getTaskType(title);
        switch(type) {
            case "coding": return 6;
            case "report": return 5;
            case "design": return 4;
            case "presentation": return 3;
            case "meeting": return 1;
            default: return 2;
        }
    }

    private List<String> getTaskSpecificSteps(String title) {
        String type = getTaskType(title);
        if (type.equals("coding")) {
            return List.of("Analyze requirements", "Design architecture", "Write code", "Test", "Deploy");
        }
        if (type.equals("design")) {
            return List.of("Research", "Wireframes", "Mockups", "Prototype", "Handoff");
        }
        return List.of("Plan", "Execute", "Review", "Complete");
    }

    private List<String> getTaskSpecificTopics(String title) {
        String type = getTaskType(title);
        if (type.equals("coding")) return List.of("Architecture", "Algorithms", "Testing");
        if (type.equals("design")) return List.of("UI/UX", "Prototyping", "Usability");
        return List.of("Planning", "Execution", "Quality");
    }

    private List<String> getTaskSpecificResources(String title) {
        String type = getTaskType(title);
        if (type.equals("coding")) return List.of("IDE", "Git", "Documentation");
        if (type.equals("design")) return List.of("Figma", "Dribbble", "Fonts");
        return List.of("Tools", "Docs", "Team");
    }

    private List<String> getTaskSpecificTips(String title) {
        return List.of("Break down tasks", "Set deadlines", "Take breaks");
    }

    private List<String> getTaskSpecificPrerequisites(String title) {
        return List.of("Clear requirements", "Necessary tools");
    }

    private String getTaskSpecificGuide(String title) {
        return "To complete '" + title + "':\n1. Define objectives\n2. Create a plan\n3. Execute\n4. Review\n5. Deliver";
    }

    private List<String> getTaskSpecificBestPractices(String title) {
        return List.of("Start early", "Communicate", "Document");
    }

    private List<String> getTaskSpecificPitfalls(String title) {
        return List.of("Underestimating time", "Poor communication");
    }

    private List<String> getTaskSpecificMetrics(String title) {
        return List.of("On time", "Quality", "Feedback");
    }

    private String getTaskSpecificTotalTime(String title) {
        double hours = getTaskSpecificHours(title);
        if (hours <= 2) return "Half day";
        if (hours <= 4) return "Full day";
        return "1-2 days";
    }

    private List<AiDto.Step> getTaskSpecificGuidanceSteps(String title) {
        List<AiDto.Step> steps = new ArrayList<>();
        List<String> stepNames = getTaskSpecificSteps(title);
        for (int i = 0; i < stepNames.size(); i++) {
            AiDto.Step step = new AiDto.Step();
            step.setStep(i + 1);
            step.setAction(stepNames.get(i));
            step.setEstimatedTime("1-2 hours");
            step.setTips(List.of("Stay focused"));
            steps.add(step);
        }
        return steps;
    }

    private String buildDynamicFallbackSummary(String username, long total, long done, 
                                                long pending, long overdue, String score) {
        if (total == 0) return "Welcome to TaskFlow! Create your first task!";
        if (overdue > 0) return "You have " + overdue + " overdue task(s). Prioritize these!";
        if (done == total) return "Outstanding! You've completed all tasks!";
        double rate = (done * 100.0) / total;
        if (rate >= 75) return "Amazing progress! You're at " + String.format("%.0f", rate) + "%!";
        if (rate >= 50) return "Great progress! You're halfway there!";
        return "Keep going! Completed " + done + " out of " + total + " tasks.";
    }

    // FIXED: Removed hardcoded API keys from this method
    private boolean isApiKeyMissing() {
        return geminiApiKey == null || geminiApiKey.isBlank() || 
               geminiApiKey.equals("your-gemini-api-key-here");
    }
}