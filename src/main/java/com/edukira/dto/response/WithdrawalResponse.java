package com.edukira.dto.response;

import com.edukira.enums.WithdrawalMethod;
import com.edukira.enums.WithdrawalStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class WithdrawalResponse {
    private UUID id;
    private BigDecimal amount;
    private WithdrawalMethod withdrawalMethod;
    private String mobileOperator;
    private String mobileNumber;
    private String bankName;
    private WithdrawalStatus status;
    private String reference;
    private Instant requestedAt;
    private Instant processedAt;
}
