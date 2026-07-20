package com.pgm.lessor.security;

import com.pgm.lessor.entity.Role;

/**
 * Authenticated identity resolved from a validated JWT. Never trust a client-supplied
 * user id / tenant id header - controllers and services must always source these from
 * this principal (see {@code SecurityContextHolder} via {@code @AuthenticationPrincipal}).
 */
public record UserPrincipal(Long userId, String email, Role role, String tenantId) {
}
