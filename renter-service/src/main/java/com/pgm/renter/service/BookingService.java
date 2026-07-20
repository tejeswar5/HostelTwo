package com.pgm.renter.service;

import com.pgm.renter.audit.Auditable;
import com.pgm.renter.dto.booking.BookingResponse;
import com.pgm.renter.dto.booking.CreateBookingRequest;
import com.pgm.renter.entity.Booking;
import com.pgm.renter.entity.BookingStatus;
import com.pgm.renter.entity.User;
import com.pgm.renter.repository.BookingRepository;
import com.pgm.renter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * lessor-service, not this one, owns the bed and enforces "at most one active
 * booking per bed" - that's the whole point of keeping bed/booking's write
 * authority together in the service that owns the scarce resource. This method
 * just records an optimistic PENDING row locally (for instant "My bookings"
 * feedback) and asks lessor-service to make the real decision over Kafka;
 * {@link com.pgm.renter.listener.BookingDecisionListener} updates this row once
 * that decision comes back, possibly flipping straight to REJECTED if the bed
 * turned out to be unavailable by the time lessor-service processed the request.
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository, EventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Auditable(action = "BOOKING_REQUESTED", entityType = "BOOKING")
    public BookingResponse create(Long renterId, CreateBookingRequest request) {
        User renter = userRepository.findById(renterId).orElseThrow();
        UUID bookingRef = UUID.randomUUID();

        Booking booking = Booking.builder()
                .bookingRef(bookingRef)
                .bedId(request.bedId())
                .bedNumber(request.bedNumber())
                .roomNumber(request.roomNumber())
                .hostelId(request.hostelId())
                .hostelName(request.hostelName())
                .renter(renter)
                .status(BookingStatus.PENDING)
                .requestedCheckIn(request.checkIn())
                .requestedCheckOut(request.checkOut())
                .build();
        booking = bookingRepository.save(booking);

        eventPublisher.publishBookingRequested(
                bookingRef.toString(),
                request.bedId(),
                renterId,
                renter.getFname() + " " + renter.getLname(),
                renter.getEmail(),
                renter.getPhoneNumber(),
                request.checkIn(),
                request.checkOut());

        return toResponse(booking);
    }

    public List<BookingResponse> myBookings(Long renterId) {
        return bookingRepository.findByRenterIdOrderByCreatedAtDesc(renterId).stream().map(this::toResponse).toList();
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBedId(),
                booking.getBedNumber(),
                booking.getHostelName(),
                booking.getStatus(),
                booking.getRequestedCheckIn(),
                booking.getRequestedCheckOut(),
                booking.getCreatedAt());
    }
}
