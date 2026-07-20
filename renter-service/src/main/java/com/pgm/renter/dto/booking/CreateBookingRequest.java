package com.pgm.renter.dto.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

/**
 * bedNumber/roomNumber/hostelId/hostelName are a snapshot the client already has
 * from the hostel detail (discovery) page it navigated from - renter-service no
 * longer holds hostel/room/bed data locally to look these up itself after the
 * DB split, so the client passes them through for display on "My bookings".
 */
public record CreateBookingRequest(
        @NotNull Long bedId,
        @NotBlank String bedNumber,
        String roomNumber,
        @NotNull Long hostelId,
        @NotBlank String hostelName,
        @NotNull @FutureOrPresent LocalDate checkIn,
        LocalDate checkOut) {
}
