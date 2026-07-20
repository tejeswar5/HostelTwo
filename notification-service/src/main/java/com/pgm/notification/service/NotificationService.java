package com.pgm.notification.service;

import com.pgm.notification.dto.NotificationResponse;
import com.pgm.notification.entity.Notification;
import com.pgm.notification.entity.NotificationType;
import com.pgm.notification.exception.NotFoundException;
import com.pgm.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> list(Long userId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void notify(Long recipientUserId, NotificationType type, String title, String body) {
        notificationRepository.save(Notification.builder()
                .recipientUserId(recipientUserId)
                .type(type)
                .title(title)
                .body(body)
                .build());
    }

    @Transactional
    public NotificationResponse markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        if (!notification.getRecipientUserId().equals(userId)) {
            throw new NotFoundException("Notification not found");
        }
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getCreatedAt(),
                notification.getReadAt());
    }
}
