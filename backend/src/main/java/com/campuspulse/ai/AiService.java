package com.campuspulse.ai;

import com.campuspulse.entity.ComplaintCategory;
import com.campuspulse.entity.ComplaintSeverity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final FallbackScoringEngine fallbackScoringEngine;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${campuspulse.ai.provider:fallback}")
    private String aiProvider;

    @Value("${campuspulse.ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${campuspulse.ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Value("${campuspulse.ai.claude.api-key:}")
    private String claudeApiKey;

    @Value("${campuspulse.ai.claude.model:claude-3-haiku-20240307}")
    private String claudeModel;

    @Value("${campuspulse.ai.timeout-ms:3500}")
    private int timeoutMs;

    public AiService(FallbackScoringEngine fallbackScoringEngine, ObjectMapper objectMapper) {
        this.fallbackScoringEngine = fallbackScoringEngine;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3500);
        factory.setReadTimeout(3500);
        this.restTemplate = new RestTemplate(factory);
    }

    public AiAnalysisResult analyzeComplaint(String title, String description, ComplaintCategory category, String location, ComplaintSeverity severity) {
        // If provider is fallback or no keys configured, use local engine immediately
        if ("fallback".equalsIgnoreCase(aiProvider) || (!StringUtils.hasText(geminiApiKey) && !StringUtils.hasText(claudeApiKey))) {
            logger.info("Using local deterministic FallbackScoringEngine (AI provider: {})", aiProvider);
            return fallbackScoringEngine.analyze(title, description, category, location, severity);
        }

        try {
            if ("gemini".equalsIgnoreCase(aiProvider) && StringUtils.hasText(geminiApiKey)) {
                return callGemini(title, description, category, location, severity);
            } else if ("claude".equalsIgnoreCase(aiProvider) && StringUtils.hasText(claudeApiKey)) {
                return callClaude(title, description, category, location, severity);
            }
        } catch (Exception ex) {
            logger.warn("External AI call failed ({}: {}). Engaging local fallback scoring engine.",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }

        // Graceful offline fallback
        return fallbackScoringEngine.analyze(title, description, category, location, severity);
    }

    private AiAnalysisResult callGemini(String title, String description, ComplaintCategory category, String location, ComplaintSeverity severity) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;

        String prompt = buildPrompt(title, description, category, location, severity);

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> parts = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(parts));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(new URI(url), entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (!textNode.isMissingNode()) {
                String aiText = textNode.asText();
                return parseStructuredJson(aiText, "AI_GEMINI", category, severity);
            }
        }
        throw new RuntimeException("Invalid response from Gemini API");
    }

    private AiAnalysisResult callClaude(String title, String description, ComplaintCategory category, String location, ComplaintSeverity severity) throws Exception {
        String url = "https://api.anthropic.com/v1/messages";

        String prompt = buildPrompt(title, description, category, location, severity);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", claudeModel);
        requestBody.put("max_tokens", 400);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", claudeApiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(new URI(url), entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("content").path(0).path("text");
            if (!textNode.isMissingNode()) {
                return parseStructuredJson(textNode.asText(), "AI_CLAUDE", category, severity);
            }
        }
        throw new RuntimeException("Invalid response from Claude API");
    }

    private String buildPrompt(String title, String description, ComplaintCategory category, String location, ComplaintSeverity severity) {
        return "You are an intelligent campus maintenance prioritization AI for CampusPulse.\n" +
                "Analyze the following student campus report:\n" +
                "Title: " + title + "\n" +
                "Description: " + description + "\n" +
                "Category: " + (category != null ? category.getDisplayName() : "Other") + "\n" +
                "Location: " + location + "\n" +
                "Severity: " + (severity != null ? severity.name() : "MEDIUM") + "\n\n" +
                "Respond ONLY with a valid JSON object in this exact structure with no extra text or markdown ticks:\n" +
                "{\n" +
                "  \"category\": \"string\",\n" +
                "  \"suggestedPriority\": \"Low | Medium | High | Critical\",\n" +
                "  \"priorityScore\": 1-100,\n" +
                "  \"department\": \"string\",\n" +
                "  \"reason\": \"short 1-2 sentence explanation\",\n" +
                "  \"safetyConcern\": true | false,\n" +
                "  \"confidence\": 0.0 - 1.0\n" +
                "}";
    }

    private AiAnalysisResult parseStructuredJson(String jsonText, String source, ComplaintCategory originalCategory, ComplaintSeverity severity) {
        try {
            // Strip any markdown fences if model included them
            String clean = jsonText.trim();
            if (clean.startsWith("```json")) {
                clean = clean.substring(7);
            }
            if (clean.startsWith("```")) {
                clean = clean.substring(3);
            }
            if (clean.endsWith("```")) {
                clean = clean.substring(0, clean.length() - 3);
            }
            clean = clean.trim();

            JsonNode node = objectMapper.readTree(clean);
            AiAnalysisResult res = new AiAnalysisResult();
            res.setResultSource(source);
            res.setCategory(node.path("category").asText(originalCategory != null ? originalCategory.getDisplayName() : "Other"));
            res.setSuggestedPriority(node.path("suggestedPriority").asText("High"));
            res.setPriorityScore(node.path("priorityScore").asInt(75));
            res.setDepartment(node.path("department").asText(fallbackScoringEngine.resolveDepartment(originalCategory)));
            res.setReason(node.path("reason").asText("AI analyzed the reported campus issue."));
            res.setSafetyConcern(node.path("safetyConcern").asBoolean(false));
            res.setConfidence(node.path("confidence").asDouble(0.90));

            List<String> factors = new ArrayList<>();
            factors.add("AI prioritized via " + source);
            factors.add("Detected category: " + res.getCategory());
            if (res.isSafetyConcern()) {
                factors.add("Safety impact flagged by AI (+30 pts)");
            }
            factors.add("Model confidence rating: " + Math.round(res.getConfidence() * 100) + "%");
            res.setBreakdownFactors(factors);

            return res;
        } catch (Exception e) {
            logger.warn("Failed to parse AI JSON response: {}. Falling back to deterministic engine.", e.getMessage());
            return fallbackScoringEngine.analyze("", "", originalCategory, "", severity);
        }
    }
}
