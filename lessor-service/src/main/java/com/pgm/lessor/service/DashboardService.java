package com.pgm.lessor.service;

import com.pgm.lessor.dto.dashboard.DashboardResponse;
import com.pgm.lessor.entity.BedStatus;
import com.pgm.lessor.entity.BookingStatus;
import com.pgm.lessor.entity.ComplaintStatus;
import com.pgm.lessor.entity.Hostel;
import com.pgm.lessor.repository.*;
import com.pgm.lessor.security.UserPrincipal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Occupancy/revenue aggregates for the lessor's dashboard. This is exactly the kind
 * of heavy, repeated read that spikes at month-end when every lessor checks rent
 * status at once - it's short-TTL cached (see app.cache in application.yml) so a
 * burst of requests for the same hostel hits Postgres once every 30s instead of once
 * per request; swap the Caffeine cache manager for Redis later without touching this
 * method.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final BedRepository bedRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ComplaintRepository complaintRepository;
    private final BookingRepository bookingRepository;
    private final HostelService hostelService;

    public DashboardService(
            BedRepository bedRepository,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            ComplaintRepository complaintRepository,
            BookingRepository bookingRepository,
            HostelService hostelService) {
        this.bedRepository = bedRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.complaintRepository = complaintRepository;
        this.bookingRepository = bookingRepository;
        this.hostelService = hostelService;
    }

    @Cacheable(cacheNames = "dashboardAggregates", key = "#principal.tenantId()")
    public DashboardResponse getDashboard(UserPrincipal principal) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        Long hostelId = hostel.getId();

        long available = bedRepository.countByHostelIdAndStatus(hostelId, BedStatus.AVAILABLE);
        long occupied = bedRepository.countByHostelIdAndStatus(hostelId, BedStatus.BOOKED);
        long maintenance = bedRepository.countByHostelIdAndStatus(hostelId, BedStatus.MAINTENANCE);
        long total = available + occupied + maintenance;

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        double collected = paymentRepository.sumAmountSince(hostelId, monthStart);
        double due = invoiceRepository.sumDueByHostel(hostelId);

        long openComplaints = complaintRepository.countByHostelIdAndStatus(hostelId, ComplaintStatus.OPEN);
        long pendingBookings = bookingRepository.countByBedHostelIdAndStatus(hostelId, BookingStatus.PENDING);

        double occupancyRate = total == 0 ? 0 : (double) occupied / total;

        return new DashboardResponse(hostelId, total, available, occupied, maintenance, occupancyRate, collected, due, openComplaints, pendingBookings);
    }
}
