package com.pgm.lessor.dto.discovery;

import com.pgm.lessor.entity.BedStatus;

import java.time.LocalDate;

public record PublicBedResponse(Long id, String bedNumber, BedStatus status, LocalDate expectedVacateDate) {
}
