package com.pgm.lessor.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Lets a still-valid (unexpired) access token be killed instantly - e.g. on logout or
 * a suspected-compromised-device report - without waiting out its remaining TTL. The
 * JWT itself is stateless by design, so this is the one piece of server-side state
 * that makes revocation possible; entries self-expire via Redis TTL so this never
 * grows unbounded. Key prefix ("authdenylist:") is deliberately NOT service-scoped:
 * lessor/renter/notification-service share one JWT secret specifically so any of them
 * can validate a token minted by another, so a revocation recorded by whichever
 * service handled the logout must be visible to all three.
 */
@Service
public class TokenDenylistService {

    private static final String KEY_PREFIX = "authdenylist:";

    private final StringRedisTemplate redisTemplate;

    public TokenDenylistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void revoke(String jti, Instant tokenExpiresAt) {
        Duration remaining = Duration.between(Instant.now(), tokenExpiresAt);
        if (remaining.isNegative() || remaining.isZero()) {
            return; // already expired, nothing to revoke
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", remaining);
    }

    public boolean isRevoked(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
    }
}
