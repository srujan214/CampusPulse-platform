package com.campuspulse.controller;

import com.campuspulse.dto.ComplaintRequest;
import com.campuspulse.dto.ComplaintResponse;
import com.campuspulse.dto.RemarkDto;
import com.campuspulse.dto.RemarkRequest;
import com.campuspulse.entity.User;
import com.campuspulse.service.AuthService;
import com.campuspulse.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final AuthService authService;

    public ComplaintController(ComplaintService complaintService, AuthService authService) {
        this.complaintService = complaintService;
        this.authService = authService;
    }

    @PostMapping(value = "/submit", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ComplaintResponse> submitComplaintMultipart(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("location") String location,
            @RequestParam("severity") String severity,
            @RequestParam("description") String description,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        User currentUser = authService.getCurrentAuthenticatedUser();

        ComplaintRequest request = new ComplaintRequest();
        request.setTitle(title);
        request.setCategory(category);
        request.setLocation(location);
        request.setSeverity(severity);
        request.setDescription(description);

        ComplaintResponse response = complaintService.submitComplaint(request, photo, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/submit", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ComplaintResponse> submitComplaintJson(@Valid @RequestBody ComplaintRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ComplaintResponse response = complaintService.submitComplaint(request, null, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ComplaintResponse>> getMyComplaints() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<ComplaintResponse> list = complaintService.getStudentComplaints(currentUser);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<ComplaintResponse> getComplaintDetails(@PathVariable("ticketId") String ticketId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ComplaintResponse response = complaintService.getComplaintByTicketId(ticketId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{ticketId}/remarks")
    public ResponseEntity<RemarkDto> addRemark(
            @PathVariable("ticketId") String ticketId,
            @Valid @RequestBody RemarkRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        RemarkDto remarkDto = complaintService.addRemark(ticketId, request, currentUser);
        return ResponseEntity.ok(remarkDto);
    }
}
