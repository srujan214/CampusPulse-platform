package com.campuspulse.controller;

import com.campuspulse.dto.AnalyticsResponse;
import com.campuspulse.entity.User;
import com.campuspulse.service.AnalyticsService;
import com.campuspulse.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthService authService;

    public AnalyticsController(AnalyticsService analyticsService, AuthService authService) {
        this.analyticsService = analyticsService;
        this.authService = authService;
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
    public ResponseEntity<AnalyticsResponse> getStaffAnalytics() {
        AnalyticsResponse response = analyticsService.getStaffAnalytics();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student")
    public ResponseEntity<Map<String, Long>> getStudentAnalytics() {
        User student = authService.getCurrentAuthenticatedUser();
        Map<String, Long> stats = analyticsService.getStudentStats(student);
        return ResponseEntity.ok(stats);
    }
}
