package com.edukira.controller;

import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.NotificationLogResponse;
import com.edukira.dto.response.NotificationSummaryResponse;
import com.edukira.enums.NotificationChannel;
import com.edukira.enums.NotificationStatus;
import com.edukira.enums.NotificationType;
import com.edukira.security.SchoolContext;
import com.edukira.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Histórico e gestão de notificações enviadas")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    @Operation(summary = "Resumo de notificações da escola")
    public ResponseEntity<ApiResponse<NotificationSummaryResponse>> summary() {
        UUID schoolId = SchoolContext.getSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getSummary(schoolId)));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    @Operation(summary = "Histórico paginado de notificações enviadas")
    public ResponseEntity<ApiResponse<Page<NotificationLogResponse>>> history(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        UUID schoolId = SchoolContext.getSchoolId();
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getHistory(schoolId, type, channel, status, pageable)));
    }

    @PostMapping("/retry")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    @Operation(summary = "Tenta reenviar notificações com falha (máx. 3 tentativas)")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> retryFailed() {
        int count = notificationService.retryFailed();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("reenviadas", count)));
    }
}
