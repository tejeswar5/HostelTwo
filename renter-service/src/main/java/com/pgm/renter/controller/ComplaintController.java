package com.pgm.renter.controller;

import com.pgm.renter.dto.complaint.ComplaintResponse;
import com.pgm.renter.dto.complaint.RaiseComplaintRequest;
import com.pgm.renter.security.UserPrincipal;
import com.pgm.renter.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renter/complaints")
@PreAuthorize("hasRole('RENTER')")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public ResponseEntity<ComplaintResponse> raise(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody RaiseComplaintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.raise(principal.userId(), request));
    }

    @GetMapping("/mine")
    public List<ComplaintResponse> myComplaints(@AuthenticationPrincipal UserPrincipal principal) {
        return complaintService.myComplaints(principal.userId());
    }
}
