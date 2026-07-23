package com.pgm.notification.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Kafka's default delivery guarantee is at-least-once: a consumer restart or
 * rebalance can redeliver a message this service already applied (e.g. writing the
 * same notification twice). The dedup key is the message's own (topic, partition,
 * offset) - a physical identity that's stable across redeliveries of the exact same
 * message - rather than business fields, because business fields (like a bookingRef)
 * can legitimately repeat across genuinely distinct events (e.g. the REQUESTED and
 * APPROVED events for the same booking) and would cause false-positive dedup. The 24h
 * TTL bounds Redis memory; it only needs to outlive how long a redelivery could
 * plausibly lag behind the original delivery, not forever.
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
