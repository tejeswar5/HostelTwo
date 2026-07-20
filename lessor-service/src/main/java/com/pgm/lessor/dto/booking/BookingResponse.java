package com.pgm.lessor.dto.booking;

import com.pgm.lessor.entity.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long bedId,
        String bedNumber,
        String roomNumber,
        Long renterId,
        String renterName,
        String renterPhone,
        BookingStatus status,
        LocalDate requestedCheckIn,
        LocalDate requestedCheckOut,
        LocalDateTime createdAt) {
}
