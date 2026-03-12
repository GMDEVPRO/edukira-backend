package com.edukira.controller;

import com.edukira.dto.request.StudentContactUpdateRequest;
import com.edukira.dto.response.*;
import com.edukira.service.StudentPortalService;
import com.edukira.util.TenantUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/student/portal")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Student Portal", description = "Painel do aluno — perfil, notas, pagamentos, documentos")
public class StudentPortalController {

    private final StudentPortalService portalService;

    // ── PERFIL ───────────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Ver o meu perfil completo — dados, resumo académico e financeiro")
    public ResponseEntity<ApiResponse<StudentPortalResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.ok(
                portalService.getMyProfile(TenantUtil.currentStudentAccountId())));
    }

    @PatchMapping("/me/contact")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Actualizar email e/ou telefone")
    public ResponseEntity<ApiResponse<StudentPortalResponse>> updateContact(
            @Valid @RequestBody StudentContactUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                portalService.updateContact(TenantUtil.currentStudentAccountId(), request)));
    }

    // ── NOTAS ────────────────────────────────────────────────

    @GetMapping("/grades")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Ver as minhas notas por bimestre (apenas notas publicadas)")
    public ResponseEntity<ApiResponse<List<StudentGradePortalResponse>>> getMyGrades() {
        return ResponseEntity.ok(ApiResponse.ok(
                portalService.getMyGrades(TenantUtil.currentStudentId())));
    }

    // ── PAGAMENTOS ───────────────────────────────────────────

    @GetMapping("/payments")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Ver pagamentos pagos, em atraso e próximas mensalidades")
    public ResponseEntity<ApiResponse<StudentPaymentPortalResponse>> getMyPayments() {
        return ResponseEntity.ok(ApiResponse.ok(
                portalService.getMyPayments(
                        TenantUtil.currentStudentId(),
                        TenantUtil.currentSchoolId())));
    }

    // ── DOCUMENTOS ───────────────────────────────────────────

    @GetMapping("/documents")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Ver os meus documentos — boletins, certidões, declarações")
    public ResponseEntity<ApiResponse<List<StudentDocumentResponse>>> getMyDocuments() {
        return ResponseEntity.ok(ApiResponse.ok(
                portalService.getMyDocuments(TenantUtil.currentStudentId())));
    }

    // ── ADMIN: GESTÃO DE CONTAS ──────────────────────────────

    @PostMapping("/admin/accounts/{accountId}/approve")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    @Operation(summary = "Admin aprova conta de aluno pendente")
    public ResponseEntity<ApiResponse<Void>> approveAccount(@PathVariable UUID accountId) {
        portalService.approveAccount(
                accountId,
                TenantUtil.currentUserId(),
                TenantUtil.currentSchoolId());
        return ResponseEntity.ok(ApiResponse.ok("Conta aprovada com sucesso", null));
    }

    @PostMapping("/admin/accounts/{accountId}/reject")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    @Operation(summary = "Admin rejeita conta de aluno pendente")
    public ResponseEntity<ApiResponse<Void>> rejectAccount(
            @PathVariable UUID accountId,
            @RequestParam String reason) {
        portalService.rejectAccount(accountId, reason, TenantUtil.currentSchoolId());
        return ResponseEntity.ok(ApiResponse.ok("Conta rejeitada", null));
    }
}
