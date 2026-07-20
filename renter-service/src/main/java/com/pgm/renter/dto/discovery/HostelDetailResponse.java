package com.pgm.renter.dto.discovery;

import java.util.List;

public record HostelDetailResponse(
        Long id,
        String name,
        String contactPhone,
        String contactEmail,
        boolean hasLift,
        String area,
        String city,
        String state,
        List<String> hostelAmenities,
        List<RenterFloorResponse> floors) {
}
