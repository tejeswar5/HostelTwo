package com.pgm.lessor.dto.hostel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AmenityAssignRequest(@NotBlank String name, @NotNull @Positive Integer quantity) {
}
