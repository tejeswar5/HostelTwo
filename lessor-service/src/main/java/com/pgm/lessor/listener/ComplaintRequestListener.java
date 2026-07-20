package com.pgm.lessor.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.lessor.entity.Complaint;
import com.pgm.lessor.entity.Hostel;
import com.pgm.lessor.event.ComplaintRequestedEvent;
import com.pgm.lessor.repository.ComplaintRepository;
import com.pgm.lessor.repository.HostelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class ComplaintRequestListener {

    private static final Logger log = LoggerFactory.getLogger(ComplaintRequestListener.class);

    private final ComplaintRepository complaintRepository;
    private final HostelRepository hostelRepository;
    private final ObjectMapper objectMapper;

    public ComplaintRequestListener(ComplaintRepository complaintRepository, HostelRepository hostelRepository, ObjectMapper objectMapper) {
        this.complaintRepository = complaintRepository;
        this.hostelRepository = hostelRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "complaint-requests", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onComplaintRequested(String payload) {
        ComplaintRequestedEvent event;
        try {
            event = objectMapper.readValue(payload, ComplaintRequestedEvent.class);
        } catch (Exception e) {
            log.error("Failed to parse complaint-requests payload: {}", payload, e);
            return;
        }

        Hostel hostel = hostelRepository.findById(event.hostelId()).orElse(null);
        if (hostel == null) {
            log.warn("Complaint request for unknown hostel {} - dropped", event.hostelId());
            return;
        }

        Complaint complaint = Complaint.builder()
                .complaintRef(UUID.fromString(event.complaintRef()))
                .hostel(hostel)
                .raisedById(event.raisedById())
                .raisedByName(event.raisedByName())
                .raisedByEmail(event.raisedByEmail())
                .category(event.category())
                .description(event.description())
                .build();
        complaintRepository.save(complaint);
    }
}
