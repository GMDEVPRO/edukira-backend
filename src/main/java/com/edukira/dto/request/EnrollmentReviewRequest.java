package com.edukira.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentReviewRequest {

    @NotNull
    private boolean approved;

    private String rejectionReason;
}
