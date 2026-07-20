package com.pgm.lessor.controller;

import com.pgm.lessor.dto.complaint.ComplaintResponse;
import com.pgm.lessor.dto.complaint.ComplaintStatusUpdateRequest;
import com.pgm.lessor.entity.ComplaintStatus;
import com.pgm.lessor.security.UserPrincipal;
import com.pgm.lessor.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessor/complaints")
@PreAuthorize("hasRole('LESSOR')")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public List<ComplaintResponse> list(@AuthenticationPrincipal UserPrincipal principal, @RequestParam(required = false) ComplaintStatus status) {
        return complaintService.list(principal, status);
    }

    @PatchMapping("/{complaintId}/status")
    public ComplaintResponse updateStatus(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long complaintId, @Valid @RequestBody ComplaintStatusUpdateRequest request) {
        return complaintService.updateStatus(principal, complaintId, request.status());
    }
}
