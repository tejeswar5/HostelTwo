package com.pgm.lessor.dto.payment;

import com.pgm.lessor.entity.PaymentMethod;

import java.time.LocalDateTime;

public record ReceiptResponse(
        Long paymentId,
        Long invoiceId,
        String hostelName,
        String roomNumber,
        String bedNumber,
        String renterName,
        Double amount,
        PaymentMethod method,
        String transactionId,
        LocalDateTime paymentDate,
        Double remainingDue) {
}
