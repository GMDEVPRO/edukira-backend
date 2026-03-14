package com.edukira.controller;

import com.edukira.dto.response.CountryConfigResponse;
import com.edukira.entity.CountryConfig;
import com.edukira.repository.CountryConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/countries")
@RequiredArgsConstructor
public class CountryConfigController {

    private final CountryConfigRepository countryConfigRepo;

    @GetMapping
    public ResponseEntity<List<CountryConfigResponse>> getAll() {
        return ResponseEntity.ok(
                countryConfigRepo.findByActiveTrue()
                        .stream().map(this::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{code}")
    public ResponseEntity<CountryConfigResponse> getByCode(@PathVariable String code) {
        return countryConfigRepo.findById(code.toUpperCase())
                .map(c -> ResponseEntity.ok(toResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    private CountryConfigResponse toResponse(CountryConfig c) {
        return CountryConfigResponse.builder()
                .countryCode(c.getCountryCode())
                .countryName(c.getCountryName())
                .currency(c.getCurrency())
                .timezone(c.getTimezone())
                .academicYearStartMonth(c.getAcademicYearStartMonth())
                .gradingMax(c.getGradingMax())
                .supportsWave(c.isSupportsWave())
                .supportsOrangeMoney(c.isSupportsOrangeMoney())
                .supportsMtnMomo(c.isSupportsMtnMomo())
                .supportsAirtelMoney(c.isSupportsAirtelMoney())
                .supportsMpesa(c.isSupportsMpesa())
                .phonePrefix(c.getPhonePrefix())
                .build();
    }
}
