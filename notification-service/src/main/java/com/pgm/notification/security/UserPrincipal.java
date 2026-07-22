package com.pgm.notification.security;

import com.pgm.notification.entity.Role;

import java.time.Instant;

/** jti/expiresAt let this service check the shared authdenylist:{jti} entry a logout
 * on lessor/renter-service would have written, even though this service never issues
 * or revokes tokens itself. */
public record UserPrincipal(Long userId, String email, Role role, String tenantId, String jti, Instant expiresAt) {
}
