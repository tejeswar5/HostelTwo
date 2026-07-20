package com.pgm.lessor.dto.complaint;

import com.pgm.lessor.entity.ComplaintStatus;

import java.time.LocalDateTime;

public record ComplaintResponse(
        Long id,
        Long hostelId,
        String raisedByName,
        String category,
        String description,
        ComplaintStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
