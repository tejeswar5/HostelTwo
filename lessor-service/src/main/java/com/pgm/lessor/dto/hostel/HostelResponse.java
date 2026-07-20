package com.pgm.lessor.dto.hostel;

public record HostelResponse(
        Long id,
        String name,
        String contactPhone,
        String contactEmail,
        boolean hasLift,
        String buildingNameOrNumber,
        String street,
        String area,
        String city,
        String state,
        Integer pinCode) {
}
