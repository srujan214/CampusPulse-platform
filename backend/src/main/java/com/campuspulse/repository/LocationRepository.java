package com.campuspulse.repository;

import com.campuspulse.entity.LocationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationItem, Long> {
    Optional<LocationItem> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
