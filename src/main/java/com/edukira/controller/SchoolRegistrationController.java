package com.edukira.controller;

import com.edukira.dto.request.SchoolRegistrationRequest;
import com.edukira.dto.response.ApiResponse;
import com.edukira.dto.response.SchoolRegistrationResponse;
import com.edukira.service.SchoolRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/register")
@RequiredArgsConstructor
public class SchoolRegistrationController {

    private final SchoolRegistrationService registrationService;

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolRegistrationResponse>> register(
            @Valid @RequestBody SchoolRegistrationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                registrationService.register(request)));
    }
}
