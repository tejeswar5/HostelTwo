package com.pgm.lessor.dto.hostel;

import jakarta.validation.constraints.NotBlank;

public record MaintenanceRequest(@NotBlank String reason) {
}
