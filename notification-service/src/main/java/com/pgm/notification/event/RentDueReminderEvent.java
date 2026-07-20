package com.pgm.notification.event;

public record RentDueReminderEvent(
        Long renterId,
        double amount,
        String bedNumber,
        String dueDate) {
}
