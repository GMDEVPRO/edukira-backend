package com.edukira.controller;

import com.edukira.dto.request.EnrollmentRequest;
import com.edukira.dto.request.EnrollmentReviewRequest;
import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.EnrollmentResponse;
import com.edukira.enums.EnrollmentStatus;
import com.edukira.service.EnrollmentService;
import com.edukira.util.TenantUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/public/{schoolId}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> submit(
            @PathVariable UUID schoolId,
            @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                enrollmentService.submit(request, schoolId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> findAll(
            @RequestParam(required = false) EnrollmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID schoolId = TenantUtil.currentSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(
                enrollmentService.findAll(schoolId, status, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> findById(@PathVariable UUID id) {
        UUID schoolId = TenantUtil.currentSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(
                enrollmentService.findById(id, schoolId)));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> review(
            @PathVariable UUID id,
            @Valid @RequestBody EnrollmentReviewRequest request) {
        UUID schoolId  = TenantUtil.currentSchoolId();
        UUID reviewerId = TenantUtil.currentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                enrollmentService.review(id, request, schoolId, reviewerId)));
    }

    @PutMapping("/{id}/confirm-payment")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> confirmPayment(@PathVariable UUID id) {
        UUID schoolId = TenantUtil.currentSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(
                enrollmentService.confirmPayment(id, schoolId)));
    }
}
