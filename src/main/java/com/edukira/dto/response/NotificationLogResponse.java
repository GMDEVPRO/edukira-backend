package com.edukira.dto.response;

import com.edukira.enums.NotificationChannel;
import com.edukira.enums.NotificationStatus;
import com.edukira.enums.NotificationType;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationLogResponse {
    private UUID id;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String recipientName;
    private String recipientPhone;
    private String body;
    private String externalId;
    private String errorMessage;
    private int retryCount;
    private Instant sentAt;
    private Instant createdAt;
}
