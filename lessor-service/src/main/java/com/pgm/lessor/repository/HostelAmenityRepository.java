package com.pgm.lessor.repository;

import com.pgm.lessor.entity.HostelAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HostelAmenityRepository extends JpaRepository<HostelAmenity, Long> {
    List<HostelAmenity> findByHostelId(Long hostelId);
}
