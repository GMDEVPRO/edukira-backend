package com.edukira.dto.response;

import com.edukira.enums.GradePeriod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ClassSummaryResponse {
    private String classLevel;
    private GradePeriod period;
    private String year;
    private int totalStudents;
    private BigDecimal classAverage;
    private BigDecimal highestAverage;
    private BigDecimal lowestAverage;
    private List<SubjectAverage> subjectAverages;
    private List<StudentRank> topStudents;

    @Data
    @Builder
    public static class SubjectAverage {
        private String subjectName;
        private BigDecimal average;
        private int passCount;
        private int failCount;
    }

    @Data
    @Builder
    public static class StudentRank {
        private UUID studentId;
        private String studentName;
        private BigDecimal average;
        private int rank;
    }
}
