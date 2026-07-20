package com.pgm.renter.event;

/**
 * Consumer-side shape of what lessor-service publishes to "booking-decisions".
 * REQUESTED events are for the lessor (notification-service handles those); this
 * service only acts on APPROVED / REJECTED, matched back to its local read-model
 * row by bookingRef.
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
