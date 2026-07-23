package com.pgm.lessor.security;

import com.pgm.lessor.entity.Role;

import java.time.Instant;

/**
 * Authenticated identity resolved from a validated JWT. Never trust a client-supplied
 * user id / tenant id header - controllers and services must always source these from
 * this principal (see {@code SecurityContextHolder} via {@code @AuthenticationPrincipal}).
 * jti/expiresAt are carried through so a logout endpoint can revoke exactly this token
 * (see TokenDenylistService) without re-parsing the raw JWT.
 */
public record UserPrincipal(Long userId, String email, Role role, String tenantId, String jti, Instant expiresAt) {
}
