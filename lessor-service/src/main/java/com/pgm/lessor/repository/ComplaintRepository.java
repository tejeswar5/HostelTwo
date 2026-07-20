package com.pgm.lessor.repository;

import com.pgm.lessor.entity.Complaint;
import com.pgm.lessor.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByHostelIdOrderByCreatedAtDesc(Long hostelId);

    List<Complaint> findByHostelIdAndStatusOrderByCreatedAtDesc(Long hostelId, ComplaintStatus status);

    Optional<Complaint> findByIdAndHostelId(Long id, Long hostelId);

    long countByHostelIdAndStatus(Long hostelId, ComplaintStatus status);
}
