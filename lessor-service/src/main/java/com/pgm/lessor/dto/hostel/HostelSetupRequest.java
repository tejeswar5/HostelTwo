package com.pgm.lessor.dto.hostel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record HostelSetupRequest(
        @NotBlank String name,
        String contactPhone,
        @Email String contactEmail,
        boolean hasLift,
        String buildingNameOrNumber,
        String street,
        String area,
        String city,
        String state,
        Integer pinCode) {
}
