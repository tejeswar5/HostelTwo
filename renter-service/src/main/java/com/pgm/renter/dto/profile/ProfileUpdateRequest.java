package com.pgm.renter.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProfileUpdateRequest(
        @NotBlank String fname,
        @NotBlank String lname,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "must be a valid phone number") String phoneNumber,
        String profilePictureUrl) {
}
