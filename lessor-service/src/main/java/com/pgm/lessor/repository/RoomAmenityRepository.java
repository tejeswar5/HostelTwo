package com.pgm.lessor.repository;

import com.pgm.lessor.entity.RoomAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, Long> {
    List<RoomAmenity> findByRoomId(Long roomId);
}
