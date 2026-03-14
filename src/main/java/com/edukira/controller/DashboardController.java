package com.edukira.controller;

import com.edukira.dto.response.DashboardResponse;
import com.edukira.security.SchoolContext;
import com.edukira.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        UUID schoolId = SchoolContext.getSchoolId();
        return ResponseEntity.ok(dashboardService.getDashboard(schoolId));
    }
}
