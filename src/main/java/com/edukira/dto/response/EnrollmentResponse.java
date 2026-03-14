package com.edukira.dto.response;

import com.edukira.enums.EnrollmentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class EnrollmentResponse {
    private UUID id;
    private String studentFirstName;
    private String studentLastName;
    private String classLevel;
    private String guardianName;
    private String guardianPhone;
    private EnrollmentStatus status;
    private String paymentMethod;
    private BigDecimal paymentAmount;
    private boolean paymentConfirmed;
    private String rejectionReason;
    private Instant createdAt;
    private Instant reviewedAt;
}
