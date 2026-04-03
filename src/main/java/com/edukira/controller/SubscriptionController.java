package com.edukira.controller;

import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.SubscriptionResponse;
import com.edukira.entity.Subscription;
import com.edukira.enums.StudentStatus;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.StudentRepository;
import com.edukira.repository.SubscriptionRepository;
import com.edukira.security.SchoolContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Plano e limites da escola")
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepo;
    private final StudentRepository      studentRepo;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ver plano actual, limite de alunos e vagas restantes")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> myPlan() {
        UUID schoolId = SchoolContext.getSchoolId();

        Subscription sub = subscriptionRepo.findBySchoolId(schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Assinatura"));

        long count = studentRepo.countBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE);

        SubscriptionResponse response = SubscriptionResponse.builder()
                .id(sub.getId())
                .schoolId(schoolId)
                .plan(sub.getPlan())
                .status(sub.getStatus())
                .studentLimit(sub.getStudentLimit())
                .currentStudentCount(count)
                .remainingSlots(sub.remainingSlots(count))
                .active(sub.isActive())
                .trialEndsAt(sub.getTrialEndsAt())
                .currentPeriodEnd(sub.getCurrentPeriodEnd())
                .createdAt(sub.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}