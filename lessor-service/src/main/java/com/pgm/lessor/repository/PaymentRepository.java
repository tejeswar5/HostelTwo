package com.pgm.lessor.repository;

import com.pgm.lessor.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    List<Payment> findByInvoiceHostelIdOrderByPaymentDateDesc(Long hostelId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.invoice.hostel.id = :hostelId and p.paymentDate >= :from")
    double sumAmountSince(@Param("hostelId") Long hostelId, @Param("from") LocalDateTime from);
}
