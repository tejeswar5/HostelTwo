package com.pgm.lessor.event;

/**
 * Producer-side shape published to "booking-decisions". eventType is one of
 * REQUESTED (recipient = lessorUserId, a new request came in - renter-service
 * ignores this one, notification-service acts on it), APPROVED / REJECTED
 * (recipient = renterId, consumed by both renter-service, to update its local
 * read-model row, and notification-service).
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
