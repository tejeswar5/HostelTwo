package com.pgm.renter.event;

public record ComplaintRequestedEvent(
        String complaintRef,
        Long hostelId,
        Long raisedById,
        String raisedByName,
        String raisedByEmail,
        String category,
        String description) {
}
