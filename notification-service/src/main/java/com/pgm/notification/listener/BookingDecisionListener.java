package com.pgm.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.notification.entity.NotificationType;
import com.pgm.notification.event.BookingDecisionEvent;
import com.pgm.notification.service.NotificationService;
import com.pgm.notification.service.ProcessedEventGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class BookingDecisionListener {

    private static final Logger log = LoggerFactory.getLogger(BookingDecisionListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final ProcessedEventGuard processedEventGuard;
    private final String groupId;

    public BookingDecisionListener(
            NotificationService notificationService,
            ObjectMapper objectMapper,
            ProcessedEventGuard processedEventGuard,
            @Value("${spring.kafka.consumer.group-id}") String groupId) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.processedEventGuard = processedEventGuard;
        this.groupId = groupId;
    }

    @KafkaListener(topics = "booking-decisions", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookingDecision(
            String payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        if (processedEventGuard.alreadyProcessed(groupId, "booking-decisions", partition, offset)) {
            log.info("Skipping already-processed booking-decisions message at partition {} offset {}", partition, offset);
            return;
        }
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
