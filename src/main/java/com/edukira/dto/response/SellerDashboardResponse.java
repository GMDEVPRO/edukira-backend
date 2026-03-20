package com.edukira.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class SellerDashboardResponse {
    private BigDecimal walletBalance;
    private BigDecimal totalEarned;
    private Long totalProducts;
    private Long totalSales;
    private BigDecimal totalWithdrawn;
}
