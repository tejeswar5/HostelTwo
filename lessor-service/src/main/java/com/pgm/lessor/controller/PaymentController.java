package com.pgm.lessor.controller;

import com.pgm.lessor.dto.payment.*;
import com.pgm.lessor.security.UserPrincipal;
import com.pgm.lessor.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessor")
@PreAuthorize("hasRole('LESSOR')")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/invoices")
    public ResponseEntity<InvoiceResponse> generateInvoice(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody GenerateInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.generateInvoice(principal, request));
    }

    @GetMapping("/invoices")
    public List<InvoiceResponse> listInvoices(@AuthenticationPrincipal UserPrincipal principal, @RequestParam(required = false) Long bedId) {
        return paymentService.listInvoices(principal, bedId);
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> recordPayment(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody RecordPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.recordPayment(principal, request));
    }

    @GetMapping("/payments")
    public List<PaymentResponse> listPayments(@AuthenticationPrincipal UserPrincipal principal, @RequestParam(required = false) Long invoiceId) {
        return paymentService.listPayments(principal, invoiceId);
    }

    @GetMapping("/payments/{paymentId}/receipt")
    public ReceiptResponse getReceipt(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long paymentId) {
        return paymentService.getReceipt(principal, paymentId);
    }
}
