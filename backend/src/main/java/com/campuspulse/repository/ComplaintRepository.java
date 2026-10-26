package com.campuspulse.repository;

import com.campuspulse.entity.Complaint;
import com.campuspulse.entity.ComplaintCategory;
import com.campuspulse.entity.ComplaintStatus;
import com.campuspulse.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Optional<Complaint> findByTicketId(String ticketId);

    List<Complaint> findByStudentOrderByCreatedAtDesc(User student);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    List<Complaint> findTop5ByStudentOrderByCreatedAtDesc(User student);

    List<Complaint> findTop10ByOrderByCreatedAtDesc();

    long countByStatus(ComplaintStatus status);

    long countByCategory(ComplaintCategory category);

    long countByStudent(User student);

    long countByStudentAndStatus(User student, ComplaintStatus status);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status NOT IN ('RESOLVED', 'CLOSED') AND c.createdAt <= :deadline")
    long countOverdueComplaints(@Param("deadline") LocalDateTime deadline);

    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countGroupByCategory();

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:query IS NULL OR LOWER(c.ticketId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.location) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:category IS NULL OR c.category = :category) " +
           "ORDER BY c.priorityScore DESC, c.createdAt DESC")
    List<Complaint> searchComplaints(
            @Param("query") String query,
            @Param("status") ComplaintStatus status,
            @Param("category") ComplaintCategory category);
}
