package com.edukira.controller;

import com.edukira.dto.request.AttendanceRequest;
import com.edukira.dto.response.AttendanceResponse;
import com.edukira.dto.response.AttendanceSummaryResponse;
import com.edukira.security.SchoolContext;
import com.edukira.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/class/{classLevel}")
    public ResponseEntity<AttendanceSummaryResponse> record(
            @PathVariable String classLevel,
            @Valid @RequestBody AttendanceRequest request) {
        UUID schoolId = SchoolContext.getSchoolId();
        UUID userId   = SchoolContext.getUserId();
        return ResponseEntity.ok(attendanceService.record(request, classLevel, schoolId, userId));
    }

    @GetMapping("/class/{classLevel}")
    public ResponseEntity<AttendanceSummaryResponse> getByClass(
            @PathVariable String classLevel,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UUID schoolId = SchoolContext.getSchoolId();
        return ResponseEntity.ok(attendanceService.getByClassAndDate(classLevel, date, schoolId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceResponse>> getByStudent(
            @PathVariable UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID schoolId = SchoolContext.getSchoolId();
        return ResponseEntity.ok(attendanceService.getByStudent(studentId, from, to, schoolId));
    }

    @GetMapping("/student/{studentId}/rate")
    public ResponseEntity<Double> getRate(
            @PathVariable UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getAttendanceRate(studentId, from, to));
    }
}
