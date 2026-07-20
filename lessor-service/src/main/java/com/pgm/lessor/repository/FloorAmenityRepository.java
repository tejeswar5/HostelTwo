package com.pgm.lessor.repository;

import com.pgm.lessor.entity.FloorAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorAmenityRepository extends JpaRepository<FloorAmenity, Long> {
    List<FloorAmenity> findByFloorId(Long floorId);

    List<FloorAmenity> findByFloorHostelId(Long hostelId);
}
