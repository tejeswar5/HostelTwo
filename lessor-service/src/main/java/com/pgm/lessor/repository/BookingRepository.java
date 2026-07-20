package com.pgm.lessor.repository;

import com.pgm.lessor.entity.Booking;
import com.pgm.lessor.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBedHostelIdOrderByCreatedAtDesc(Long hostelId);

    List<Booking> findByBedHostelIdAndStatusOrderByCreatedAtDesc(Long hostelId, BookingStatus status);

    Optional<Booking> findByIdAndBedHostelId(Long id, Long hostelId);

    boolean existsByBedIdAndStatusIn(Long bedId, List<BookingStatus> statuses);

    Optional<Booking> findFirstByBedIdAndStatusOrderByDecidedAtDesc(Long bedId, BookingStatus status);

    long countByBedHostelIdAndStatus(Long hostelId, BookingStatus status);
}
