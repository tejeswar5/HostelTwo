package com.pgm.renter.dto.auth;

import com.pgm.renter.entity.Role;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String fname,
        String lname,
        String email,
        Role role) {
}
