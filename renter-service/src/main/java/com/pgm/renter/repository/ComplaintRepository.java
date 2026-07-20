package com.pgm.renter.repository;

import com.pgm.renter.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByRaisedByIdOrderByCreatedAtDesc(Long raisedById);

    Optional<Complaint> findByComplaintRef(UUID complaintRef);
}
