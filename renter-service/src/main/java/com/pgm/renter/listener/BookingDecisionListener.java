package com.pgm.renter.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.renter.entity.Booking;
import com.pgm.renter.entity.BookingStatus;
import com.pgm.renter.event.BookingDecisionEvent;
import com.pgm.renter.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Updates this service's local, eventually-consistent copy of the booking once
 * lessor-service (the actual owner of the bed) has decided. REQUESTED events on
 * this same topic are for the lessor's notification only - nothing to update here.
 */
@Component
public class BookingDecisionListener {

    private static final Logger log = LoggerFactory.getLogger(BookingDecisionListener.class);

    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;

    public BookingDecisionListener(BookingRepository bookingRepository, ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "booking-decisions", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onBookingDecision(String payload) {
        try {
            BookingDecisionEvent event = objectMapper.readValue(payload, BookingDecisionEvent.class);
            if (!"APPROVED".equals(event.eventType()) && !"REJECTED".equals(event.eventType())) {
                return;
            }
            UUID bookingRef = UUID.fromString(event.bookingRef());
            Booking booking = bookingRepository.findByBookingRef(bookingRef).orElse(null);
            if (booking == null) {
                log.warn("Booking decision for unknown bookingRef {} - dropped", bookingRef);
                return;
            }
            booking.setStatus(BookingStatus.valueOf(event.eventType()));
            booking.setDecidedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to process booking-decisions payload: {}", payload, e);
        }
    }
}
