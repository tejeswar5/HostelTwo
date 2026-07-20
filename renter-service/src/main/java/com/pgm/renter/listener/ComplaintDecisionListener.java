package com.pgm.renter.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgm.renter.entity.Complaint;
import com.pgm.renter.entity.ComplaintStatus;
import com.pgm.renter.event.ComplaintDecisionEvent;
import com.pgm.renter.repository.ComplaintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class ComplaintDecisionListener {

    private static final Logger log = LoggerFactory.getLogger(ComplaintDecisionListener.class);

    private final ComplaintRepository complaintRepository;
    private final ObjectMapper objectMapper;

    public ComplaintDecisionListener(ComplaintRepository complaintRepository, ObjectMapper objectMapper) {
        this.complaintRepository = complaintRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "complaint-decisions", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onComplaintDecision(String payload) {
        try {
            ComplaintDecisionEvent event = objectMapper.readValue(payload, ComplaintDecisionEvent.class);
            UUID complaintRef = UUID.fromString(event.complaintRef());
            Complaint complaint = complaintRepository.findByComplaintRef(complaintRef).orElse(null);
            if (complaint == null) {
                log.warn("Complaint decision for unknown complaintRef {} - dropped", complaintRef);
                return;
            }
            complaint.setStatus(ComplaintStatus.valueOf(event.status()));
        } catch (Exception e) {
            log.error("Failed to process complaint-decisions payload: {}", payload, e);
        }
    }
}
