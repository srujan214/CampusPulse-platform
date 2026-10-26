package com.campuspulse.controller;

import com.campuspulse.dto.ComplaintResponse;
import com.campuspulse.dto.StatusUpdateRequest;
import com.campuspulse.entity.User;
import com.campuspulse.service.AuthService;
import com.campuspulse.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
public class StaffController {

    private final ComplaintService complaintService;
    private final AuthService authService;

    public StaffController(ComplaintService complaintService, AuthService authService) {
        this.complaintService = complaintService;
        this.authService = authService;
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintResponse>> getStaffComplaints(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category) {
        List<ComplaintResponse> complaints = complaintService.getStaffComplaints(query, status, category);
        return ResponseEntity.ok(complaints);
    }

    @PutMapping("/complaints/{ticketId}/status")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @PathVariable("ticketId") String ticketId,
            @Valid @RequestBody StatusUpdateRequest request) {
        User staff = authService.getCurrentAuthenticatedUser();
        ComplaintResponse updated = complaintService.updateStatus(ticketId, request, staff);
        return ResponseEntity.ok(updated);
    }
}
