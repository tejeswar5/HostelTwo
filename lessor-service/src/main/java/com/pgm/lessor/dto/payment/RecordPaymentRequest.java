package com.pgm.lessor.dto.payment;

import com.pgm.lessor.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecordPaymentRequest(
        @NotNull Long invoiceId,
        @NotNull @Positive Double amount,
        @NotNull PaymentMethod method,
        String transactionId,
        String notes) {
}
