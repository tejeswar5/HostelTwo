package com.pgm.notification.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Read-only here: this service never issues or revokes tokens, but it does validate
 * tokens minted by lessor/renter-service (shared JWT secret), so it must honor a
 * revocation either of them recorded. Key prefix ("authdenylist:") must match exactly
 * what lessor/renter-service's TokenDenylistService writes - it is deliberately not
 * scoped per-service, since the whole point is cross-service visibility.
 */
@Service
public class TokenDenylistService {

    private static final String KEY_PREFIX = "authdenylist:";

    private final StringRedisTemplate redisTemplate;

    public TokenDenylistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isRevoked(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
    }
}
