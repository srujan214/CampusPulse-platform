package com.campuspulse.dto;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsResponse {
    private long total;
    private long submitted;
    private long open; // open = submitted + acknowledged
    private long inProgress;
    private long resolved;
    private long overdue;
    private Map<String, Long> complaintsByStatus = new HashMap<>();
    private Map<String, Long> complaintsByCategory = new HashMap<>();

    public AnalyticsResponse() {
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSubmitted() {
        return submitted;
    }

    public void setSubmitted(long submitted) {
        this.submitted = submitted;
    }

    public long getOpen() {
        return open;
    }

    public void setOpen(long open) {
        this.open = open;
    }

    public long getInProgress() {
        return inProgress;
    }

    public void setInProgress(long inProgress) {
        this.inProgress = inProgress;
    }

    public long getResolved() {
        return resolved;
    }

    public void setResolved(long resolved) {
        this.resolved = resolved;
    }

    public long getOverdue() {
        return overdue;
    }

    public void setOverdue(long overdue) {
        this.overdue = overdue;
    }

    public Map<String, Long> getComplaintsByStatus() {
        return complaintsByStatus;
    }

    public void setComplaintsByStatus(Map<String, Long> complaintsByStatus) {
        this.complaintsByStatus = complaintsByStatus;
    }

    public Map<String, Long> getComplaintsByCategory() {
        return complaintsByCategory;
    }

    public void setComplaintsByCategory(Map<String, Long> complaintsByCategory) {
        this.complaintsByCategory = complaintsByCategory;
    }
}
