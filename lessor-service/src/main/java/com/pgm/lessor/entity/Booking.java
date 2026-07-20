package com.pgm.lessor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The renter who requested this bed lives in renter-service's own database, not
 * this one - renterId/renterName/renterEmail are a snapshot taken from the
 * BookingRequestedEvent at creation time, not a foreign key. bookingRef is the
 * business key renter-service uses to correlate this booking with its own local
 * read-model row when it consumes our decision event back.
 */
@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_ref", nullable = false, unique = true)
    private UUID bookingRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @Column(name = "renter_name", nullable = false)
    private String renterName;

    @Column(name = "renter_email")
    private String renterEmail;

    @Column(name = "renter_phone")
    private String renterPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "requested_check_in", nullable = false)
    private LocalDate requestedCheckIn;

    @Column(name = "requested_check_out")
    private LocalDate requestedCheckOut;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;

    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = BookingStatus.PENDING;
        }
        if (bookingRef == null) {
            bookingRef = UUID.randomUUID();
        }
    }
}
