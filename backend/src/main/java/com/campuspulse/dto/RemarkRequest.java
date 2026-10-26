package com.campuspulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RemarkRequest {

    @NotBlank(message = "Remark content cannot be empty")
    @Size(max = 1000, message = "Remark cannot exceed 1000 characters")
    private String content;

    public RemarkRequest() {
    }

    public RemarkRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
