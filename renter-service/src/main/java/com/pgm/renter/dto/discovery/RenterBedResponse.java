package com.pgm.renter.dto.discovery;

import com.pgm.renter.entity.BedStatus;

import java.time.LocalDate;

/**
 * Renter-facing bed view - deliberately omits the lessor's maintenance_reason free
 * text, which may contain internal notes; renters only need to know a bed isn't
 * bookable and, if booked, when it's expected to free up.
 */
public record RenterBedResponse(Long id, String bedNumber, BedStatus status, LocalDate expectedVacateDate) {
}
