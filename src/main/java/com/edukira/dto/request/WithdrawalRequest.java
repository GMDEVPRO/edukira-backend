package com.edukira.dto.request;

import com.edukira.enums.WithdrawalMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalRequest {

    @NotNull
    @DecimalMin("1.00")
    private BigDecimal amount;

    @NotNull
    private WithdrawalMethod withdrawalMethod;

    // ── Mobile Money ──
    private String mobileOperator;  // WAVE, ORANGE, MTN
    private String mobileNumber;

    // ── Bank Transfer ──
    private String bankName;
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankIban;
    private String bankSwift;
    private String bankCountry;
}
