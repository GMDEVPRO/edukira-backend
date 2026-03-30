package com.edukira.dto.response;

import com.edukira.enums.LeadStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LeadResponse {
    private UUID id;
    private String name;
    private String email;
    private String school;
    private String phone;
    private String message;
    private LeadStatus status;
    private String source;
    private String language;
    private Instant createdAt;
}
