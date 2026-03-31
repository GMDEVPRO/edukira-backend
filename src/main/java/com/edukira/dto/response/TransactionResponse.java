package com.edukira.dto.response;

import com.edukira.enums.TransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String currency;
    private UUID referenceId;
    private String referenceType;
    private String description;
    private Instant createdAt;
}
