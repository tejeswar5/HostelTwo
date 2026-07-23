package com.pgm.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.notification.entity.NotificationType;
import com.pgm.notification.event.PaymentReceivedEvent;
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
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final ProcessedEventGuard processedEventGuard;
    private final String groupId;

    public PaymentEventListener(
            NotificationService notificationService,
            ObjectMapper objectMapper,
            ProcessedEventGuard processedEventGuard,
            @Value("${spring.kafka.consumer.group-id}") String groupId) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.processedEventGuard = processedEventGuard;
        this.groupId = groupId;
    }

    @KafkaListener(topics = "payment-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentReceived(
            String payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        if (processedEventGuard.alreadyProcessed(groupId, "payment-events", partition, offset)) {
            log.info("Skipping already-processed payment-events message at partition {} offset {}", partition, offset);
            return;
        }
        try {
            PaymentReceivedEvent event = objectMapper.readValue(payload, PaymentReceivedEvent.class);
            notificationService.notify(event.renterId(), NotificationType.PAYMENT_RECEIVED,
                    "Payment received",
                    "We received a payment of " + event.amount() + " via " + event.method() + ". Remaining due: " + event.remainingDue() + ".");
        } catch (Exception e) {
            log.error("Failed to process payment-events payload: {}", payload, e);
        }
    }
}
