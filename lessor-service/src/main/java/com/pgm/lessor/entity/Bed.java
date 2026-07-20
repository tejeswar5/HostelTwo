package com.pgm.lessor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "bed")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "bed_number", nullable = false)
    private String bedNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedStatus status;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "next_month_rent_paid", nullable = false)
    private boolean nextMonthRentPaid;

    @Column(name = "maintenance_reason")
    private String maintenanceReason;

    @Version
    private Long version;

    public void markUnderMaintenance(String reason) {
        this.status = BedStatus.MAINTENANCE;
        this.maintenanceReason = reason;
    }

    public void markAvailable() {
        this.status = BedStatus.AVAILABLE;
        this.maintenanceReason = null;
        this.checkInDate = null;
        this.checkOutDate = null;
        this.nextMonthRentPaid = false;
    }

    public void markBooked(LocalDate checkIn, LocalDate checkOut) {
        this.status = BedStatus.BOOKED;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.nextMonthRentPaid = false;
    }
}
