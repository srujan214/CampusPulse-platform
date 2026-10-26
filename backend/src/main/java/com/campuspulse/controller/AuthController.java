package com.campuspulse.controller;

import com.campuspulse.dto.AuthResponse;
import com.campuspulse.dto.LoginRequest;
import com.campuspulse.dto.RegisterRequest;
import com.campuspulse.entity.User;
import com.campuspulse.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.registerStudent(registerRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        User user = authService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "studentOrStaffId", user.getStudentOrStaffId() != null ? user.getStudentOrStaffId() : "",
                "role", user.getRole().name(),
                "department", user.getDepartment() != null ? user.getDepartment() : ""
        ));
    }
}
