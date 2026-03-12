package com.edukira.dto.request;

import com.edukira.enums.MessageChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BroadcastMessageRequest {
    @NotBlank
    private String body;

    @NotNull
    private MessageChannel channel;

    // null = toda a escola | preenchido = só uma turma
    private String classLevel;
}
