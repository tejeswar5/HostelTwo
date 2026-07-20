package com.pgm.renter.service;

import com.pgm.renter.audit.Auditable;
import com.pgm.renter.dto.complaint.ComplaintResponse;
import com.pgm.renter.dto.complaint.RaiseComplaintRequest;
import com.pgm.renter.entity.Complaint;
import com.pgm.renter.entity.User;
import com.pgm.renter.repository.ComplaintRepository;
import com.pgm.renter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Same read-model pattern as {@link BookingService#create}: the authoritative
 * complaint row is created by lessor-service (which owns hostel_id as a real FK)
 * after consuming the complaint-requests event; this local row is an optimistic
 * copy for "My complaints", updated by {@link com.pgm.renter.listener.ComplaintDecisionListener}.
 */
@Service
@Transactional(readOnly = true)
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public ComplaintService(ComplaintRepository complaintRepository, UserRepository userRepository, EventPublisher eventPublisher) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Auditable(action = "COMPLAINT_RAISED", entityType = "COMPLAINT")
    public ComplaintResponse raise(Long renterId, RaiseComplaintRequest request) {
        User renter = userRepository.findById(renterId).orElseThrow();
        UUID complaintRef = UUID.randomUUID();

        Complaint complaint = Complaint.builder()
                .complaintRef(complaintRef)
                .hostelId(request.hostelId())
                .hostelName(request.hostelName())
                .raisedBy(renter)
                .category(request.category())
                .description(request.description())
                .build();
        complaint = complaintRepository.save(complaint);

        eventPublisher.publishComplaintRequested(
                complaintRef.toString(),
                request.hostelId(),
                renterId,
                renter.getFname() + " " + renter.getLname(),
                renter.getEmail(),
                request.category(),
                request.description());

        return toResponse(complaint);
    }

    public List<ComplaintResponse> myComplaints(Long renterId) {
        return complaintRepository.findByRaisedByIdOrderByCreatedAtDesc(renterId).stream().map(this::toResponse).toList();
    }

    private ComplaintResponse toResponse(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getHostelId(),
                complaint.getHostelName(),
                complaint.getCategory(),
                complaint.getDescription(),
                complaint.getStatus(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt());
    }
}
