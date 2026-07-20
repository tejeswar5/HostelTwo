package com.pgm.lessor.dto.payment;

import com.pgm.lessor.entity.PaymentMethod;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long invoiceId,
        Double amount,
        PaymentMethod method,
        String transactionId,
        String notes,
        LocalDateTime paymentDate) {
}
