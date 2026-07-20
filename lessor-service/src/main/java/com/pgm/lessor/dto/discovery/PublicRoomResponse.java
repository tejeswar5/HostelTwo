package com.pgm.lessor.dto.discovery;

import java.util.List;

public record PublicRoomResponse(
        Long id,
        String roomNumber,
        Integer sharingType,
        boolean airConditioned,
        Double monthlyRent,
        List<PublicBedResponse> beds) {
}
