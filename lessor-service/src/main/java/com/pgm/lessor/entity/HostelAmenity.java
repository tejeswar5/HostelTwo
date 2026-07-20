package com.pgm.lessor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hostel_amenity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostelAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @Column(nullable = false)
    private Integer quantity;
}
