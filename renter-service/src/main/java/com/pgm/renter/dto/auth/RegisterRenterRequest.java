package com.pgm.renter.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRenterRequest(
        @NotBlank String fname,
        @NotBlank String lname,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "must be a valid phone number") String phoneNumber,
        @NotBlank @Size(min = 8, max = 72, message = "must be at least 8 characters") String password) {
}
