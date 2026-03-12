package com.edukira.service;

import com.edukira.dto.request.AttendanceRequest;
import com.edukira.dto.response.AttendanceResponse;
import com.edukira.dto.response.AttendanceSummaryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    AttendanceSummaryResponse record(AttendanceRequest request, String classLevel, UUID schoolId, UUID userId);

    AttendanceSummaryResponse getByClassAndDate(String classLevel, LocalDate date, UUID schoolId);

    List<AttendanceResponse> getByStudent(UUID studentId, LocalDate from, LocalDate to, UUID schoolId);

    double getAttendanceRate(UUID studentId, LocalDate from, LocalDate to);
}
