package com.pgm.lessor.dto.auth;

import com.pgm.lessor.entity.Role;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String fname,
        String lname,
        String email,
        Role role) {
}
