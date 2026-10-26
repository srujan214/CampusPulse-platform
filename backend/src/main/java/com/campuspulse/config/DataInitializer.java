package com.campuspulse.config;

import com.campuspulse.entity.*;
import com.campuspulse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ComplaintRepository complaintRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final RemarkRepository remarkRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           LocationRepository locationRepository,
                           ComplaintRepository complaintRepository,
                           StatusHistoryRepository statusHistoryRepository,
                           RemarkRepository remarkRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.remarkRepository = remarkRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Verifying CampusPulse database initialization...");

        // 1. Initialize Default Campus Locations if not present
        initializeLocations();

        // 2. Initialize Demonstration Accounts if not present
        User staffRahul = initializeStaffAccount();
        User studentAlex = initializeStudentAlex();
        User studentPriya = initializeStudentPriya();

        // 3. Initialize Demonstration Complaints only if database is completely empty
        if (complaintRepository.count() == 0) {
            initializeDemoComplaints(studentAlex, studentPriya, staffRahul);
            logger.info("Demo complaints initialized successfully.");
        } else {
            logger.info("Existing database complaints found (count: {}). Preserving all records.", complaintRepository.count());
        }
    }

    private void initializeLocations() {
        List<LocationItem> defaultLocations = List.of(
                new LocationItem("Lab-1", "Block A", "Ground Floor", "Computer Science Lab 1"),
                new LocationItem("Lab-2", "Block A", "1st Floor", "Electronics & IoT Systems Lab"),
                new LocationItem("Library", "Central Block", "2nd Floor", "Central Academic Library"),
                new LocationItem("Block-A", "Block A", "Ground Floor", "Engineering & Science Wing"),
                new LocationItem("Block-B", "Block B", "Ground Floor", "Administrative & Humanities Block"),
                new LocationItem("Classroom", "Block A", "Room 201", "Lecture Hall 201"),
                new LocationItem("Canteen", "Student Amenities Block", "Ground Floor", "Central Food Court"),
                new LocationItem("Washroom", "Block B", "1st Floor", "Main Restroom Complex"),
                new LocationItem("Block A - Room 201", "Block A", "2nd Floor", "Smart Classroom AV Lab")
        );

        for (LocationItem loc : defaultLocations) {
            if (!locationRepository.existsByNameIgnoreCase(loc.getName())) {
                locationRepository.save(loc);
            }
        }
    }

    private User initializeStaffAccount() {
        return userRepository.findByEmail("rahul.staff@campuspulse.edu")
                .orElseGet(() -> {
                    User staff = new User();
                    staff.setEmail("rahul.staff@campuspulse.edu");
                    staff.setFullName("Rahul Sharma");
                    staff.setStudentOrStaffId("STF-201");
                    staff.setPassword(passwordEncoder.encode("Password@123"));
                    staff.setRole(Role.ROLE_STAFF);
                    staff.setDepartment("Maintenance & Facilities");
                    return userRepository.save(staff);
                });
    }

    private User initializeStudentAlex() {
        return userRepository.findByEmail("alex.student@campuspulse.edu")
                .orElseGet(() -> {
                    User student = new User();
                    student.setEmail("alex.student@campuspulse.edu");
                    student.setFullName("Alex Rivera");
                    student.setStudentOrStaffId("STU-2024-001");
                    student.setPassword(passwordEncoder.encode("Password@123"));
                    student.setRole(Role.ROLE_STUDENT);
                    student.setDepartment("Computer Science");
                    return userRepository.save(student);
                });
    }

    private User initializeStudentPriya() {
        return userRepository.findByEmail("priya.sharma@campuspulse.edu")
                .orElseGet(() -> {
                    User student = new User();
                    student.setEmail("priya.sharma@campuspulse.edu");
                    student.setFullName("Priya Sharma");
                    student.setStudentOrStaffId("STU-2024-002");
                    student.setPassword(passwordEncoder.encode("Password@123"));
                    student.setRole(Role.ROLE_STUDENT);
                    student.setDepartment("Electronics");
                    return userRepository.save(student);
                });
    }

    private void initializeDemoComplaints(User alex, User priya, User rahul) {
        LocalDateTime now = LocalDateTime.now();

        // 1. CP-1005: Projector not working (matches reference screen 4 & 6)
        Complaint cp1005 = new Complaint();
        cp1005.setTicketId("CP-1005");
        cp1005.setTitle("Projector not working");
        cp1005.setCategory(ComplaintCategory.CLASSROOM_EQUIPMENT);
        cp1005.setLocation("Block A - Room 201");
        cp1005.setSeverity(ComplaintSeverity.HIGH);
        cp1005.setDescription("The main ceiling projector in Room 201 fails to turn on and flashes red warning LED. Lectures are being disrupted.");
        cp1005.setStatus(ComplaintStatus.IN_PROGRESS);
        cp1005.setPriorityScore(78);
        cp1005.setPriorityLevel("High");
        cp1005.setPriorityExplanation("High user severity selected (+30 pts) | Standard safety assessment (+5 pts) | Smart classroom AV impact (+15 pts) | Age (12 pts) | Overdue (16 pts)");
        cp1005.setAiDepartment("AV & Facility Team");
        cp1005.setAiSafetyConcern(false);
        cp1005.setAiConfidence(0.92);
        cp1005.setAiResultSource("FALLBACK_ENGINE");
        cp1005.setStudent(alex);
        cp1005.setAssignedStaff(rahul);
        cp1005.setCreatedAt(now.minusHours(6));
        cp1005.setUpdatedAt(now.minusHours(1));
        Complaint saved1005 = complaintRepository.save(cp1005);

        // Timeline for CP-1005
        statusHistoryRepository.save(new StatusHistory(saved1005, null, ComplaintStatus.SUBMITTED, alex, "Issue reported via student portal."));
        statusHistoryRepository.save(new StatusHistory(saved1005, ComplaintStatus.SUBMITTED, ComplaintStatus.ACKNOWLEDGED, rahul, "Acknowledged by Maintenance desk."));
        statusHistoryRepository.save(new StatusHistory(saved1005, ComplaintStatus.ACKNOWLEDGED, ComplaintStatus.IN_PROGRESS, rahul, "Dispatched technician to check projector lamp."));

        // Remarks for CP-1005
        remarkRepository.save(new Remark(saved1005, rahul, "Checking the projector. Replacement bulb ordered, will update soon.", true));
        remarkRepository.save(new Remark(saved1005, priya, "Thanks for the quick response! We have another lecture at 3 PM.", false));

        // 2. CP-1004: Water leakage
        Complaint cp1004 = new Complaint();
        cp1004.setTicketId("CP-1004");
        cp1004.setTitle("Water leakage");
        cp1004.setCategory(ComplaintCategory.PLUMBING);
        cp1004.setLocation("Block B - GF");
        cp1004.setSeverity(ComplaintSeverity.MEDIUM);
        cp1004.setDescription("Continuous water leakage from ceiling pipe near the main lobby entrance.");
        cp1004.setStatus(ComplaintStatus.SUBMITTED);
        cp1004.setPriorityScore(48);
        cp1004.setPriorityLevel("Medium");
        cp1004.setPriorityExplanation("Medium user severity selected (+20 pts) | Safety impact: Hazard keyword 'water leak' (+35 pts) | Common public campus facility (+8 pts)");
        cp1004.setAiDepartment("Plumbing & Sanitation");
        cp1004.setAiSafetyConcern(true);
        cp1004.setAiConfidence(0.88);
        cp1004.setAiResultSource("FALLBACK_ENGINE");
        cp1004.setStudent(alex);
        cp1004.setCreatedAt(now.minusHours(12));
        Complaint saved1004 = complaintRepository.save(cp1004);
        statusHistoryRepository.save(new StatusHistory(saved1004, null, ComplaintStatus.SUBMITTED, alex, "Issue registered into CampusPulse."));

        // 3. CP-1003: WiFi not working
        Complaint cp1003 = new Complaint();
        cp1003.setTicketId("CP-1003");
        cp1003.setTitle("WiFi not working");
        cp1003.setCategory(ComplaintCategory.INTERNET);
        cp1003.setLocation("Library");
        cp1003.setSeverity(ComplaintSeverity.LOW);
        cp1003.setDescription("Students on the 2nd floor library reading room cannot connect to Campus-Secure WiFi.");
        cp1003.setStatus(ComplaintStatus.RESOLVED);
        cp1003.setPriorityScore(32);
        cp1003.setPriorityLevel("Low");
        cp1003.setPriorityExplanation("Low user severity selected (+15 pts) | Standard safety assessment (+5 pts) | Common public facility (+8 pts)");
        cp1003.setAiDepartment("IT & Network Support");
        cp1003.setAiSafetyConcern(false);
        cp1003.setAiConfidence(0.90);
        cp1003.setAiResultSource("FALLBACK_ENGINE");
        cp1003.setStudent(alex);
        cp1003.setAssignedStaff(rahul);
        cp1003.setCreatedAt(now.minusDays(2));
        cp1003.setResolvedAt(now.minusDays(1));
        Complaint saved1003 = complaintRepository.save(cp1003);
        statusHistoryRepository.save(new StatusHistory(saved1003, null, ComplaintStatus.SUBMITTED, alex, "Reported WiFi disconnectivity in Library."));
        statusHistoryRepository.save(new StatusHistory(saved1003, ComplaintStatus.SUBMITTED, ComplaintStatus.IN_PROGRESS, rahul, "Access point restarted."));
        statusHistoryRepository.save(new StatusHistory(saved1003, ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED, rahul, "Firmware updated and 5GHz radio restored."));
        remarkRepository.save(new Remark(saved1003, rahul, "Access point 2B rebooted and signal strength verified.", true));

        // 4. CP-1002: Broken chair
        Complaint cp1002 = new Complaint();
        cp1002.setTicketId("CP-1002");
        cp1002.setTitle("Broken chair");
        cp1002.setCategory(ComplaintCategory.INFRASTRUCTURE);
        cp1002.setLocation("Block C - R102");
        cp1002.setSeverity(ComplaintSeverity.LOW);
        cp1002.setDescription("Desk chair in third row has broken armrest.");
        cp1002.setStatus(ComplaintStatus.RESOLVED);
        cp1002.setPriorityScore(28);
        cp1002.setPriorityLevel("Low");
        cp1002.setPriorityExplanation("Low user severity selected (+15 pts) | Standard classroom furniture (+5 pts)");
        cp1002.setAiDepartment("Civil Maintenance");
        cp1002.setAiSafetyConcern(false);
        cp1002.setAiConfidence(0.85);
        cp1002.setAiResultSource("FALLBACK_ENGINE");
        cp1002.setStudent(alex);
        cp1002.setAssignedStaff(rahul);
        cp1002.setCreatedAt(now.minusDays(4));
        cp1002.setResolvedAt(now.minusDays(3));
        Complaint saved1002 = complaintRepository.save(cp1002);
        statusHistoryRepository.save(new StatusHistory(saved1002, null, ComplaintStatus.SUBMITTED, alex, "Reported broken chair."));
        statusHistoryRepository.save(new StatusHistory(saved1002, ComplaintStatus.SUBMITTED, ComplaintStatus.RESOLVED, rahul, "Replaced with newly cushioned desk chair."));

        // 5. CP-1001: Electrical sparks in laboratory (Critical Safety Hazard example)
        Complaint cp1001 = new Complaint();
        cp1001.setTicketId("CP-1001");
        cp1001.setTitle("Electrical sparks in laboratory");
        cp1001.setCategory(ComplaintCategory.ELECTRICAL);
        cp1001.setLocation("Lab-2");
        cp1001.setSeverity(ComplaintSeverity.CRITICAL);
        cp1001.setDescription("Exposed wire and visible electrical sparks emitting from power strip on workbench #4.");
        cp1001.setStatus(ComplaintStatus.ACKNOWLEDGED);
        cp1001.setPriorityScore(94);
        cp1001.setPriorityLevel("Critical");
        cp1001.setPriorityExplanation("Critical user severity selected (+45 pts) | Safety impact: Hazard keyword 'sparks' identified (+35 pts) | High-traffic laboratory zone (+12 pts)");
        cp1001.setAiDepartment("Electrical Maintenance");
        cp1001.setAiSafetyConcern(true);
        cp1001.setAiConfidence(0.97);
        cp1001.setAiResultSource("FALLBACK_ENGINE");
        cp1001.setStudent(priya);
        cp1001.setAssignedStaff(rahul);
        cp1001.setCreatedAt(now.minusHours(3));
        Complaint saved1001 = complaintRepository.save(cp1001);
        statusHistoryRepository.save(new StatusHistory(saved1001, null, ComplaintStatus.SUBMITTED, priya, "Urgent safety report submitted."));
        statusHistoryRepository.save(new StatusHistory(saved1001, ComplaintStatus.SUBMITTED, ComplaintStatus.ACKNOWLEDGED, rahul, "Circuit breaker tripped remotely. Senior electrician en route."));
        remarkRepository.save(new Remark(saved1001, rahul, "Circuit isolated. Do not touch workbench #4 until green safety tag is applied.", true));
    }
}
