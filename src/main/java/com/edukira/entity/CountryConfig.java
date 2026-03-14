package com.edukira.entity;

import com.edukira.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "country_configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CountryConfig {

    @Id
    @Column(name = "country_code", columnDefinition = "VARCHAR(3)")
    private String countryCode;

    @Column(nullable = false)
    private String countryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private String timezone;

    @Column(name = "academic_year_start_month", nullable = false)
    private int academicYearStartMonth;

    @Column(name = "grading_max", nullable = false)
    @Builder.Default
    private int gradingMax = 20;

    @Column(name = "supports_wave")
    @Builder.Default
    private boolean supportsWave = false;

    @Column(name = "supports_orange_money")
    @Builder.Default
    private boolean supportsOrangeMoney = false;

    @Column(name = "supports_mtn_momo")
    @Builder.Default
    private boolean supportsMtnMomo = false;

    @Column(name = "supports_airtel_money")
    @Builder.Default
    private boolean supportsAirtelMoney = false;

    @Column(name = "supports_mpesa")
    @Builder.Default
    private boolean supportsMpesa = false;

    @Column(name = "phone_prefix", nullable = false)
    private String phonePrefix;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
