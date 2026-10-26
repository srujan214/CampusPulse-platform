package com.campuspulse.ai;

import java.util.ArrayList;
import java.util.List;

public class AiAnalysisResult {
    private String category;
    private String suggestedPriority;
    private int priorityScore;
    private String department;
    private String reason;
    private boolean safetyConcern;
    private Double confidence;
    private String resultSource; // "AI_GEMINI", "AI_CLAUDE", "FALLBACK_ENGINE"
    private List<String> breakdownFactors = new ArrayList<>();

    public AiAnalysisResult() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSuggestedPriority() {
        return suggestedPriority;
    }

    public void setSuggestedPriority(String suggestedPriority) {
        this.suggestedPriority = suggestedPriority;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(int priorityScore) {
        this.priorityScore = priorityScore;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isSafetyConcern() {
        return safetyConcern;
    }

    public void setSafetyConcern(boolean safetyConcern) {
        this.safetyConcern = safetyConcern;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getResultSource() {
        return resultSource;
    }

    public void setResultSource(String resultSource) {
        this.resultSource = resultSource;
    }

    public List<String> getBreakdownFactors() {
        return breakdownFactors;
    }

    public void setBreakdownFactors(List<String> breakdownFactors) {
        this.breakdownFactors = breakdownFactors;
    }

    public void addBreakdownFactor(String factor) {
        this.breakdownFactors.add(factor);
    }
}
