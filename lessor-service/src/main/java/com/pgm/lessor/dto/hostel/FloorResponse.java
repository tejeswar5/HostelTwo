package com.pgm.lessor.dto.hostel;

import java.util.List;

public record FloorResponse(Long id, Integer floorNumber, List<RoomResponse> rooms) {
}
