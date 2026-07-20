package com.pgm.lessor.dto.discovery;

import java.util.List;

public record PublicFloorResponse(Long id, Integer floorNumber, List<String> amenities, List<PublicRoomResponse> rooms) {
}
