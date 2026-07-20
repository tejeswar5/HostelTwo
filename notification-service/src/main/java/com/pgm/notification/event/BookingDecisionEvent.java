package com.pgm.notification.event;

/**
 * Mirrors the shape lessor-service publishes to the "booking-decisions" topic.
 * eventType is one of REQUESTED (recipient = lessorUserId, a new request came in),
 * APPROVED / REJECTED (recipient = renterId, the renter's request was decided).
 */
public record BookingDecisionEvent(
        String eventType,
        String bookingRef,
        Long bedId,
        String bedNumber,
        Long renterId,
        String renterName,
        Long lessorUserId) {
}
