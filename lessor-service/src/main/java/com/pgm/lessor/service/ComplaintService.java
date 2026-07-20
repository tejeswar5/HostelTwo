package com.pgm.lessor.service;

import com.pgm.lessor.audit.Auditable;
import com.pgm.lessor.dto.complaint.ComplaintResponse;
import com.pgm.lessor.entity.Complaint;
import com.pgm.lessor.entity.ComplaintStatus;
import com.pgm.lessor.entity.Hostel;
import com.pgm.lessor.exception.NotFoundException;
import com.pgm.lessor.repository.ComplaintRepository;
import com.pgm.lessor.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final HostelService hostelService;
    private final EventPublisher eventPublisher;

    public ComplaintService(ComplaintRepository complaintRepository, HostelService hostelService, EventPublisher eventPublisher) {
        this.complaintRepository = complaintRepository;
        this.hostelService = hostelService;
        this.eventPublisher = eventPublisher;
    }

    public List<ComplaintResponse> list(UserPrincipal principal, ComplaintStatus statusFilter) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        List<Complaint> complaints = statusFilter == null
                ? complaintRepository.findByHostelIdOrderByCreatedAtDesc(hostel.getId())
                : complaintRepository.findByHostelIdAndStatusOrderByCreatedAtDesc(hostel.getId(), statusFilter);
        return complaints.stream().map(this::toResponse).toList();
    }

    @Transactional
    @Auditable(action = "COMPLAINT_STATUS_UPDATED", entityType = "COMPLAINT")
    public ComplaintResponse updateStatus(UserPrincipal principal, Long complaintId, ComplaintStatus newStatus) {
        Hostel hostel = hostelService.requireMyHostel(principal);
        Complaint complaint = complaintRepository.findByIdAndHostelId(complaintId, hostel.getId())
                .orElseThrow(() -> new NotFoundException("Complaint not found"));
        complaint.setStatus(newStatus);

        eventPublisher.publishComplaintDecision(complaint.getComplaintRef().toString(), complaint.getRaisedById(),
                complaint.getCategory(), newStatus.name());
        return toResponse(complaint);
    }

    private ComplaintResponse toResponse(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getHostel().getId(),
                complaint.getRaisedByName(),
                complaint.getCategory(),
                complaint.getDescription(),
                complaint.getStatus(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt());
    }
}
