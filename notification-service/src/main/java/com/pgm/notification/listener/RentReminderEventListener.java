package com.pgm.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.notification.entity.NotificationType;
import com.pgm.notification.event.RentDueReminderEvent;
import com.pgm.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RentReminderEventListener {

    private static final Logger log = LoggerFactory.getLogger(RentReminderEventListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public RentReminderEventListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "rent-reminder-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onRentDueReminder(String payload) {
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
