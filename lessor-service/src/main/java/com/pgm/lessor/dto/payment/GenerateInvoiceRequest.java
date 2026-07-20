package com.pgm.lessor.dto.payment;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record GenerateInvoiceRequest(
        @NotNull Long bedId,
        @NotNull @Positive Double totalAmount,
        @NotNull @FutureOrPresent LocalDate dueDate) {
}
