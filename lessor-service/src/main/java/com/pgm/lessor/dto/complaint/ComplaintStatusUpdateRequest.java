package com.pgm.lessor.dto.complaint;

import com.pgm.lessor.entity.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public record ComplaintStatusUpdateRequest(@NotNull ComplaintStatus status) {
}
