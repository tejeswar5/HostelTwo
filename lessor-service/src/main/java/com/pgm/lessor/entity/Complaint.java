package com.pgm.lessor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The renter who raised this complaint lives in renter-service's own database -
 * raisedById/raisedByName/raisedByEmail are a snapshot taken from the
 * ComplaintRequestedEvent, not a foreign key. complaintRef is the business key
 * renter-service uses to match this row back to its own local copy.
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Column(name = "raised_by_id", nullable = false)
    private Long raisedById;

    @Column(name = "raised_by_name", nullable = false)
    private String raisedByName;

    @Column(name = "raised_by_email")
    private String raisedByEmail;

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
