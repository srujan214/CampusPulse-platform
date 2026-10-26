package com.campuspulse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_histories")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus newStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "acted_by_id")
    private User actedBy;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public StatusHistory() {
    }

    public StatusHistory(Complaint complaint, ComplaintStatus previousStatus, ComplaintStatus newStatus, User actedBy, String remarks) {
        this.complaint = complaint;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.actedBy = actedBy;
        this.remarks = remarks;
        this.timestamp = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public ComplaintStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ComplaintStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public ComplaintStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ComplaintStatus newStatus) {
        this.newStatus = newStatus;
    }

    public User getActedBy() {
        return actedBy;
    }

    public void setActedBy(User actedBy) {
        this.actedBy = actedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
