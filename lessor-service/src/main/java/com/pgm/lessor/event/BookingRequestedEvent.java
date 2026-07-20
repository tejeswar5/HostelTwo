package com.pgm.lessor.event;

import java.time.LocalDate;

/**
 * Consumer-side shape of the event renter-service publishes to "booking-requests"
 * when a renter submits a booking request. This is a plain JSON contract, not a
 * shared Java type - renter-service's producer record is structurally identical
 * but lives in its own package.
 */
public record BookingRequestedEvent(
        String bookingRef,
        Long bedId,
        Long renterId,
        String renterName,
        String renterEmail,
        String renterPhone,
        LocalDate requestedCheckIn,
        LocalDate requestedCheckOut) {
}
