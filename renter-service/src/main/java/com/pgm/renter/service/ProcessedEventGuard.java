package com.pgm.renter.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Kafka's default delivery guarantee is at-least-once: a consumer restart or
 * rebalance can redeliver a message this service already applied. The dedup key is
 * the message's own (topic, partition, offset) - a physical identity that's stable
 * across redeliveries of the exact same message - rather than business fields (e.g.
 * bookingRef), because business fields can legitimately repeat across genuinely
 * distinct events and would cause false-positive dedup. The 24h TTL bounds Redis
 * memory; it only needs to outlive how long a redelivery could plausibly lag behind
 * the original delivery, not forever. Note: this guard is set before handler logic
 * runs, so if the handler throws, the message is still marked processed and won't be
 * retried - the listeners it wraps already swallow exceptions with a log-and-drop
 * rather than a dead-letter/retry policy, so this doesn't change that risk profile,
 * it just extends the same policy to duplicate deliveries.
 */
@Component
public class ProcessedEventGuard {

    private static final String KEY_PREFIX = "kafka:processed:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public ProcessedEventGuard(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** Returns true if this exact message was already processed (caller should skip it). */
    public boolean alreadyProcessed(String consumerGroupId, String topic, int partition, long offset) {
        String key = KEY_PREFIX + consumerGroupId + ":" + topic + ":" + partition + ":" + offset;
        Boolean firstTime = redisTemplate.opsForValue().setIfAbsent(key, "1", TTL);
        return !Boolean.TRUE.equals(firstTime);
    }
}
