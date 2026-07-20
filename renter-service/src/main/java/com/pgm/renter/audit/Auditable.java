package com.pgm.renter.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method as a mutation that must be recorded in {@code audit_log} -
 * who did what, and when. Applied to booking creation and complaint creation so there
 * is a trail of who booked what bed, from the renter side.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    String action();

    String entityType();
}
