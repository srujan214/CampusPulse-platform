package com.campuspulse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints", indexes = {
    @Index(name = "idx_ticket_id", columnList = "ticketId", unique = true),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_category", columnList = "category")
})
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String ticketId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ComplaintCategory category;

    @Column(nullable = false, length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintSeverity severity;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus status;

    private int priorityScore;

    @Column(length = 30)
    private String priorityLevel;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String priorityExplanation;

    @Column(length = 100)
    private String aiDepartment;

    private boolean aiSafetyConcern;

    private Double aiConfidence;

    @Column(length = 50)
    private String aiResultSource; // "AI_GEMINI", "AI_CLAUDE", "FALLBACK_ENGINE"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    public Complaint() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ComplaintStatus.SUBMITTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ComplaintSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ComplaintSeverity severity) {
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

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
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

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public User getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(User assignedStaff) {
        this.assignedStaff = assignedStaff;
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
}
