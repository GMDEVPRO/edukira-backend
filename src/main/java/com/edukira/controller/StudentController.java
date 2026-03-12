package com.edukira.controller;

import com.edukira.dto.request.StudentRequest;
import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.StudentResponse;
import com.edukira.service.StudentService;
import com.edukira.util.TenantUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/students")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Students", description = "Gestão de alunos")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @Operation(summary = "Listar alunos da escola (paginado)")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                studentService.findAll(TenantUtil.currentSchoolId(), pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar aluno por ID")
    public ResponseEntity<ApiResponse<StudentResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                studentService.findById(id, TenantUtil.currentSchoolId())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'ADMIN')")
    @Operation(summary = "Criar novo aluno")
    public ResponseEntity<ApiResponse<StudentResponse>> create(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                studentService.create(request, TenantUtil.currentSchoolId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'ADMIN')")
    @Operation(summary = "Actualizar aluno")
    public ResponseEntity<ApiResponse<StudentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                studentService.update(id, request, TenantUtil.currentSchoolId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'ADMIN')")
    @Operation(summary = "Desactivar aluno (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        studentService.delete(id, TenantUtil.currentSchoolId());
        return ResponseEntity.ok(ApiResponse.ok("Aluno desactivado", null));
    }
}
