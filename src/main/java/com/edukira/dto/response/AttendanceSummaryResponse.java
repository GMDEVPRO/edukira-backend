package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AttendanceSummaryResponse {
    private LocalDate date;
    private String classLevel;
    private String subject;
    private int totalStudents;
    private int totalPresent;
    private int totalAbsent;
    private int totalLate;
    private int totalExcused;
    private double attendanceRate;
    private List<AttendanceResponse> entries;
}
