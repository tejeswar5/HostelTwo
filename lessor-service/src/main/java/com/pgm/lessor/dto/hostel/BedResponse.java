package com.pgm.lessor.dto.hostel;

import com.pgm.lessor.entity.BedStatus;

import java.time.LocalDate;

public record BedResponse(
        Long id,
        Long floorId,
        Integer floorNumber,
        Long roomId,
        String roomNumber,
        String bedNumber,
        BedStatus status,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        boolean nextMonthRentPaid,
        String maintenanceReason,
        Double monthlyRent) {
}
