package com.campuspulse.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email or Student/Staff ID is required")
    private String emailOrId;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String emailOrId, String password) {
        this.emailOrId = emailOrId;
        this.password = password;
    }

    public String getEmailOrId() {
        return emailOrId;
    }

    public void setEmailOrId(String emailOrId) {
        this.emailOrId = emailOrId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
