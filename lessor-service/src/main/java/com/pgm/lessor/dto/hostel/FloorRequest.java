package com.pgm.lessor.dto.hostel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FloorRequest(@NotNull @Positive Integer floorNumber) {
}
