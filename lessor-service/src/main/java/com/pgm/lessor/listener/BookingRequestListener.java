package com.pgm.lessor.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.lessor.entity.Bed;
import com.pgm.lessor.entity.BedStatus;
import com.pgm.lessor.entity.Booking;
import com.pgm.lessor.entity.BookingStatus;
import com.pgm.lessor.entity.Role;
import com.pgm.lessor.event.BookingRequestedEvent;
import com.pgm.lessor.repository.BedRepository;
import com.pgm.lessor.repository.BookingRepository;
import com.pgm.lessor.repository.UserRepository;
import com.pgm.lessor.service.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * lessor-service owns the bed, so it - not renter-service - is the one that
 * enforces "at most one active booking per bed". The bed row is locked for the
 * transaction and the DB's uq_active_booking_per_bed partial unique index is
 * still the backstop, exactly as it was when renter-service wrote this row
 * directly against the shared database. renter-service only ever finds out the
 * outcome afterward, via the booking-decisions event this publishes back.
 */
@Component
public class BookingRequestListener {

    private static final Logger log = LoggerFactory.getLogger(BookingRequestListener.class);

    private final BedRepository bedRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public BookingRequestListener(
            BedRepository bedRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            EventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        this.bedRepository = bedRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "booking-requests", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onBookingRequested(String payload) {
        BookingRequestedEvent event;
        try {
            event = objectMapper.readValue(payload, BookingRequestedEvent.class);
        } catch (Exception e) {
            log.error("Failed to parse booking-requests payload: {}", payload, e);
            return;
        }

        Bed bed = bedRepository.findByIdForUpdate(event.bedId()).orElse(null);
        if (bed == null || bed.getStatus() != BedStatus.AVAILABLE) {
            eventPublisher.publishBookingDecision("REJECTED", event.bookingRef(), event.bedId(),
                    bed == null ? null : bed.getBedNumber(), event.renterId(), event.renterName(), null);
            return;
        }

        Booking booking = Booking.builder()
                .bookingRef(UUID.fromString(event.bookingRef()))
                .bed(bed)
                .renterId(event.renterId())
                .renterName(event.renterName())
                .renterEmail(event.renterEmail())
                .renterPhone(event.renterPhone())
                .status(BookingStatus.PENDING)
                .requestedCheckIn(event.requestedCheckIn())
                .requestedCheckOut(event.requestedCheckOut())
                .build();
        try {
            bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            eventPublisher.publishBookingDecision("REJECTED", event.bookingRef(), event.bedId(),
                    bed.getBedNumber(), event.renterId(), event.renterName(), null);
            return;
        }

        Long lessorUserId = userRepository.findByTenantIdAndRole(bed.getHostel().getTenantId(), Role.LESSOR)
                .map(u -> u.getId())
                .orElse(null);
        if (lessorUserId != null) {
            eventPublisher.publishBookingDecision("REQUESTED", event.bookingRef(), event.bedId(),
                    bed.getBedNumber(), event.renterId(), event.renterName(), lessorUserId);
        }
    }
}
