package com.pgm.lessor.dto.profile;

import com.pgm.lessor.entity.Role;

public record ProfileResponse(
        Long id,
        String fname,
        String lname,
        String email,
        String phoneNumber,
        String profilePictureUrl,
        Role role) {
}
