package com.pgm.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.notification.entity.NotificationType;
import com.pgm.notification.event.RentDueReminderEvent;
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
public class RentReminderEventListener {

    private static final Logger log = LoggerFactory.getLogger(RentReminderEventListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final ProcessedEventGuard processedEventGuard;
    private final String groupId;

    public RentReminderEventListener(
            NotificationService notificationService,
            ObjectMapper objectMapper,
            ProcessedEventGuard processedEventGuard,
            @Value("${spring.kafka.consumer.group-id}") String groupId) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.processedEventGuard = processedEventGuard;
        this.groupId = groupId;
    }

    @KafkaListener(topics = "rent-reminder-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onRentDueReminder(
            String payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        if (processedEventGuard.alreadyProcessed(groupId, "rent-reminder-events", partition, offset)) {
            log.info("Skipping already-processed rent-reminder-events message at partition {} offset {}", partition, offset);
            return;
        }
        try {
            RentDueReminderEvent event = objectMapper.readValue(payload, RentDueReminderEvent.class);
            notificationService.notify(event.renterId(), NotificationType.RENT_DUE_REMINDER,
                    "Rent due soon",
                    "Rent of " + event.amount() + " for bed " + event.bedNumber() + " is due on " + event.dueDate() + ".");
        } catch (Exception e) {
            log.error("Failed to process rent-reminder-events payload: {}", payload, e);
        }
    }
}
