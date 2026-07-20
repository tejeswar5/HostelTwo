package com.pgm.renter.repository;

import com.pgm.renter.entity.Booking;
import com.pgm.renter.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRenterIdOrderByCreatedAtDesc(Long renterId);

    Optional<Booking> findByBookingRef(UUID bookingRef);

    List<Booking> findByHostelIdAndStatusAndRenterIdIn(Long hostelId, BookingStatus status, List<Long> renterIds);
}
