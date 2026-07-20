package com.pgm.renter.dto.booking;

import com.pgm.renter.entity.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long bedId,
        String bedNumber,
        String hostelName,
        BookingStatus status,
        LocalDate requestedCheckIn,
        LocalDate requestedCheckOut,
        LocalDateTime createdAt) {
}
