package com.pgm.lessor.repository;

import com.pgm.lessor.dto.discovery.PublicHostelSummaryResponse;
import com.pgm.lessor.entity.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HostelRepository extends JpaRepository<Hostel, Long> {
    Optional<Hostel> findByTenantId(String tenantId);

    boolean existsByTenantId(String tenantId);

    @Query("""
            select new com.pgm.lessor.dto.discovery.PublicHostelSummaryResponse(
                h.id, h.name, h.contactPhone, h.contactEmail, h.hasLift,
                a.area, a.city, a.state,
                (select count(b) from Bed b where b.hostel = h and b.status = com.pgm.lessor.entity.BedStatus.AVAILABLE),
                (select min(r.monthlyRent) from Room r where r.hostel = h)
            )
            from Hostel h left join h.address a
            where (:city is null or lower(a.city) = lower(:city))
              and (:sharingType is null or exists (select 1 from Room r2 where r2.hostel = h and r2.sharingType = :sharingType))
              and (:airConditioned is null or exists (select 1 from Room r3 where r3.hostel = h and r3.airConditioned = :airConditioned))
            order by h.name
            """)
    List<PublicHostelSummaryResponse> discover(
            @Param("city") String city,
            @Param("sharingType") Integer sharingType,
            @Param("airConditioned") Boolean airConditioned);

    @Query("""
            select new com.pgm.lessor.dto.discovery.PublicHostelSummaryResponse(
                h.id, h.name, h.contactPhone, h.contactEmail, h.hasLift,
                a.area, a.city, a.state,
                (select count(b) from Bed b where b.hostel = h and b.status = com.pgm.lessor.entity.BedStatus.AVAILABLE),
                (select min(r.monthlyRent) from Room r where r.hostel = h)
            )
            from Hostel h left join h.address a
            order by h.name
            """)
    List<PublicHostelSummaryResponse> showAllHostels();
}
