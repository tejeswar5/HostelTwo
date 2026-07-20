package com.pgm.renter.controller;

import com.pgm.renter.dto.discovery.DiscoverHostelResponse;
import com.pgm.renter.dto.discovery.HostelDetailResponse;
import com.pgm.renter.service.DiscoveryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renter/hostels")
@PreAuthorize("hasRole('RENTER')")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping
    public List<DiscoverHostelResponse> discover(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer sharingType,
            @RequestParam(required = false) Boolean airConditioned) {
        return discoveryService.discover(city, sharingType, airConditioned);
    }

    @GetMapping("/{hostelId}")
    public HostelDetailResponse getDetail(@PathVariable Long hostelId) {
        return discoveryService.getHostelDetail(hostelId);
    }
}
