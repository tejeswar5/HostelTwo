package com.pgm.renter.dto.discovery;

public record DiscoverHostelResponse(
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
