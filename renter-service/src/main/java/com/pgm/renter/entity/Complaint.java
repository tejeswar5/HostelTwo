package com.pgm.renter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-model, same reasoning as {@link Booking}: the authoritative complaint row
 * lives in lessor-service (hostel_id there is a real FK it can enforce); this one
 * is written optimistically on raise() and updated when the complaint-decisions
 * event comes back, matched by complaintRef.
 */
@Entity
@Table(name = "complaint")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_ref", nullable = false, unique = true)
    private UUID complaintRef;

    @Column(name = "hostel_id", nullable = false)
    private Long hostelId;

    @Column(name = "hostel_name", nullable = false)
    private String hostelName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raised_by", nullable = false)
    private User raisedBy;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = ComplaintStatus.OPEN;
        }
        if (complaintRef == null) {
            complaintRef = UUID.randomUUID();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
