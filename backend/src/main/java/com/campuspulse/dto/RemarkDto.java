package com.campuspulse.dto;

import java.time.LocalDateTime;

public class RemarkDto {
    private Long id;
    private String authorName;
    private String authorRole;
    private String content;
    private boolean isStaffRemark;
    private LocalDateTime timestamp;

    public RemarkDto() {
    }

    public RemarkDto(Long id, String authorName, String authorRole, String content, boolean isStaffRemark, LocalDateTime timestamp) {
        this.id = id;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.content = content;
        this.isStaffRemark = isStaffRemark;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isStaffRemark() {
        return isStaffRemark;
    }

    public void setStaffRemark(boolean staffRemark) {
        isStaffRemark = staffRemark;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
