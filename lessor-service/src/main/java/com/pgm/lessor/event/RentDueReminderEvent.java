package com.pgm.lessor.event;

import java.time.LocalDate;

public record RentDueReminderEvent(
        Long renterId,
        double amount,
        String bedNumber,
        LocalDate dueDate) {
}
