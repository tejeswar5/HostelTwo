package com.pgm.renter.event;

import java.time.LocalDate;

/**
 * Producer-side shape published to "booking-requests". lessor-service's consumer
 * parses this into its own local record of the same shape - no shared Java type,
 * just a matching JSON field contract.
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
