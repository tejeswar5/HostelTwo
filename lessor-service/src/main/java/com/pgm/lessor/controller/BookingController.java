package com.pgm.lessor.controller;

import com.pgm.lessor.dto.booking.BookingResponse;
import com.pgm.lessor.entity.BookingStatus;
import com.pgm.lessor.security.UserPrincipal;
import com.pgm.lessor.service.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessor/bookings")
@PreAuthorize("hasRole('LESSOR')")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingResponse> list(@AuthenticationPrincipal UserPrincipal principal, @RequestParam(required = false) BookingStatus status) {
        return bookingService.list(principal, status);
    }

    @PostMapping("/{bookingId}/approve")
    public BookingResponse approve(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long bookingId) {
        return bookingService.approve(principal, bookingId);
    }

    @PostMapping("/{bookingId}/reject")
    public BookingResponse reject(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long bookingId) {
        return bookingService.reject(principal, bookingId);
    }
}
