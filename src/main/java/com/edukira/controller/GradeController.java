package com.edukira.controller;

import com.edukira.dto.request.GradeBatchRequest;
import com.edukira.dto.request.GradeRequest;
import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.ClassSummaryResponse;
import com.edukira.dto.response.GradeResponse;
import com.edukira.dto.response.ReportCardResponse;
import com.edukira.enums.GradePeriod;
import com.edukira.service.GradeService;
import com.edukira.util.TenantUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('TEACHER','SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> saveBatch(
            @Valid @RequestBody GradeBatchRequest request) {
        UUID schoolId = TenantUtil.currentSchoolId();
        List<GradeResponse> result = gradeService.saveBatch(request, schoolId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<GradeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody GradeRequest request) {
        UUID schoolId = TenantUtil.currentSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(gradeService.update(id, request, schoolId)));
    }

    @GetMapping("/class/{classLevel}")
    @PreAuthorize("hasAnyRole('TEACHER','SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> findByClass(
            @PathVariable String classLevel,
            @RequestParam GradePeriod period,
            @RequestParam String year) {
        UUID schoolId = TenantUtil.currentSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(
                gradeService.findByClass(classLevel, period, year, schoolId)));
    }

    @GetMapping("/student/{id}/report")
    @PreAuthorize("hasAnyRole('TEACHER','SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ReportCardResponse>> getReportCard(
            @PathVariable UUID id,
            @RequestParam String year) {
        UUID schoolId = TenantUtil.currentSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(
                gradeService.getReportCard(id, year, schoolId)));
    }

    @GetMapping("/class/{classLevel}/summary")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ClassSummaryResponse>> getClassSummary(
            @PathVariable String classLevel,
            @RequestParam GradePeriod period,
            @RequestParam String year) {
        UUID schoolId = TenantUtil.currentSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(
                gradeService.getClassSummary(classLevel, period, year, schoolId)));
    }

    @PostMapping("/publish/{classLevel}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<String>> publishPeriod(
            @PathVariable String classLevel,
            @RequestParam GradePeriod period,
            @RequestParam String year) {
        UUID schoolId = TenantUtil.currentSchoolId();
        int count = gradeService.publishPeriod(classLevel, period, year, schoolId);
        return ResponseEntity.ok(ApiResponse.ok(
                count + " nota(s) publicada(s) e responsáveis notificados."));
    }
}