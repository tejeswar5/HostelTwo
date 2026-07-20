package com.pgm.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.notification.entity.NotificationType;
import com.pgm.notification.event.ComplaintDecisionEvent;
import com.pgm.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ComplaintDecisionListener {

    private static final Logger log = LoggerFactory.getLogger(ComplaintDecisionListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public ComplaintDecisionListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "complaint-decisions", groupId = "${spring.kafka.consumer.group-id}")
    public void onComplaintDecision(String payload) {
        try {
            ComplaintDecisionEvent event = objectMapper.readValue(payload, ComplaintDecisionEvent.class);
            notificationService.notify(event.raisedById(), NotificationType.COMPLAINT_UPDATED,
                    "Complaint update",
                    "Your complaint \"" + event.category() + "\" is now " + event.status().replace('_', ' ') + ".");
        } catch (Exception e) {
            log.error("Failed to process complaint-decisions payload: {}", payload, e);
        }
    }
}
