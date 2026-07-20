package com.pgm.lessor.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method as a mutation that must be recorded in {@code audit_log} -
 * who did what, to which entity, and when. Applied to booking decisions, payment
 * recording, maintenance toggles, and hostel/room/bed CRUD.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    String action();

    String entityType();
}
