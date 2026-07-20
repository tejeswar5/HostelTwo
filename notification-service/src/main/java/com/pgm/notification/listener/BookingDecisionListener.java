package com.pgm.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.notification.entity.NotificationType;
import com.pgm.notification.event.BookingDecisionEvent;
import com.pgm.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingDecisionListener {

    private static final Logger log = LoggerFactory.getLogger(BookingDecisionListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public BookingDecisionListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "booking-decisions", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookingDecision(String payload) {
        try {
            BookingDecisionEvent event = objectMapper.readValue(payload, BookingDecisionEvent.class);
            switch (event.eventType()) {
                case "REQUESTED" -> notificationService.notify(event.lessorUserId(), NotificationType.BOOKING_REQUESTED,
                        "New booking request",
                        "A renter requested bed " + event.bedNumber() + ".");
                case "APPROVED" -> notificationService.notify(event.renterId(), NotificationType.BOOKING_APPROVED,
                        "Booking approved",
                        "Your request for bed " + event.bedNumber() + " has been approved.");
                case "REJECTED" -> notificationService.notify(event.renterId(), NotificationType.BOOKING_REJECTED,
                        "Booking rejected",
                        "Your request for bed " + event.bedNumber() + " was not approved.");
                default -> log.warn("Unknown booking decision eventType: {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Failed to process booking-decisions payload: {}", payload, e);
        }
    }
}
