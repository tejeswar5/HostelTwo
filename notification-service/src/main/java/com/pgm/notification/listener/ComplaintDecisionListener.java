package com.pgm.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.notification.entity.NotificationType;
import com.pgm.notification.event.ComplaintDecisionEvent;
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
public class ComplaintDecisionListener {

    private static final Logger log = LoggerFactory.getLogger(ComplaintDecisionListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final ProcessedEventGuard processedEventGuard;
    private final String groupId;

    public ComplaintDecisionListener(
            NotificationService notificationService,
            ObjectMapper objectMapper,
            ProcessedEventGuard processedEventGuard,
            @Value("${spring.kafka.consumer.group-id}") String groupId) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.processedEventGuard = processedEventGuard;
        this.groupId = groupId;
    }

    @KafkaListener(topics = "complaint-decisions", groupId = "${spring.kafka.consumer.group-id}")
    public void onComplaintDecision(
            String payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        if (processedEventGuard.alreadyProcessed(groupId, "complaint-decisions", partition, offset)) {
            log.info("Skipping already-processed complaint-decisions message at partition {} offset {}", partition, offset);
            return;
        }
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
