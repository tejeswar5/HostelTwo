package com.pgm.lessor.service;

import com.pgm.lessor.audit.Auditable;
import com.pgm.lessor.dto.booking.BookingResponse;
import com.pgm.lessor.entity.*;
import com.pgm.lessor.exception.ConflictException;
import com.pgm.lessor.exception.NotFoundException;
import com.pgm.lessor.repository.BedRepository;
import com.pgm.lessor.repository.BookingRepository;
import com.pgm.lessor.repository.UserRepository;
import com.pgm.lessor.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Approving/rejecting a booking request is the other half of the double-booking
 * guard described in {@link BedRepository#findByIdForUpdate}:
 * the bed row is locked for the transaction so an approval can't race a concurrent
 * approval of a different pending request for the same bed. The request itself
 * arrives via Kafka from renter-service (see {@link com.pgm.lessor.listener.BookingRequestListener})
 * rather than a direct HTTP call, since renter-service no longer shares this database.
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BedRepository bedRepository;
    private final UserRepository userRepository;
    private final HostelService hostelService;
    private final EventPublisher eventPublisher;

    public BookingService(
            BookingRepository bookingRepository,
            BedRepository bedRepository,
            UserRepository userRepository,
            HostelService hostelService,
            EventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.bedRepository = bedRepository;
        this.userRepository = userRepository;
        this.hostelService = hostelService;
        this.eventPublisher = eventPublisher;
    }

    public List<BookingResponse> list(UserPrincipal principal, BookingStatus statusFilter) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        List<Booking> bookings = statusFilter == null
                ? bookingRepository.findByBedHostelIdOrderByCreatedAtDesc(hostel.getId())
                : bookingRepository.findByBedHostelIdAndStatusOrderByCreatedAtDesc(hostel.getId(), statusFilter);
        return bookings.stream().map(this::toResponse).toList();
    }

    @Transactional
    @Auditable(action = "BOOKING_APPROVED", entityType = "BOOKING")
    public BookingResponse approve(UserPrincipal principal, Long bookingId) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        Booking booking = bookingRepository.findByIdAndBedHostelId(bookingId, hostel.getId())
                .orElseThrow(() -> new NotFoundException("Booking request not found"));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("This booking request has already been decided");
        }
        Bed bed = bedRepository.findByIdForUpdate(booking.getBed().getId())
                .orElseThrow(() -> new NotFoundException("Bed not found"));

        booking.setStatus(BookingStatus.APPROVED);
        booking.setDecidedAt(LocalDateTime.now());
        booking.setDecidedBy(userRepository.getReferenceById(principal.userId()));
        bed.markBooked(booking.getRequestedCheckIn(), booking.getRequestedCheckOut());

        eventPublisher.publishBookingDecision("APPROVED", booking.getBookingRef().toString(), bed.getId(),
                bed.getBedNumber(), booking.getRenterId(), booking.getRenterName(), null);
        return toResponse(booking);
    }

    @Transactional
    @Auditable(action = "BOOKING_REJECTED", entityType = "BOOKING")
    public BookingResponse reject(UserPrincipal principal, Long bookingId) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        Booking booking = bookingRepository.findByIdAndBedHostelId(bookingId, hostel.getId())
                .orElseThrow(() -> new NotFoundException("Booking request not found"));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("This booking request has already been decided");
        }
        booking.setStatus(BookingStatus.REJECTED);
        booking.setDecidedAt(LocalDateTime.now());
        booking.setDecidedBy(userRepository.getReferenceById(principal.userId()));

        eventPublisher.publishBookingDecision("REJECTED", booking.getBookingRef().toString(), booking.getBed().getId(),
                booking.getBed().getBedNumber(), booking.getRenterId(), booking.getRenterName(), null);
        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBed().getId(),
                booking.getBed().getBedNumber(),
                booking.getBed().getRoom().getRoomNumber(),
                booking.getRenterId(),
                booking.getRenterName(),
                booking.getRenterPhone(),
                booking.getStatus(),
                booking.getRequestedCheckIn(),
                booking.getRequestedCheckOut(),
                booking.getCreatedAt());
    }
}
