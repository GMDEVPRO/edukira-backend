package com.edukira.controller;

import com.edukira.dto.request.StudentLoginRequest;
import com.edukira.dto.request.StudentRegisterRequest;
import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.StudentAuthResponse;
import com.edukira.service.StudentAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/student/auth")
@RequiredArgsConstructor
@Tag(name = "Student Auth", description = "Registo e login do aluno/tutor")
public class StudentAuthController {

    private final StudentAuthService studentAuthService;

    @PostMapping("/register")
    @Operation(summary = "Aluno ou tutor cria conta com nome + número de documento",
               description = "Conta criada com status PENDING_APPROVAL até admin aprovar")
    public ResponseEntity<ApiResponse<StudentAuthResponse>> register(
            @Valid @RequestBody StudentRegisterRequest request) {
        StudentAuthResponse response = studentAuthService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Conta criada! Aguarda aprovação da escola.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login do aluno com número de documento + password + schoolId")
    public ResponseEntity<ApiResponse<StudentAuthResponse>> login(
            @Valid @RequestBody StudentLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(studentAuthService.login(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout do aluno")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestParam String refreshToken) {
        studentAuthService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.ok("Sessão encerrada", null));
    }
}
