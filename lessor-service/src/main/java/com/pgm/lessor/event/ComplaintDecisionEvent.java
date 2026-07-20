package com.pgm.lessor.event;

public record ComplaintDecisionEvent(
        String complaintRef,
        Long raisedById,
        String category,
        String status) {
}
