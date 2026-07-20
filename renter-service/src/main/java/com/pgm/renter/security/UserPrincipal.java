package com.pgm.renter.security;

import com.pgm.renter.entity.Role;

/**
 * Authenticated identity resolved from a validated JWT. Never trust a client-supplied
 * user id header - controllers and services must always source it from this principal.
 */
public record UserPrincipal(Long userId, String email, Role role, String tenantId) {
}
