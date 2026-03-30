package com.edukira.controller;

import com.edukira.dto.request.LeadRequest;
import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.LeadResponse;
import com.edukira.enums.LeadStatus;
import com.edukira.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    // ── Público: landing page envia aqui ──────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> create(
            @Valid @RequestBody LeadRequest request) {
        LeadResponse response = leadService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Solicitação recebida com sucesso!", response));
    }

    // ── Protegido: apenas admins Edukira consultam leads ──────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(leadService.findAll()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> findByStatus(
            @PathVariable LeadStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(leadService.findByStatus(status)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LeadResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam LeadStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(leadService.updateStatus(id, status)));
    }
}