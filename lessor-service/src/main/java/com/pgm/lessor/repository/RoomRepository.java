package com.pgm.lessor.repository;

import com.pgm.lessor.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHostelIdOrderByFloorIdAscRoomNumberAsc(Long hostelId);

    List<Room> findByFloorIdOrderByRoomNumberAsc(Long floorId);

    boolean existsByFloorIdAndRoomNumber(Long floorId, String roomNumber);
}
