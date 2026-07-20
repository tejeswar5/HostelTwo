package com.pgm.lessor.dto.discovery;

/**
 * Field-compatible with renter-service's DiscoverHostelResponse - renter-service
 * deserializes this JSON straight into its own local record by field name, since
 * it no longer holds hostel/room/bed data locally after the DB split.
 */
public record PublicHostelSummaryResponse(
        Long id,
        String name,
        String contactPhone,
        String contactEmail,
        boolean hasLift,
        String area,
        String city,
        String state,
        long availableBeds,
        Double cheapestMonthlyRent) {
}
