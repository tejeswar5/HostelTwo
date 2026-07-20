package com.pgm.lessor.dto.hostel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RoomRequest(
        @NotNull Long floorId,
        @NotBlank String roomNumber,
        @NotNull @Positive Integer capacity,
        @NotNull @Positive Double monthlyRent,
        @NotNull @Positive Integer sharingType,
        boolean airConditioned) {
}
