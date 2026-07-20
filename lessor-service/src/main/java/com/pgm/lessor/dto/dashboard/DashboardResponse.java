package com.pgm.lessor.dto.dashboard;

public record DashboardResponse(
        Long hostelId,
        long totalBeds,
        long availableBeds,
        long occupiedBeds,
        long maintenanceBeds,
        double occupancyRate,
        double monthlyRevenueCollected,
        double monthlyRevenueDue,
        long openComplaints,
        long pendingBookingRequests) {
}
