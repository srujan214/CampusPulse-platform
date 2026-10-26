package com.campuspulse;

import com.campuspulse.ai.AiAnalysisResult;
import com.campuspulse.ai.AiService;
import com.campuspulse.ai.FallbackScoringEngine;
import com.campuspulse.dto.*;
import com.campuspulse.entity.ComplaintCategory;
import com.campuspulse.entity.ComplaintSeverity;
import com.campuspulse.entity.ComplaintStatus;
import com.campuspulse.entity.User;
import com.campuspulse.qr.QrCodeGenerator;
import com.campuspulse.repository.ComplaintRepository;
import com.campuspulse.repository.UserRepository;
import com.campuspulse.service.AuthService;
import com.campuspulse.service.ComplaintService;
import com.campuspulse.service.QrService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CampusPulseApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private FallbackScoringEngine fallbackScoringEngine;

    @Autowired
    private AiService aiService;

    @Autowired
    private QrCodeGenerator qrCodeGenerator;

    @Autowired
    private QrService qrService;

    @Test
    @DisplayName("1. Application Context & Initial Data Loads Successfully")
    void contextLoads() {
        assertNotNull(userRepository);
        assertNotNull(complaintRepository);
        assertTrue(userRepository.count() >= 2, "Default staff and student accounts should exist");
        assertTrue(complaintRepository.count() >= 1, "Demo exhibition complaints should exist");
    }

    @Test
    @DisplayName("2. Student and Staff Authentication Works with Hashed Passwords")
    void testAuthentication() {
        // Test Student Login
        LoginRequest studentLogin = new LoginRequest("alex.student@campuspulse.edu", "Password@123");
        AuthResponse studentAuth = authService.authenticateUser(studentLogin);
        assertNotNull(studentAuth.getToken());
        assertEquals("ROLE_STUDENT", studentAuth.getRole());
        assertEquals("Alex Rivera", studentAuth.getFullName());

        // Test Staff Login
        LoginRequest staffLogin = new LoginRequest("rahul.staff@campuspulse.edu", "Password@123");
        AuthResponse staffAuth = authService.authenticateUser(staffLogin);
        assertNotNull(staffAuth.getToken());
        assertEquals("ROLE_STAFF", staffAuth.getRole());
    }

    @Test
    @DisplayName("3. Deterministic Local Fallback Engine Scores Safety Hazards Correctly")
    void testFallbackScoringEngine() {
        // High hazard test case: electrical sparks in laboratory
        AiAnalysisResult result = fallbackScoringEngine.analyze(
                "Electrical sparks in laboratory",
                "Visible sparks and smoke coming from wall socket",
                ComplaintCategory.ELECTRICAL,
                "Lab-2",
                ComplaintSeverity.CRITICAL
        );

        assertNotNull(result);
        assertEquals("FALLBACK_ENGINE", result.getResultSource());
        assertTrue(result.isSafetyConcern(), "Should detect safety concern from hazard keywords");
        assertEquals("Critical", result.getSuggestedPriority());
        assertTrue(result.getPriorityScore() >= 85, "Critical hazard should score 85+ points");
        assertEquals("Electrical Maintenance", result.getDepartment());

        // Moderate disruption test case: broken chair in classroom
        AiAnalysisResult lowResult = fallbackScoringEngine.analyze(
                "Broken chair",
                "Desk chair armrest loose",
                ComplaintCategory.INFRASTRUCTURE,
                "Classroom",
                ComplaintSeverity.LOW
        );
        assertEquals("Low", lowResult.getSuggestedPriority());
        assertFalse(lowResult.isSafetyConcern());
    }

    @Test
    @DisplayName("4. Complaint Submission, Unique Ticket ID & Status Timeline Workflow")
    void testComplaintSubmissionAndLifecycle() {
        User student = userRepository.findByEmail("alex.student@campuspulse.edu").orElseThrow();
        User staff = userRepository.findByEmail("rahul.staff@campuspulse.edu").orElseThrow();

        ComplaintRequest req = new ComplaintRequest();
        req.setTitle("Water leakage in Washroom");
        req.setCategory("PLUMBING");
        req.setLocation("Washroom");
        req.setSeverity("HIGH");
        req.setDescription("Severe ceiling leak with water accumulating on tile floor");

        // Submit Complaint
        ComplaintResponse submitted = complaintService.submitComplaint(req, null, student);
        assertNotNull(submitted.getTicketId());
        assertTrue(submitted.getTicketId().startsWith("CP-"));
        assertEquals("SUBMITTED", submitted.getStatus());
        assertEquals("Washroom", submitted.getLocation());

        // Verify Status Stepper Timeline contains SUBMITTED step
        assertFalse(submitted.getTimeline().isEmpty());
        assertEquals("SUBMITTED", submitted.getTimeline().get(0).getNewStatus());

        // Staff updates status to IN_PROGRESS
        StatusUpdateRequest updateReq = new StatusUpdateRequest("IN_PROGRESS", "Plumbing maintenance team dispatched with tools");
        ComplaintResponse updated = complaintService.updateStatus(submitted.getTicketId(), updateReq, staff);
        assertEquals("IN_PROGRESS", updated.getStatus());
        assertTrue(updated.getTimeline().size() >= 2);

        // Staff marks RESOLVED
        StatusUpdateRequest resolveReq = new StatusUpdateRequest("RESOLVED", "Replaced cracked gasket. Leak completely stopped.");
        ComplaintResponse resolved = complaintService.updateStatus(submitted.getTicketId(), resolveReq, staff);
        assertEquals("RESOLVED", resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());
    }

    @Test
    @DisplayName("5. Java ZXing QR Code Generation Produces Valid Images and Base64")
    void testQrCodeGeneration() throws Exception {
        byte[] qrBytes = qrCodeGenerator.generateQrCodeImage("https://campuspulse.edu/report?location=Lab-2", 200, 200);
        assertNotNull(qrBytes);
        assertTrue(qrBytes.length > 500, "PNG image bytes should be valid and non-empty");

        String qrBase64 = qrCodeGenerator.generateQrCodeBase64("https://campuspulse.edu/report?location=Lab-2", 200, 200);
        assertNotNull(qrBase64);
        assertTrue(qrBase64.startsWith("data:image/png;base64,"));

        List<QrLocationDto> allLocations = qrService.getAllLocationQrs();
        assertFalse(allLocations.isEmpty(), "Configured campus locations should have QR codes");
    }
}
