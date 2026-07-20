package com.pgm.renter.dto.discovery;

import java.util.List;

public record RenterFloorResponse(Long id, Integer floorNumber, List<String> amenities, List<RenterRoomResponse> rooms) {
}
