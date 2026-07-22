package com.pgm.lessor.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Fixed-window request counter. INCR-then-conditionally-EXPIRE as two separate
 * commands would race under concurrent requests (both could see count==1 and both
 * set the TTL, or a request could land between the two commands with no TTL set at
 * all) - a small Lua script makes the increment-and-maybe-expire atomic server-side.
 */
@Component
public class RateLimiter {

    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>(
            """
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """,
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** Returns true if this call is within the limit; false if it should be rejected. */
    public boolean allow(String key, int limit, Duration window) {
        Long count = redisTemplate.execute(SCRIPT, List.of(key), String.valueOf(window.toSeconds()));
        return count != null && count <= limit;
    }
}
