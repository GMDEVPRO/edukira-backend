package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RankingResponse {
    private UUID schoolId;
    private String schoolName;
    private String city;
    private String countryCode;
    private String year;
    private String period;
    private BigDecimal academicScore;
    private BigDecimal attendanceScore;
    private BigDecimal paymentScore;
    private BigDecimal globalScore;
    private Integer nationalRank;
    private Integer regionalRank;
    private Integer totalStudents;
    private BigDecimal passRate;
    private BigDecimal attendanceRate;
    private BigDecimal paymentRate;
    private Instant computedAt;
}