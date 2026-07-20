package com.pgm.lessor.service;

import com.pgm.lessor.audit.Auditable;
import com.pgm.lessor.dto.payment.*;
import com.pgm.lessor.entity.*;
import com.pgm.lessor.exception.ConflictException;
import com.pgm.lessor.exception.NotFoundException;
import com.pgm.lessor.repository.BedRepository;
import com.pgm.lessor.repository.BookingRepository;
import com.pgm.lessor.repository.InvoiceRepository;
import com.pgm.lessor.repository.PaymentRepository;
import com.pgm.lessor.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Recording a payment and recomputing an invoice's due amount happen inside one
 * transaction with the invoice row locked ({@link InvoiceRepository#findByIdForUpdate}),
 * so two concurrent payment entries (e.g. a cash entry from the lessor and a UPI
 * reference entered moments later) can't race and silently drop one update - this is
 * the guard against payment-status lag called out in the requirements. There is no
 * real payment gateway here (out of scope), so there is no webhook-delivery lag to
 * account for; if one is added later it should write through an idempotency key and
 * an outbox event rather than mutating the invoice directly from the webhook handler.
 */
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final BedRepository bedRepository;
    private final BookingRepository bookingRepository;
    private final HostelService hostelService;
    private final EventPublisher eventPublisher;

    public PaymentService(
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            BedRepository bedRepository,
            BookingRepository bookingRepository,
            HostelService hostelService,
            EventPublisher eventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.bedRepository = bedRepository;
        this.bookingRepository = bookingRepository;
        this.hostelService = hostelService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Auditable(action = "INVOICE_GENERATED", entityType = "INVOICE")
    public InvoiceResponse generateInvoice(UserPrincipal principal, GenerateInvoiceRequest request) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        Bed bed = bedRepository.findById(request.bedId()).orElseThrow(() -> new NotFoundException("Bed not found"));
        if (!bed.getHostel().getId().equals(hostel.getId())) {
            throw new NotFoundException("Bed not found");
        }
        Invoice invoice = Invoice.builder()
                .tenantId(hostel.getTenantId())
                .hostel(hostel)
                .bed(bed)
                .totalAmount(request.totalAmount())
                .partialPayment(0.0)
                .dueAmount(request.totalAmount())
                .status(InvoiceStatus.PENDING)
                .dueDate(request.dueDate())
                .build();
        return toInvoiceResponse(invoiceRepository.save(invoice));
    }

    public List<InvoiceResponse> listInvoices(UserPrincipal principal, Long bedId) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        List<Invoice> invoices = bedId != null
                ? invoiceRepository.findByBedIdOrderByDueDateDesc(bedId)
                : invoiceRepository.findByHostelIdOrderByDueDateDesc(hostel.getId());
        return invoices.stream().map(this::toInvoiceResponse).toList();
    }

    @Transactional
    @Auditable(action = "PAYMENT_RECORDED", entityType = "PAYMENT")
    public PaymentResponse recordPayment(UserPrincipal principal, RecordPaymentRequest request) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        Invoice invoice = invoiceRepository.findByIdForUpdate(request.invoiceId())
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (!invoice.getHostel().getId().equals(hostel.getId())) {
            throw new NotFoundException("Invoice not found");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new ConflictException("This invoice is already fully paid");
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.amount())
                .method(request.method())
                .transactionId(request.transactionId())
                .notes(request.notes())
                .paymentDate(LocalDateTime.now())
                .build();
        payment = paymentRepository.save(payment);

        double newPartial = invoice.getPartialPayment() + request.amount();
        double newDue = Math.max(0, invoice.getTotalAmount() - newPartial);
        invoice.setPartialPayment(newPartial);
        invoice.setDueAmount(newDue);
        invoice.setStatus(newDue <= 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID);
        invoice.setUpdatedAt(LocalDateTime.now());

        bookingRepository.findFirstByBedIdAndStatusOrderByDecidedAtDesc(invoice.getBed().getId(), BookingStatus.APPROVED)
                .ifPresent(booking -> eventPublisher.publishPaymentReceived(booking.getRenterId(), request.amount(), request.method().name(), newDue));

        return toPaymentResponse(payment);
    }

    public List<PaymentResponse> listPayments(UserPrincipal principal, Long invoiceId) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        List<Payment> payments = invoiceId != null
                ? paymentRepository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId)
                : paymentRepository.findByInvoiceHostelIdOrderByPaymentDateDesc(hostel.getId());
        return payments.stream().map(this::toPaymentResponse).toList();
    }

    public ReceiptResponse getReceipt(UserPrincipal principal, Long paymentId) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found"));
        Invoice invoice = payment.getInvoice();
        if (!invoice.getHostel().getId().equals(hostel.getId())) {
            throw new NotFoundException("Payment not found");
        }
        Bed bed = invoice.getBed();
        String renterName = bookingRepository.findFirstByBedIdAndStatusOrderByDecidedAtDesc(bed.getId(), BookingStatus.APPROVED)
                .map(Booking::getRenterName)
                .orElse("N/A");
        return new ReceiptResponse(
                payment.getId(),
                invoice.getId(),
                hostel.getName(),
                bed.getRoom().getRoomNumber(),
                bed.getBedNumber(),
                renterName,
                payment.getAmount(),
                payment.getMethod(),
                payment.getTransactionId(),
                payment.getPaymentDate(),
                invoice.getDueAmount());
    }

    private InvoiceResponse toInvoiceResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getBed().getId(),
                invoice.getBed().getBedNumber(),
                invoice.getTotalAmount(),
                invoice.getPartialPayment(),
                invoice.getDueAmount(),
                invoice.getStatus(),
                invoice.getDueDate());
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getInvoice().getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getTransactionId(),
                payment.getNotes(),
                payment.getPaymentDate());
    }
}
