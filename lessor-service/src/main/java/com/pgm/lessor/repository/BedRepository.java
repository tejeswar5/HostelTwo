package com.pgm.lessor.repository;

import com.pgm.lessor.entity.Bed;
import com.pgm.lessor.entity.BedStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByHostelIdOrderByFloorIdAscRoomIdAscBedNumberAsc(Long hostelId);

    boolean existsByRoomIdAndBedNumber(Long roomId, String bedNumber);

    long countByHostelIdAndStatus(Long hostelId, BedStatus status);

    /**
     * Row-locks the bed for the duration of the caller's transaction so a concurrent
     * approval/maintenance-toggle/booking-insert against the same bed serializes
     * instead of racing. Paired with the {@code uq_active_booking_per_bed} partial
     * unique index as a second, DB-enforced line of defense.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bed b where b.id = :id")
    Optional<Bed> findByIdForUpdate(@Param("id") Long id);
}
