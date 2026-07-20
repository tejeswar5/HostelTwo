package com.pgm.notification.entity;

/**
 * Mirrors lessor/renter-service's Role enum for JWT claim parsing only - this
 * service has no users table of its own, so recipients are addressed purely by
 * the userId embedded in the JWT / carried on inbound Kafka events.
 */
public enum Role {
    LESSOR,
    RENTER
}
