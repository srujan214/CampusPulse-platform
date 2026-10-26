package com.campuspulse.service;

import com.campuspulse.dto.AnalyticsResponse;
import com.campuspulse.entity.ComplaintCategory;
import com.campuspulse.entity.ComplaintStatus;
import com.campuspulse.entity.User;
import com.campuspulse.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ComplaintRepository complaintRepository;

    public AnalyticsService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getStaffAnalytics() {
        AnalyticsResponse resp = new AnalyticsResponse();

        long total = complaintRepository.count();
        long submitted = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        long acknowledged = complaintRepository.countByStatus(ComplaintStatus.ACKNOWLEDGED);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED) +
                         complaintRepository.countByStatus(ComplaintStatus.CLOSED);

        // Overdue: open complaints older than 48 hours
        LocalDateTime deadline = LocalDateTime.now().minusHours(48);
        long overdue = complaintRepository.countOverdueComplaints(deadline);

        resp.setTotal(total);
        resp.setSubmitted(submitted);
        resp.setOpen(submitted + acknowledged);
        resp.setInProgress(inProgress);
        resp.setResolved(resolved);
        resp.setOverdue(overdue);

        // By Status breakdown for Donut Chart
        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("Submitted", submitted);
        statusMap.put("In Progress", inProgress);
        statusMap.put("Resolved", resolved);
        statusMap.put("Overdue", overdue);
        resp.setComplaintsByStatus(statusMap);

        // By Category breakdown for Bar Chart
        Map<String, Long> categoryMap = new HashMap<>();
        for (ComplaintCategory cat : ComplaintCategory.values()) {
            categoryMap.put(cat.getDisplayName(), 0L);
        }
        List<Object[]> catCounts = complaintRepository.countGroupByCategory();
        for (Object[] row : catCounts) {
            ComplaintCategory cat = (ComplaintCategory) row[0];
            Long cnt = (Long) row[1];
            categoryMap.put(cat.getDisplayName(), cnt);
        }
        resp.setComplaintsByCategory(categoryMap);

        return resp;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStudentStats(User student) {
        Map<String, Long> stats = new HashMap<>();
        long total = complaintRepository.countByStudent(student);
        long submitted = complaintRepository.countByStudentAndStatus(student, ComplaintStatus.SUBMITTED);
        long ack = complaintRepository.countByStudentAndStatus(student, ComplaintStatus.ACKNOWLEDGED);
        long inProg = complaintRepository.countByStudentAndStatus(student, ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStudentAndStatus(student, ComplaintStatus.RESOLVED) +
                         complaintRepository.countByStudentAndStatus(student, ComplaintStatus.CLOSED);

        stats.put("total", total);
        stats.put("open", submitted + ack);
        stats.put("inProgress", inProg);
        stats.put("resolved", resolved);
        return stats;
    }
}
