package com.pgm.renter.dto.complaint;

import com.pgm.renter.entity.ComplaintStatus;

import java.time.LocalDateTime;

public record ComplaintResponse(
        Long id,
        Long hostelId,
        String hostelName,
        String category,
        String description,
        ComplaintStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
