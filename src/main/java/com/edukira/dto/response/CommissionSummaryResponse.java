package com.edukira.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommissionSummaryResponse {
    private BigDecimal totalCommissionAllTime;
    private BigDecimal totalCommissionThisMonth;
    private long totalOrders;
}
