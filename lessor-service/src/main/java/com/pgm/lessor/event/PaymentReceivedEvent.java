package com.pgm.lessor.event;

public record PaymentReceivedEvent(
        Long renterId,
        double amount,
        String method,
        double remainingDue) {
}
