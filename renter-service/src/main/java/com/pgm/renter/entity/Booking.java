package com.pgm.renter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This is an eventually-consistent read-model, not the source of truth. The
 * authoritative row (and the double-booking guard) lives in lessor-service, which
 * owns the bed. Renter-service writes a PENDING row here optimistically when the
 * renter submits a request, then updates status/decidedAt when the
 * booking-decisions Kafka event comes back, matched by bookingRef.
 * bedId/bedNumber/roomNumber/hostelId/hostelName are a snapshot the client already
 * had from the discovery page, not a live join - renter-service no longer holds
 * hostel/room/bed data locally after the DB split.
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

    @Column(name = "bed_id", nullable = false)
    private Long bedId;

    @Column(name = "bed_number", nullable = false)
    private String bedNumber;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "hostel_id", nullable = false)
    private Long hostelId;

    @Column(name = "hostel_name", nullable = false)
    private String hostelName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;

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
