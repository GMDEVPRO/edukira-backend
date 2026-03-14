package com.edukira.service;

import com.edukira.dto.request.EnrollmentRequest;
import com.edukira.dto.request.EnrollmentReviewRequest;
import com.edukira.dto.response.EnrollmentResponse;
import com.edukira.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponse submit(EnrollmentRequest request, UUID schoolId);

    Page<EnrollmentResponse> findAll(UUID schoolId, EnrollmentStatus status, Pageable pageable);

    EnrollmentResponse findById(UUID id, UUID schoolId);

    EnrollmentResponse review(UUID id, EnrollmentReviewRequest request, UUID schoolId, UUID reviewerId);

    EnrollmentResponse confirmPayment(UUID id, UUID schoolId);
}
