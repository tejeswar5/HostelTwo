package com.pgm.renter.dto.profile;

import com.pgm.renter.entity.Role;

public record ProfileResponse(
        Long id,
        String fname,
        String lname,
        String email,
        String phoneNumber,
        String profilePictureUrl,
        Role role) {
}
