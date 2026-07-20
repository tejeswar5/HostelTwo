package com.pgm.lessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.lessor.event.BookingDecisionEvent;
import com.pgm.lessor.event.ComplaintDecisionEvent;
import com.pgm.lessor.event.PaymentReceivedEvent;
import com.pgm.lessor.event.RentDueReminderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Publishes domain events as plain JSON strings so any consumer (renter-service,
 * notification-service, or something else entirely later) can parse them without
 * sharing a Java class with this service. Publish failures are logged, not
 * rethrown - a dropped notification/read-model update should never roll back the
 * business transaction that already committed.
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

    public void publishBookingDecision(String eventType, String bookingRef, Long bedId, String bedNumber,
                                        Long renterId, String renterName, Long lessorUserId) {
        publish("booking-decisions", bookingRef, new BookingDecisionEvent(
                eventType, bookingRef, bedId, bedNumber, renterId, renterName, lessorUserId));
    }

    public void publishComplaintDecision(String complaintRef, Long raisedById, String category, String status) {
        publish("complaint-decisions", complaintRef, new ComplaintDecisionEvent(complaintRef, raisedById, category, status));
    }

    public void publishPaymentReceived(Long renterId, double amount, String method, double remainingDue) {
        publish("payment-events", String.valueOf(renterId), new PaymentReceivedEvent(renterId, amount, method, remainingDue));
    }

    public void publishRentDueReminder(Long renterId, double amount, String bedNumber, LocalDate dueDate) {
        publish("rent-reminder-events", String.valueOf(renterId), new RentDueReminderEvent(renterId, amount, bedNumber, dueDate));
    }

    private void publish(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.error("Failed to publish event to topic {}: {}", topic, payload, e);
        }
    }
}
