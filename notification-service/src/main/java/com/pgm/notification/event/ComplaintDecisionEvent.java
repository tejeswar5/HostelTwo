package com.pgm.notification.event;

public record ComplaintDecisionEvent(
        String complaintRef,
        Long raisedById,
        String category,
        String status) {
}
