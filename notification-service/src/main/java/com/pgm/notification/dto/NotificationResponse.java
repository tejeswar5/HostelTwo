package com.pgm.notification.dto;

import com.pgm.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String body,
        LocalDateTime createdAt,
        LocalDateTime readAt) {
}
