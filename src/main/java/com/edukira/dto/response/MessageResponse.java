package com.edukira.dto.response;

import com.edukira.enums.MessageChannel;
import com.edukira.enums.MessageStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private UUID id;
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private String body;
    private MessageChannel channel;
    private MessageStatus status;
    private String externalMessageId;
    private Instant sentAt;
}
