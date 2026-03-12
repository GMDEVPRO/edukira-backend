package com.edukira.dto.response;

import com.edukira.enums.GradePeriod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class GradeResponse {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String subjectName;
    private BigDecimal grade1;
    private BigDecimal grade2;
    private BigDecimal average;
    private BigDecimal coefficient;
    private GradePeriod period;
    private String year;
    private Boolean published;
    private Instant publishedAt;
    private Instant updatedAt;

}
