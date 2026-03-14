package com.edukira.controller;

import com.edukira.dto.response.RankingResponse;
import com.edukira.service.RankingService;
import com.edukira.util.TenantUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/national/{countryCode}")
    public ResponseEntity<List<RankingResponse>> getNational(
            @PathVariable String countryCode,
            @RequestParam(defaultValue = "2025") String year,
            @RequestParam(defaultValue = "ANNUAL") String period) {
        return ResponseEntity.ok(rankingService.getNationalRanking(countryCode, year, period));
    }

    @GetMapping("/global")
    public ResponseEntity<List<RankingResponse>> getGlobal(
            @RequestParam(defaultValue = "2025") String year,
            @RequestParam(defaultValue = "ANNUAL") String period) {
        return ResponseEntity.ok(rankingService.getGlobalRanking(year, period));
    }

    @GetMapping("/my-school")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','ADMIN')")
    public ResponseEntity<RankingResponse> getMySchool(
            @RequestParam(defaultValue = "2025") String year,
            @RequestParam(defaultValue = "ANNUAL") String period) {
        UUID schoolId = TenantUtil.currentSchoolId();
        return ResponseEntity.ok(rankingService.getSchoolRanking(schoolId, year, period));
    }

    @PostMapping("/compute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> compute(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String period) {
        rankingService.computeRankings(year, period);
        return ResponseEntity.ok().build();
    }
}
