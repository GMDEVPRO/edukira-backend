package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SchoolRegistrationResponse {
    private UUID schoolId;
    private String schoolName;
    private String adminEmail;
    private String plan;
    private Instant trialEndsAt;
    private String status;
    private String message;
}
