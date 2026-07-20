package com.pgm.renter.controller;

import com.pgm.renter.dto.booking.BookingResponse;
import com.pgm.renter.dto.booking.CreateBookingRequest;
import com.pgm.renter.security.UserPrincipal;
import com.pgm.renter.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renter/bookings")
@PreAuthorize("hasRole('RENTER')")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(principal.userId(), request));
    }

    @GetMapping("/mine")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return bookingService.myBookings(principal.userId());
    }
}
