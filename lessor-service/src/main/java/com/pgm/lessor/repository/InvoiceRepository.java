package com.pgm.lessor.repository;

import com.pgm.lessor.entity.Invoice;
import com.pgm.lessor.entity.InvoiceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByBedIdOrderByDueDateDesc(Long bedId);

    List<Invoice> findByHostelIdOrderByDueDateDesc(Long hostelId);

    List<Invoice> findByStatusNotAndDueDateBetween(InvoiceStatus status, LocalDate from, LocalDate to);

    /** Row-locks the invoice so concurrent payment recordings can't race on due_amount. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(i.dueAmount), 0) from Invoice i where i.hostel.id = :hostelId and i.status <> com.pgm.lessor.entity.InvoiceStatus.PAID")
    double sumDueByHostel(@Param("hostelId") Long hostelId);
}
