package com.edukira.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommissionResponse {
    private UUID id;
    private UUID orderId;
    private String productTitle;
    private String sellerDisplayName;
    private BigDecimal saleAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal sellerNet;
    private String currency;
    private Instant createdAt;
}
