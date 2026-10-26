package com.campuspulse.repository;

import com.campuspulse.entity.Role;
import com.campuspulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.studentOrStaffId = :id")
    Optional<User> findByStudentOrStaffId(@Param("id") String id);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.studentOrStaffId = :id")
    boolean existsByStudentOrStaffId(@Param("id") String id);

    List<User> findByRole(Role role);
}
