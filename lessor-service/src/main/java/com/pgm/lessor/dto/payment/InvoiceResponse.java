package com.pgm.lessor.dto.payment;

import com.pgm.lessor.entity.InvoiceStatus;

import java.time.LocalDate;

public record InvoiceResponse(
        Long id,
        Long bedId,
        String bedNumber,
        Double totalAmount,
        Double partialPayment,
        Double dueAmount,
        InvoiceStatus status,
        LocalDate dueDate) {
}
