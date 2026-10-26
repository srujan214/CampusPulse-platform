package com.campuspulse.dto;

import java.time.LocalDateTime;

public class StatusHistoryDto {
    private Long id;
    private String previousStatus;
    private String newStatus;
    private String actedByName;
    private String actedByRole;
    private String remarks;
    private LocalDateTime timestamp;

    public StatusHistoryDto() {
    }

    public StatusHistoryDto(Long id, String previousStatus, String newStatus, String actedByName, String actedByRole, String remarks, LocalDateTime timestamp) {
        this.id = id;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.actedByName = actedByName;
        this.actedByRole = actedByRole;
        this.remarks = remarks;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getActedByName() {
        return actedByName;
    }

    public void setActedByName(String actedByName) {
        this.actedByName = actedByName;
    }

    public String getActedByRole() {
        return actedByRole;
    }

    public void setActedByRole(String actedByRole) {
        this.actedByRole = actedByRole;
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
