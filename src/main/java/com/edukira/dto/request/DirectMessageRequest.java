package com.edukira.dto.request;

import com.edukira.enums.MessageChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DirectMessageRequest {
    @NotNull
    private UUID studentId;

    @NotBlank
    private String body;

    @NotNull
    private MessageChannel channel;
}
