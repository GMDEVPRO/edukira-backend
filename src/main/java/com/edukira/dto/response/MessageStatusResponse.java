package com.edukira.dto.response;

import com.edukira.enums.MessageChannel;
import com.edukira.enums.MessageStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
@Data
@Builder
public class MessageStatusResponse {
    private UUID id;
    private MessageStatus status;
    private MessageChannel channel;
    private String externalMessageId;
    private String recipientPhone;
    private Instant sentAt;
    private String detail;
}
