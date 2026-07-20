package com.pgm.lessor.dto.hostel;

import java.util.List;

public record RoomResponse(
        Long id,
        Long floorId,
        String roomNumber,
        Integer capacity,
        Double monthlyRent,
        Integer sharingType,
        boolean airConditioned,
        List<BedResponse> beds) {
}
