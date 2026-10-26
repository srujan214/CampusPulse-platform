package com.campuspulse.service;

import com.campuspulse.ai.AiAnalysisResult;
import com.campuspulse.ai.AiService;
import com.campuspulse.dto.*;
import com.campuspulse.entity.*;
import com.campuspulse.exception.BadRequestException;
import com.campuspulse.exception.ResourceNotFoundException;
import com.campuspulse.exception.UnauthorizedException;
import com.campuspulse.repository.ComplaintRepository;
import com.campuspulse.repository.RemarkRepository;
import com.campuspulse.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final RemarkRepository remarkRepository;
    private final AiService aiService;
    private final FileStorageService fileStorageService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            StatusHistoryRepository statusHistoryRepository,
                            RemarkRepository remarkRepository,
                            AiService aiService,
                            FileStorageService fileStorageService) {
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.remarkRepository = remarkRepository;
        this.aiService = aiService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ComplaintResponse submitComplaint(ComplaintRequest request, MultipartFile photo, User student) {
        if (student == null) {
            throw new BadRequestException("Student account is required to report an issue");
        }

        ComplaintCategory category = ComplaintCategory.fromString(request.getCategory());
        ComplaintSeverity severity;
        try {
            severity = ComplaintSeverity.valueOf(request.getSeverity().toUpperCase());
        } catch (Exception e) {
            severity = ComplaintSeverity.MEDIUM;
        }

        // Store photo if provided
        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            photoUrl = fileStorageService.storeFile(photo);
        }

        // Run AI classification / local fallback
        AiAnalysisResult analysis = aiService.analyzeComplaint(
                request.getTitle(),
                request.getDescription(),
                category,
                request.getLocation(),
                severity
        );

        // Generate unique ticket ID, e.g., CP-1001, CP-1002
        long count = complaintRepository.count();
        String ticketId = "CP-" + (1001 + count);
        while (complaintRepository.findByTicketId(ticketId).isPresent()) {
            ticketId = "CP-" + (1001 + count + (int)(Math.random() * 1000));
        }

        Complaint complaint = new Complaint();
        complaint.setTicketId(ticketId);
        complaint.setTitle(request.getTitle().trim());
        complaint.setCategory(category);
        complaint.setLocation(request.getLocation().trim());
        complaint.setSeverity(severity);
        complaint.setDescription(request.getDescription().trim());
        complaint.setPhotoUrl(photoUrl);
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setStudent(student);

        // Prioritization metrics
        complaint.setPriorityScore(analysis.getPriorityScore());
        complaint.setPriorityLevel(analysis.getSuggestedPriority());
        complaint.setPriorityExplanation(String.join(" | ", analysis.getBreakdownFactors()));
        complaint.setAiDepartment(analysis.getDepartment());
        complaint.setAiSafetyConcern(analysis.isSafetyConcern());
        complaint.setAiConfidence(analysis.getConfidence());
        complaint.setAiResultSource(analysis.getResultSource());

        Complaint saved = complaintRepository.save(complaint);

        // Record initial status history
        StatusHistory initialHistory = new StatusHistory(
                saved,
                null,
                ComplaintStatus.SUBMITTED,
                student,
                "Complaint filed successfully into CampusPulse."
        );
        statusHistoryRepository.save(initialHistory);

        return mapToResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getStudentComplaints(User student) {
        return complaintRepository.findByStudentOrderByCreatedAtDesc(student)
                .stream()
                .map(c -> mapToResponse(c, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getStaffComplaints(String query, String statusStr, String categoryStr) {
        ComplaintStatus status = null;
        if (statusStr != null && !statusStr.isBlank() && !"ALL".equalsIgnoreCase(statusStr)) {
            try {
                status = ComplaintStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        ComplaintCategory category = null;
        if (categoryStr != null && !categoryStr.isBlank() && !"ALL".equalsIgnoreCase(categoryStr)) {
            category = ComplaintCategory.fromString(categoryStr);
        }

        String search = (query != null && !query.isBlank()) ? query.trim() : null;

        return complaintRepository.searchComplaints(search, status, category)
                .stream()
                .map(c -> mapToResponse(c, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintByTicketId(String ticketId, User currentUser) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ticket ID: " + ticketId));

        // Enforce ownership: Students can only view their own complaints
        if (currentUser.getRole() == Role.ROLE_STUDENT && !complaint.getStudent().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access Denied: You do not have permission to view this complaint.");
        }

        return mapToResponse(complaint, true);
    }

    @Transactional
    public ComplaintResponse updateStatus(String ticketId, StatusUpdateRequest request, User staff) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ticket ID: " + ticketId));

        ComplaintStatus targetStatus;
        try {
            targetStatus = ComplaintStatus.valueOf(request.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid status value: " + request.getStatus());
        }

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setStatus(targetStatus);

        if (targetStatus == ComplaintStatus.RESOLVED && complaint.getResolvedAt() == null) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        if (complaint.getAssignedStaff() == null && staff != null) {
            complaint.setAssignedStaff(staff);
        }

        Complaint updated = complaintRepository.save(complaint);

        // Record status history transition
        String remarks = (request.getRemarks() != null && !request.getRemarks().isBlank())
                ? request.getRemarks().trim()
                : "Status changed from " + oldStatus + " to " + targetStatus;

        StatusHistory history = new StatusHistory(updated, oldStatus, targetStatus, staff, remarks);
        statusHistoryRepository.save(history);

        // If remarks were explicitly provided by staff, record in remarks thread too
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            Remark remark = new Remark(updated, staff, request.getRemarks().trim(), true);
            remarkRepository.save(remark);
        }

        return mapToResponse(updated, true);
    }

    @Transactional
    public RemarkDto addRemark(String ticketId, RemarkRequest request, User currentUser) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ticket ID: " + ticketId));

        // Permission check
        if (currentUser.getRole() == Role.ROLE_STUDENT && !complaint.getStudent().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You cannot post remarks on another student's complaint.");
        }

        boolean isStaff = currentUser.getRole() == Role.ROLE_STAFF || currentUser.getRole() == Role.ROLE_ADMIN;

        Remark remark = new Remark(complaint, currentUser, request.getContent().trim(), isStaff);
        Remark saved = remarkRepository.save(remark);

        return new RemarkDto(
                saved.getId(),
                currentUser.getFullName(),
                isStaff ? "Staff" : "Student",
                saved.getContent(),
                saved.isStaffRemark(),
                saved.getTimestamp()
        );
    }

    public ComplaintResponse mapToResponse(Complaint c, boolean includeDetails) {
        ComplaintResponse dto = new ComplaintResponse();
        dto.setId(c.getId());
        dto.setTicketId(c.getTicketId());
        dto.setTitle(c.getTitle());
        dto.setCategory(c.getCategory().name());
        dto.setCategoryDisplayName(c.getCategory().getDisplayName());
        dto.setLocation(c.getLocation());
        dto.setSeverity(c.getSeverity().name());
        dto.setDescription(c.getDescription());
        dto.setPhotoUrl(c.getPhotoUrl());
        dto.setStatus(c.getStatus().name());
        dto.setPriorityScore(c.getPriorityScore());
        dto.setPriorityLevel(c.getPriorityLevel());
        dto.setPriorityExplanation(c.getPriorityExplanation());
        dto.setAiDepartment(c.getAiDepartment());
        dto.setAiSafetyConcern(c.isAiSafetyConcern());
        dto.setAiConfidence(c.getAiConfidence());
        dto.setAiResultSource(c.getAiResultSource());

        if (c.getStudent() != null) {
            dto.setStudentId(c.getStudent().getId());
            dto.setStudentName(c.getStudent().getFullName());
            dto.setStudentEmail(c.getStudent().getEmail());
            dto.setStudentRollOrStaffId(c.getStudent().getStudentOrStaffId());
        }

        if (c.getAssignedStaff() != null) {
            dto.setAssignedStaffId(c.getAssignedStaff().getId());
            dto.setAssignedStaffName(c.getAssignedStaff().getFullName());
        }

        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setResolvedAt(c.getResolvedAt());

        if (includeDetails) {
            // Load timeline
            List<StatusHistoryDto> timeline = statusHistoryRepository.findByComplaintOrderByTimestampAsc(c)
                    .stream()
                    .map(h -> new StatusHistoryDto(
                            h.getId(),
                            h.getPreviousStatus() != null ? h.getPreviousStatus().name() : null,
                            h.getNewStatus().name(),
                            h.getActedBy() != null ? h.getActedBy().getFullName() : "System",
                            h.getActedBy() != null && (h.getActedBy().getRole() == Role.ROLE_STAFF || h.getActedBy().getRole() == Role.ROLE_ADMIN) ? "Staff" : "Student",
                            h.getRemarks(),
                            h.getTimestamp()
                    ))
                    .collect(Collectors.toList());
            dto.setTimeline(timeline);

            // Load remarks
            List<RemarkDto> remarks = remarkRepository.findByComplaintOrderByTimestampAsc(c)
                    .stream()
                    .map(r -> new RemarkDto(
                            r.getId(),
                            r.getUser().getFullName(),
                            r.isStaffRemark() ? "Staff" : "Student",
                            r.getContent(),
                            r.isStaffRemark(),
                            r.getTimestamp()
                    ))
                    .collect(Collectors.toList());
            dto.setRemarks(remarks);
        }

        return dto;
    }
}
