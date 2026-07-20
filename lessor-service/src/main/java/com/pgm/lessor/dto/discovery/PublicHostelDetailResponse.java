package com.pgm.lessor.dto.discovery;

import java.util.List;

public record PublicHostelDetailResponse(
        Long id,
        String name,
        String contactPhone,
        String contactEmail,
        boolean hasLift,
        String area,
        String city,
        String state,
        List<String> hostelAmenities,
        List<PublicFloorResponse> floors) {
}
