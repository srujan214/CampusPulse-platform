package com.campuspulse;

import com.campuspulse.dto.ComplaintRequest;
import com.campuspulse.dto.ComplaintResponse;
import com.campuspulse.dto.StatusUpdateRequest;
import com.campuspulse.entity.Complaint;
import com.campuspulse.entity.ComplaintStatus;
import com.campuspulse.entity.StatusHistory;
import com.campuspulse.entity.User;
import com.campuspulse.repository.ComplaintRepository;
import com.campuspulse.repository.StatusHistoryRepository;
import com.campuspulse.repository.UserRepository;
import com.campuspulse.service.ComplaintService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabasePersistenceTest {

    @Test
    @DisplayName("Verification of Persistence Across Independent Application Lifecycles")
    void testFileBasedPersistenceAcrossRestarts() {
        String testTicketId;

        // Step 1: Start Application (Session 1)
        ConfigurableApplicationContext context1 = SpringApplication.run(CampusPulseApplication.class);
        try {
            ComplaintService complaintService1 = context1.getBean(ComplaintService.class);
            UserRepository userRepo1 = context1.getBean(UserRepository.class);

            // Step 2 & 3: Submit a new complaint as student Alex
            User studentAlex = userRepo1.findByEmail("alex.student@campuspulse.edu").orElseThrow();
            ComplaintRequest req = new ComplaintRequest();
            req.setTitle("Persistence Restart Verification Issue");
            req.setCategory("ELECTRICAL");
            req.setLocation("Block-B");
            req.setSeverity("HIGH");
            req.setDescription("Testing that this record survives complete application shutdowns.");

            ComplaintResponse response = complaintService1.submitComplaint(req, null, studentAlex);
            testTicketId = response.getTicketId();
            assertNotNull(testTicketId);
            System.out.println("Session 1: Submitted complaint with ticket ID: " + testTicketId);

        } finally {
            // Step 5: Stop the backend (Shutdown Context 1)
            context1.close();
            System.out.println("Session 1: Backend closed completely.");
        }

        // Step 6: Start it again (Session 2)
        ConfigurableApplicationContext context2 = SpringApplication.run(CampusPulseApplication.class);
        try {
            ComplaintRepository complaintRepo2 = context2.getBean(ComplaintRepository.class);
            ComplaintService complaintService2 = context2.getBean(ComplaintService.class);
            UserRepository userRepo2 = context2.getBean(UserRepository.class);

            // Step 7: Confirm the complaint still exists
            Complaint complaint = complaintRepo2.findByTicketId(testTicketId).orElse(null);
            assertNotNull(complaint, "Complaint must survive backend restart!");
            assertEquals("Persistence Restart Verification Issue", complaint.getTitle());
            assertEquals(ComplaintStatus.SUBMITTED, complaint.getStatus());
            System.out.println("Session 2: Confirmed complaint exists after restart: " + testTicketId);

            // Step 8: Update its status as staff
            User staffRahul = userRepo2.findByEmail("rahul.staff@campuspulse.edu").orElseThrow();
            StatusUpdateRequest updateReq = new StatusUpdateRequest("IN_PROGRESS", "Staff dispatched after restart verification");
            complaintService2.updateStatus(testTicketId, updateReq, staffRahul);

        } finally {
            // Step 9: Shutdown Context 2
            context2.close();
            System.out.println("Session 2: Backend closed completely.");
        }

        // Step 10: Restart again (Session 3) and confirm status & history remain intact
        ConfigurableApplicationContext context3 = SpringApplication.run(CampusPulseApplication.class);
        try {
            ComplaintRepository complaintRepo3 = context3.getBean(ComplaintRepository.class);
            StatusHistoryRepository historyRepo3 = context3.getBean(StatusHistoryRepository.class);

            Complaint reloaded = complaintRepo3.findByTicketId(testTicketId).orElse(null);
            assertNotNull(reloaded, "Complaint must survive second restart!");
            assertEquals(ComplaintStatus.IN_PROGRESS, reloaded.getStatus(), "Updated status must be preserved!");

            List<StatusHistory> history = historyRepo3.findByComplaintOrderByTimestampAsc(reloaded);
            assertTrue(history.size() >= 2, "Status transition history must be preserved!");
            assertEquals(ComplaintStatus.IN_PROGRESS, history.get(history.size() - 1).getNewStatus());
            System.out.println("Session 3: Persistence successfully verified across restarts for ticket: " + testTicketId);

        } finally {
            context3.close();
        }
    }
}
