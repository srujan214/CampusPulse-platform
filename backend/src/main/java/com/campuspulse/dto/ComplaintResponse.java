package com.campuspulse.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ComplaintResponse {
    private Long id;
    private String ticketId;
    private String title;
    private String category;
    private String categoryDisplayName;
    private String location;
    private String severity;
    private String description;
    private String photoUrl;
    private String status;
    private int priorityScore;
    private String priorityLevel;
    private String priorityExplanation;
    private String aiDepartment;
    private boolean aiSafetyConcern;
    private Double aiConfidence;
    private String aiResultSource; // "AI_GEMINI", "AI_CLAUDE", "FALLBACK_ENGINE"
    
    // Student Info
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentRollOrStaffId;

    // Staff Info
    private Long assignedStaffId;
    private String assignedStaffName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    // History and Remarks
    private List<StatusHistoryDto> timeline = new ArrayList<>();
    private List<RemarkDto> remarks = new ArrayList<>();

    public ComplaintResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(int priorityScore) {
        this.priorityScore = priorityScore;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getPriorityExplanation() {
        return priorityExplanation;
    }

    public void setPriorityExplanation(String priorityExplanation) {
        this.priorityExplanation = priorityExplanation;
    }

    public String getAiDepartment() {
        return aiDepartment;
    }

    public void setAiDepartment(String aiDepartment) {
        this.aiDepartment = aiDepartment;
    }

    public boolean isAiSafetyConcern() {
        return aiSafetyConcern;
    }

    public void setAiSafetyConcern(boolean aiSafetyConcern) {
        this.aiSafetyConcern = aiSafetyConcern;
    }

    public Double getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(Double aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public String getAiResultSource() {
        return aiResultSource;
    }

    public void setAiResultSource(String aiResultSource) {
        this.aiResultSource = aiResultSource;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentRollOrStaffId() {
        return studentRollOrStaffId;
    }

    public void setStudentRollOrStaffId(String studentRollOrStaffId) {
        this.studentRollOrStaffId = studentRollOrStaffId;
    }

    public Long getAssignedStaffId() {
        return assignedStaffId;
    }

    public void setAssignedStaffId(Long assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }

    public String getAssignedStaffName() {
        return assignedStaffName;
    }

    public void setAssignedStaffName(String assignedStaffName) {
        this.assignedStaffName = assignedStaffName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public List<StatusHistoryDto> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<StatusHistoryDto> timeline) {
        this.timeline = timeline;
    }

    public List<RemarkDto> getRemarks() {
        return remarks;
    }

    public void setRemarks(List<RemarkDto> remarks) {
        this.remarks = remarks;
    }
}
