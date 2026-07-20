package com.pgm.notification.event;

public record PaymentReceivedEvent(
        Long renterId,
        double amount,
        String method,
        double remainingDue) {
}
