package com.pgm.lessor.controller;

import com.pgm.lessor.dto.dashboard.DashboardResponse;
import com.pgm.lessor.security.UserPrincipal;
import com.pgm.lessor.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lessor/dashboard")
@PreAuthorize("hasRole('LESSOR')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return dashboardService.getDashboard(principal);
    }
}
