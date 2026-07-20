package com.pgm.lessor.repository;

import com.pgm.lessor.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findByHostelIdOrderByFloorNumberAsc(Long hostelId);

    boolean existsByHostelIdAndFloorNumber(Long hostelId, Integer floorNumber);
}
