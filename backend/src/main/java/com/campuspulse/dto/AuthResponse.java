package com.campuspulse.dto;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    private String studentOrStaffId;
    private String role;
    private String department;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long id, String email, String fullName, String studentOrStaffId, String role, String department) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.studentOrStaffId = studentOrStaffId;
        this.role = role;
        this.department = department;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentOrStaffId() {
        return studentOrStaffId;
    }

    public void setStudentOrStaffId(String studentOrStaffId) {
        this.studentOrStaffId = studentOrStaffId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
