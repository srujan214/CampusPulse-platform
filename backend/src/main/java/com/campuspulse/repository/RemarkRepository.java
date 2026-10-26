package com.campuspulse.repository;

import com.campuspulse.entity.Complaint;
import com.campuspulse.entity.Remark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemarkRepository extends JpaRepository<Remark, Long> {
    List<Remark> findByComplaintOrderByTimestampAsc(Complaint complaint);
}
