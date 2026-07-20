package com.pgm.lessor.service;

import com.pgm.lessor.entity.BookingStatus;
import com.pgm.lessor.entity.Invoice;
import com.pgm.lessor.entity.InvoiceStatus;
import com.pgm.lessor.repository.BookingRepository;
import com.pgm.lessor.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Daily rent-due reminder job. Runs once across every tenant hosted by this
 * lessor-service instance - a scheduled system job, not tied to any one request's
 * tenant scope. Publishes one event per due invoice rather than notifying
 * synchronously, so a burst of due invoices doesn't block this job on
 * notification-service being up.
 */
@Component
public class RentReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(RentReminderScheduler.class);
    private static final int REMINDER_WINDOW_DAYS = 3;

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final EventPublisher eventPublisher;

    public RentReminderScheduler(InvoiceRepository invoiceRepository, BookingRepository bookingRepository, EventPublisher eventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendDueReminders() {
        LocalDate today = LocalDate.now();
        LocalDate windowEnd = today.plusDays(REMINDER_WINDOW_DAYS);
        var dueSoon = invoiceRepository.findByStatusNotAndDueDateBetween(InvoiceStatus.PAID, today, windowEnd);
        for (Invoice invoice : dueSoon) {
            bookingRepository.findFirstByBedIdAndStatusOrderByDecidedAtDesc(invoice.getBed().getId(), BookingStatus.APPROVED)
                    .ifPresent(booking -> eventPublisher.publishRentDueReminder(
                            booking.getRenterId(), invoice.getDueAmount(), invoice.getBed().getBedNumber(), invoice.getDueDate()));
        }
        log.info("Rent reminder run: {} invoice(s) due within {} days", dueSoon.size(), REMINDER_WINDOW_DAYS);
    }
}
