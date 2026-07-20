package com.pgm.renter.dto.discovery;

import java.util.List;

public record RenterRoomResponse(
        Long id,
        String roomNumber,
        Integer sharingType,
        boolean airConditioned,
        Double monthlyRent,
        List<RenterBedResponse> beds) {
}
