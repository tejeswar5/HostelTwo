package com.pgm.renter.dto.connections;

public record ConnectionResponse(Long userId, String fullName, String phoneNumber, String roomNumber) {
}
