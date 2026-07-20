package com.pgm.renter.dto.complaint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * hostelName is a snapshot the client already has from the hostel detail page -
 * renter-service no longer holds hostel data locally to look it up after the DB
 * split.
 */
public record RaiseComplaintRequest(
        @NotNull Long hostelId,
        @NotBlank String hostelName,
        @NotBlank String category,
        @NotBlank String description) {
}
