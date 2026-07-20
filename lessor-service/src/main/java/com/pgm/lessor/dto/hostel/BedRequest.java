package com.pgm.lessor.dto.hostel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BedRequest(@NotNull Long roomId, @NotBlank String bedNumber) {
}
