package com.pgm.lessor.controller;

import com.pgm.lessor.dto.discovery.PublicHostelDetailResponse;
import com.pgm.lessor.dto.discovery.PublicHostelSummaryResponse;
import com.pgm.lessor.service.PublicDiscoveryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Unauthenticated, read-only - browsing hostel listings carries no PII and is
 * called server-to-server by renter-service's DiscoveryService now that hostel
 * structure lives only in this service's database. See SecurityConfig for the
 * permitAll rule on /api/public/**.
 */
@RestController
@RequestMapping("/api/public/hostels")
public class PublicDiscoveryController {

    private final PublicDiscoveryService publicDiscoveryService;

    public PublicDiscoveryController(PublicDiscoveryService publicDiscoveryService) {
        this.publicDiscoveryService = publicDiscoveryService;
    }

    @GetMapping
    public List<PublicHostelSummaryResponse> discover(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer sharingType,
            @RequestParam(required = false) Boolean airConditioned) {
        return publicDiscoveryService.discover(city, sharingType, airConditioned);
    }

    @GetMapping("/{hostelId}")
    public PublicHostelDetailResponse getDetail(@PathVariable Long hostelId) {
        return publicDiscoveryService.getHostelDetail(hostelId);
    }
}
