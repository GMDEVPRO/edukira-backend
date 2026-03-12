package com.edukira.dto.response;

import com.edukira.enums.GradePeriod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ReportCardResponse {
    private UUID studentId;
    private String studentName;
    private String classLevel;
    private String schoolYear;
    private Map<GradePeriod, List<SubjectGrade>> gradesByPeriod;
    private Map<GradePeriod, BigDecimal> averageByPeriod;
    private BigDecimal yearAverage;

    @Data
    @Builder
    public static class SubjectGrade {
        private String subjectName;
        private BigDecimal grade1;
        private BigDecimal grade2;
        private BigDecimal average;
        private BigDecimal coefficient;
        private String appreciation;
        private Boolean published;
    }
}
