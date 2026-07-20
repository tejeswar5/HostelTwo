package com.pgm.notification.security;

import com.pgm.notification.entity.Role;

public record UserPrincipal(Long userId, String email, Role role, String tenantId) {
}
