package com.pgm.renter.event;

public record ComplaintDecisionEvent(
        String complaintRef,
        Long raisedById,
        String category,
        String status) {
}
