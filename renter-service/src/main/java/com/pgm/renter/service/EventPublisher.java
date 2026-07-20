package com.pgm.renter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.renter.event.BookingRequestedEvent;
import com.pgm.renter.event.ComplaintRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Publishes the renter-initiated request events. Publish failures are logged, not
 * rethrown - the renter's local optimistic row has already been saved; a dropped
 * event just means lessor-service never gets asked, which is a lesser failure
 * than rolling back a request the renter already believes went through.
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishBookingRequested(String bookingRef, Long bedId, Long renterId, String renterName,
                                         String renterEmail, String renterPhone, LocalDate checkIn, LocalDate checkOut) {
        publish("booking-requests", bookingRef, new BookingRequestedEvent(
                bookingRef, bedId, renterId, renterName, renterEmail, renterPhone, checkIn, checkOut));
    }

    public void publishComplaintRequested(String complaintRef, Long hostelId, Long raisedById, String raisedByName,
                                           String raisedByEmail, String category, String description) {
        publish("complaint-requests", complaintRef, new ComplaintRequestedEvent(
                complaintRef, hostelId, raisedById, raisedByName, raisedByEmail, category, description));
    }

    private void publish(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.error("Failed to publish event to topic {}: {}", topic, payload, e);
        }
    }
}
