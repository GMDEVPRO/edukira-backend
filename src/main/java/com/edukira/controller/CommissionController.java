package com.edukira.controller;

import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.CommissionResponse;
import com.edukira.dto.response.CommissionSummaryResponse;
import com.edukira.service.CommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/commissions")
@RequiredArgsConstructor
@Tag(name = "Commissions", description = "Gestão de comissões da plataforma Edukira")
public class CommissionController {

    private final CommissionService commissionService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_ADMIN')")
    @Operation(summary = "Resumo total de comissões (admin)")
    public ResponseEntity<ApiResponse<CommissionSummaryResponse>> summary() {
        return ResponseEntity.ok(ApiResponse.ok(commissionService.summary()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_ADMIN')")
    @Operation(summary = "Lista paginada de todas as comissões (admin)")
    public ResponseEntity<ApiResponse<Page<CommissionResponse>>> listAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(commissionService.listAll(pageable)));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Comissões pagas pelo vendedor autenticado")
    public ResponseEntity<ApiResponse<Page<CommissionResponse>>> mySeller(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(commissionService.mySeller(pageable)));
    }
}
