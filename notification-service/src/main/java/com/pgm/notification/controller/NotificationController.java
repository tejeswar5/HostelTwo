package com.pgm.notification.controller;

import com.pgm.notification.dto.NotificationResponse;
import com.pgm.notification.security.UserPrincipal;
import com.pgm.notification.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Single endpoint for both roles - unlike lessor/renter-service, this service
 * doesn't care whether the caller is a LESSOR or a RENTER, only whose userId is
 * on the validated JWT. That id is always the recipientUserId column, regardless
 * of which service originally issued the token.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return notificationService.list(principal.userId());
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long notificationId) {
        return notificationService.markRead(principal.userId(), notificationId);
    }
}
