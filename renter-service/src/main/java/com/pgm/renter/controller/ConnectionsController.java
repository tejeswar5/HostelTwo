package com.pgm.renter.controller;

import com.pgm.renter.dto.connections.ConnectionResponse;
import com.pgm.renter.dto.connections.ConnectionsRequest;
import com.pgm.renter.security.UserPrincipal;
import com.pgm.renter.service.ConnectionsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renter/hostels/{hostelId}/connections")
@PreAuthorize("hasRole('RENTER')")
public class ConnectionsController {

    private final ConnectionsService connectionsService;

    public ConnectionsController(ConnectionsService connectionsService) {
        this.connectionsService = connectionsService;
    }

    @PostMapping
    public List<ConnectionResponse> find(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long hostelId,
            @RequestBody ConnectionsRequest request) {
        return connectionsService.findAtHostel(principal.userId(), hostelId, request);
    }
}
